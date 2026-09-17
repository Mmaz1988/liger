package de.ukon.liger.webservice.rest;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerDrsProvenanceResponse;
import de.ukon.liger.webservice.rest.dtos.LigerProvenanceCondition;
import de.ukon.liger.webservice.rest.dtos.LigerProvenanceReferent;
import de.ukon.liger.webservice.rest.dtos.LigerSourceToken;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps the elements of one composed DRS back onto the input string.
 *
 * Every condition node of a GSWB semantic graph carries {@code SRC in}, the source index of the
 * meaning constructor it came from; {@link SourceTokenIndex} resolves that to tokens.
 *
 * A discourse referent has no surface span of its own: what describes {@code x} in
 * <i>the black dog on the table barked</i> is {@code dog(x), black(x), on(x,y), table(y)}, not
 * the token <i>dog</i>. Following every condition on {@code x} is too much, though --
 * {@code arg1(e,x)} would pull in <i>barked</i>. The description is therefore bounded by the
 * f-structure of the referent's own source (its determiner or head): a condition belongs to it
 * iff its tokens project into that f-structure, and the search continues through the other
 * arguments of the conditions that do. That yields <i>the black dog on the table</i> for the
 * dog and <i>the table</i> for the table, and keeps clause-level predications out.
 *
 * An event referent's f-structure is its clause, so its description is the whole clause;
 * {@link LigerProvenanceReferent#directConditionIds} is returned alongside for callers that
 * want only the conditions on the referent itself.
 *
 * The lexical description ({@link LigerProvenanceReferent#lexicalText}) is narrower: only the
 * one-place conditions on the referent, i.e. its lexical content without its relations to
 * other referents -- <i>black dog</i>, <i>barked</i>, <i>he</i>.
 */
@Service
public class DrsProvenanceService {

    public LigerDrsProvenanceResponse describe(LinguisticStructure syntax, LinguisticStructure drs) {
        SourceTokenIndex index = new SourceTokenIndex(syntax);
        DrsGraph graph = new DrsGraph(drs);
        LigerDrsProvenanceResponse response = new LigerDrsProvenanceResponse();
        response.text = index.text();

        LinkedHashMap<String, LigerSourceToken> tokens = new LinkedHashMap<>();
        Map<String, List<LigerSourceToken>> tokensByNode = new LinkedHashMap<>();
        for (String node : graph.nodes()) {
            Integer source = graph.sourceIndex(node);
            List<LigerSourceToken> nodeTokens = source == null ? List.of() : index.tokensFor(List.of(source));
            tokensByNode.put(node, nodeTokens);
            nodeTokens.forEach(token -> tokens.putIfAbsent(token.id, token));
            if (source != null && nodeTokens.isEmpty()) {
                response.warnings.add("Source index i" + source + " of " + describeNode(graph, node)
                        + " resolves to no token.");
            }
        }
        response.tokens = tokens.values().stream()
                .sorted(Comparator.comparingInt((LigerSourceToken t) -> t.start).thenComparingInt(t -> t.end))
                .collect(Collectors.toList());
        for (LigerSourceToken token : response.tokens) {
            if (token.start < 0 || token.end > response.text.length()
                    || !response.text.substring(token.start, token.end).equalsIgnoreCase(token.form)) {
                response.warnings.add("Token '" + token.form + "' at [" + token.start + "," + token.end
                        + ") does not match the input text.");
            }
        }

        for (String node : graph.nodesOfType("condition")) {
            LigerProvenanceCondition condition = new LigerProvenanceCondition();
            condition.nodeId = node;
            condition.symbol = graph.name(node);
            condition.args = graph.arguments(node);
            condition.sourceIndex = graph.sourceIndex(node);
            fillSurface(tokensByNode.get(node), response.text, condition.tokenIds, condition.spans);
            condition.text = textOf(condition.spans, response.text);
            if (condition.sourceIndex == null) {
                response.warnings.add(describeNode(graph, node) + " has no SRC.");
            }
            response.conditions.add(condition);
        }

        for (String node : graph.nodesOfType("referent")) {
            response.referents.add(describeReferent(node, graph, index, tokensByNode, response));
        }
        return response;
    }

    private LigerProvenanceReferent describeReferent(String node, DrsGraph graph, SourceTokenIndex index,
                                                     Map<String, List<LigerSourceToken>> tokensByNode,
                                                     LigerDrsProvenanceResponse response) {
        LigerProvenanceReferent referent = new LigerProvenanceReferent();
        referent.nodeId = node;
        referent.label = graph.name(node);
        referent.sourceIndex = graph.sourceIndex(node);
        referent.directConditionIds = graph.conditionsOn(node);

        List<LigerSourceToken> ownTokens = tokensByNode.getOrDefault(node, List.of());
        referent.fsNode = outermostFsNode(ownTokens, index);
        LinkedHashSet<String> describingNodes = new LinkedHashSet<>();
        if (referent.fsNode == null) {
            response.warnings.add("No f-structure for referent " + referent.label + " (" + node
                    + "); its description falls back to the conditions directly on it.");
            describingNodes.add(node);
            describingNodes.addAll(referent.directConditionIds);
        } else {
            Set<String> subtree = index.fsSubtree(referent.fsNode);
            Deque<String> queue = new ArrayDeque<>(List.of(node));
            Set<String> visitedReferents = new HashSet<>(List.of(node));
            while (!queue.isEmpty()) {
                String current = queue.poll();
                if (inside(tokensByNode.get(current), subtree)) {
                    describingNodes.add(current);
                }
                for (String condition : graph.conditionsOn(current)) {
                    if (describingNodes.contains(condition) || !inside(tokensByNode.get(condition), subtree)) {
                        continue;
                    }
                    describingNodes.add(condition);
                    for (String argument : graph.arguments(condition)) {
                        if (graph.isReferent(argument) && visitedReferents.add(argument)) {
                            queue.add(argument);
                        }
                    }
                }
            }
        }

        referent.descriptionConditionIds = describingNodes.stream()
                .filter(graph::isCondition).collect(Collectors.toList());
        referent.descriptionIndices = describingNodes.stream()
                .map(graph::sourceIndex).filter(Objects::nonNull).distinct().sorted()
                .collect(Collectors.toList());
        LinkedHashSet<LigerSourceToken> describingTokens = new LinkedHashSet<>();
        describingNodes.forEach(n -> describingTokens.addAll(tokensByNode.getOrDefault(n, List.of())));
        fillSurface(new ArrayList<>(describingTokens), response.text, referent.tokenIds, referent.spans);
        referent.text = textOf(referent.spans, response.text);

        referent.lexicalConditionIds = referent.directConditionIds.stream()
                .filter(condition -> graph.arguments(condition).equals(List.of(node)))
                .collect(Collectors.toList());
        LinkedHashSet<LigerSourceToken> lexicalTokens = new LinkedHashSet<>();
        referent.lexicalConditionIds.forEach(c -> lexicalTokens.addAll(tokensByNode.getOrDefault(c, List.of())));
        if (lexicalTokens.isEmpty()) {
            lexicalTokens.addAll(ownTokens);
            referent.lexicalFromReferentSource = !ownTokens.isEmpty();
        }
        fillSurface(new ArrayList<>(lexicalTokens), response.text, referent.lexicalTokenIds, referent.lexicalSpans);
        referent.lexicalText = textOf(referent.lexicalSpans, response.text);

        if (referent.fsNode != null) {
            Set<String> subtree = index.fsSubtree(referent.fsNode);
            List<LigerSourceToken> phraseTokens = index.allTokens().stream()
                    .filter(token -> token.fsNodes.stream().anyMatch(subtree::contains))
                    .toList();
            fillSurface(phraseTokens, response.text, referent.phraseTokenIds, referent.phraseSpans);
            referent.phraseText = textOf(referent.phraseSpans, response.text);
        }
        return referent;
    }

    /** Of the f-structures the tokens project to, the one containing the most of the others
     *  (a terminal can project to a disconnected f-structure that nothing else contains). */
    private String outermostFsNode(List<LigerSourceToken> tokens, SourceTokenIndex index) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        tokens.forEach(token -> candidates.addAll(token.fsNodes));
        String best = null;
        long bestCount = 0;
        for (String candidate : candidates) {
            Set<String> subtree = index.fsSubtree(candidate);
            long count = candidates.stream().filter(subtree::contains).count();
            if (count > bestCount) {
                best = candidate;
                bestCount = count;
            }
        }
        return best;
    }

    private static boolean inside(List<LigerSourceToken> tokens, Set<String> subtree) {
        return tokens != null && !tokens.isEmpty()
                && tokens.stream().allMatch(token -> token.fsNodes.stream().anyMatch(subtree::contains));
    }

    private static void fillSurface(List<LigerSourceToken> tokens, String text,
                                    List<String> tokenIds, List<int[]> spans) {
        if (tokens == null) {
            return;
        }
        List<LigerSourceToken> ordered = tokens.stream()
                .sorted(Comparator.comparingInt((LigerSourceToken t) -> t.start).thenComparingInt(t -> t.end))
                .toList();
        for (LigerSourceToken token : ordered) {
            tokenIds.add(token.id);
            int[] last = spans.isEmpty() ? null : spans.get(spans.size() - 1);
            if (last != null && token.start >= last[1] && token.start <= text.length()
                    && text.substring(last[1], token.start).isBlank()) {
                last[1] = Math.max(last[1], token.end);
            } else if (last != null && token.start < last[1]) {
                last[1] = Math.max(last[1], token.end);
            } else {
                spans.add(new int[]{token.start, token.end});
            }
        }
    }

    private static String textOf(List<int[]> spans, String text) {
        return spans.stream()
                .filter(span -> span[0] >= 0 && span[1] <= text.length())
                .map(span -> text.substring(span[0], span[1]))
                .collect(Collectors.joining(" "));
    }

    private static String describeNode(DrsGraph graph, String node) {
        return graph.type(node) + " " + node + " (" + graph.name(node) + ")";
    }

    /** Read access to a GSWB semantic graph parsed into constraints by {@link LinguisticStructure#parseFromJson}. */
    private static final class DrsGraph {
        private final Map<String, Map<String, List<String>>> outgoing = new LinkedHashMap<>();

        DrsGraph(LinguisticStructure drs) {
            if (drs == null) {
                return;
            }
            for (GraphConstraint constraint : drs.returnFullGraph()) {
                if (constraint.getFsNode() == null || constraint.getRelationLabel() == null
                        || constraint.getFsValue() == null) {
                    continue;
                }
                outgoing.computeIfAbsent(constraint.getFsNode(), k -> new LinkedHashMap<>())
                        .computeIfAbsent(constraint.getRelationLabel(), k -> new ArrayList<>())
                        .add(String.valueOf(constraint.getFsValue()));
            }
        }

        List<String> nodes() {
            return outgoing.keySet().stream().filter(node -> type(node) != null).toList();
        }

        List<String> nodesOfType(String type) {
            return nodes().stream().filter(node -> type.equals(type(node))).toList();
        }

        String type(String node) {
            return first(node, "NODE_TYPE");
        }

        boolean isReferent(String node) {
            return "referent".equals(type(node));
        }

        boolean isCondition(String node) {
            return "condition".equals(type(node));
        }

        String name(String node) {
            String name = first(node, "NAME");
            return name == null ? null : SourceTokenIndex.unquote(name);
        }

        Integer sourceIndex(String node) {
            String value = first(node, "SRC");
            if (value == null) {
                return null;
            }
            try {
                return Integer.valueOf(SourceTokenIndex.normalizeIndex(value).substring(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /** Argument node ids of a condition, ordered by TERM number. */
        List<String> arguments(String condition) {
            return outgoing.getOrDefault(condition, Map.of()).entrySet().stream()
                    .filter(entry -> entry.getKey().matches("TERM\\d+"))
                    .sorted(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().substring(4))))
                    .flatMap(entry -> entry.getValue().stream())
                    .collect(Collectors.toList());
        }

        /** Conditions with a TERM edge to the node. */
        List<String> conditionsOn(String node) {
            return nodesOfType("condition").stream()
                    .filter(condition -> arguments(condition).contains(node))
                    .collect(Collectors.toList());
        }

        private String first(String node, String relation) {
            List<String> values = outgoing.getOrDefault(node, Map.of()).get(relation);
            return values == null || values.isEmpty() ? null : values.get(0);
        }
    }
}
