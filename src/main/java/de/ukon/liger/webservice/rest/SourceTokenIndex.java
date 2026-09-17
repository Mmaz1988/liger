package de.ukon.liger.webservice.rest;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerSourceToken;

import java.util.*;

/**
 * Resolves meaning-constructor source indices of one syntactic structure to surface tokens.
 *
 * A source index {@code n} names the meaning constructor carrying {@code SYN-ID in}. The
 * lexical item that introduced it is reached backwards along {@code in_set < GLUE < g:: < cproj}.
 * That lands on several c-structure nodes, of which only one leads to character offsets:
 * <ul>
 *   <li>for an unanalysed word (<i>the</i>, <i>on</i>) the terminal itself carries
 *       {@code token}/{@code start}/{@code end};</li>
 *   <li>for a morphologically analysed word (<i>dog</i>) {@code cproj} reaches the sublexical
 *       node ({@code N-S_BASE}) and the stem node, neither of which carries offsets -- the stem
 *       node's {@code terminal} edge points at the surface node that does.</li>
 * </ul>
 * So {@code terminal} is followed as well, and only nodes carrying a token are kept.
 *
 * XLE reports offsets shifted by one ({@code surfaceform(1,'John',1,5)} for "John" at
 * {@code [0,4)}); tokens returned here are 0-based and end-exclusive, into
 * {@link LinguisticStructure#plainText} -- for a sequence, the sentences joined by
 * {@link LinguisticStructure#SENTENCE_SEPARATOR}, whose offsets the assembler has shifted.
 */
public final class SourceTokenIndex {
    private static final List<String> REVERSE_PATH = List.of("in_set", "GLUE", "g::", "cproj");
    /** Edges leaving the f-structure into other projections; never followed inside an f-structure. */
    private static final Set<String> PROJECTION_LABELS = Set.of("phi", "cproj", "GLUE");

    private final String text;
    private final Map<String, List<GraphConstraint>> outgoing = new LinkedHashMap<>();
    private final Map<String, List<GraphConstraint>> incoming = new LinkedHashMap<>();
    private final Map<String, List<String>> mcNodesBySynId = new LinkedHashMap<>();
    private final Map<String, LigerSourceToken> tokensByNode = new LinkedHashMap<>();

    public SourceTokenIndex(LinguisticStructure structure) {
        this.text = structure == null ? "" : LinguisticStructure.plainText(structure.text);
        if (structure == null) {
            return;
        }
        for (GraphConstraint constraint : structure.returnFullGraph()) {
            if (constraint.getFsNode() == null || constraint.getFsValue() == null) {
                continue;
            }
            String value = String.valueOf(constraint.getFsValue());
            outgoing.computeIfAbsent(constraint.getFsNode(), k -> new ArrayList<>()).add(constraint);
            incoming.computeIfAbsent(value, k -> new ArrayList<>()).add(constraint);
            if ("SYN-ID".equals(constraint.getRelationLabel())) {
                mcNodesBySynId.computeIfAbsent(normalizeIndex(value), k -> new ArrayList<>())
                        .add(constraint.getFsNode());
            }
        }
        for (String node : outgoing.keySet()) {
            LigerSourceToken token = readToken(node);
            if (token != null) {
                tokensByNode.put(node, token);
            }
        }
    }

    /** The structure's input text, unquoted. */
    public String text() {
        return text;
    }

    /** Distinct tokens behind the given source indices, ordered by position. */
    public List<LigerSourceToken> tokensFor(Collection<Integer> sourceIndices) {
        LinkedHashSet<LigerSourceToken> tokens = new LinkedHashSet<>();
        if (sourceIndices == null) {
            return List.of();
        }
        for (Integer sourceIndex : sourceIndices) {
            if (sourceIndex == null) {
                continue;
            }
            for (String mcNode : mcNodesBySynId.getOrDefault("i" + sourceIndex, List.of())) {
                for (String cNode : surfaceCandidates(mcNode)) {
                    LigerSourceToken token = tokensByNode.get(cNode);
                    if (token != null) {
                        tokens.add(token);
                    }
                }
            }
        }
        return tokens.stream()
                .sorted(Comparator.comparingInt((LigerSourceToken t) -> t.start).thenComparingInt(t -> t.end))
                .toList();
    }

    /** Every surface token of the structure, ordered by position. */
    public List<LigerSourceToken> allTokens() {
        return tokensByNode.values().stream()
                .sorted(Comparator.comparingInt((LigerSourceToken t) -> t.start).thenComparingInt(t -> t.end))
                .toList();
    }

    /** Whether the structure carries a meaning constructor with this source index at all. */
    public boolean hasSourceIndex(int sourceIndex) {
        return mcNodesBySynId.containsKey("i" + sourceIndex);
    }

    /** The node itself plus every node reachable from it inside its f-structure. */
    public Set<String> fsSubtree(String fsNode) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        if (fsNode == null) {
            return seen;
        }
        seen.add(fsNode);
        queue.add(fsNode);
        while (!queue.isEmpty()) {
            for (GraphConstraint constraint : outgoing.getOrDefault(queue.poll(), List.of())) {
                String label = constraint.getRelationLabel();
                if (label == null || PROJECTION_LABELS.contains(label) || label.endsWith("::")) {
                    continue;
                }
                String target = String.valueOf(constraint.getFsValue());
                if (outgoing.containsKey(target) && seen.add(target)) {
                    queue.add(target);
                }
            }
        }
        return seen;
    }

    private Set<String> surfaceCandidates(String mcNode) {
        Set<String> current = new LinkedHashSet<>(List.of(mcNode));
        for (String relation : REVERSE_PATH) {
            Set<String> next = new LinkedHashSet<>();
            for (String target : current) {
                for (GraphConstraint constraint : incoming.getOrDefault(target, List.of())) {
                    if (relation.equals(constraint.getRelationLabel())) {
                        next.add(constraint.getFsNode());
                    }
                }
            }
            current = next;
            if (current.isEmpty()) {
                return Set.of();
            }
        }
        Set<String> withTerminals = new LinkedHashSet<>(current);
        for (String cNode : current) {
            withTerminals.addAll(values(cNode, "terminal"));
        }
        return withTerminals;
    }

    private LigerSourceToken readToken(String node) {
        String form = firstValue(node, "token");
        Integer start = integerValue(firstValue(node, "start"));
        Integer end = integerValue(firstValue(node, "end"));
        if (form == null || start == null || end == null) {
            return null;
        }
        LigerSourceToken token = new LigerSourceToken(node, unquote(form), start - 1, end - 1);
        // The terminal's own phi is not reliable: the relative pronoun "that" projects to a
        // disconnected f-structure while its preterminal PRON projects into the noun phrase.
        // So stem nodes pointing here via terminal and the direct mother count as well.
        LinkedHashSet<String> fsNodes = new LinkedHashSet<>(values(node, "phi"));
        for (GraphConstraint constraint : incoming.getOrDefault(node, List.of())) {
            String relation = constraint.getRelationLabel();
            if ("terminal".equals(relation) || "left".equals(relation) || "right".equals(relation)) {
                fsNodes.addAll(values(constraint.getFsNode(), "phi"));
            }
        }
        token.fsNodes = new ArrayList<>(fsNodes);
        return token;
    }

    private List<String> values(String node, String relation) {
        List<String> result = new ArrayList<>();
        for (GraphConstraint constraint : outgoing.getOrDefault(node, List.of())) {
            if (relation.equals(constraint.getRelationLabel())) {
                result.add(String.valueOf(constraint.getFsValue()));
            }
        }
        return result;
    }

    private String firstValue(String node, String relation) {
        List<String> values = values(node, relation);
        return values.isEmpty() ? null : values.get(0);
    }

    private static Integer integerValue(String value) {
        if (value == null) return null;
        String normalized = value.replaceAll("[^0-9-]", "");
        return normalized.isEmpty() ? null : Integer.valueOf(normalized);
    }

    static String normalizeIndex(String value) {
        String normalized = value.trim();
        return normalized.startsWith("i") ? normalized : "i" + normalized;
    }

    static String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
