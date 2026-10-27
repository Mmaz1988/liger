package de.ukon.liger.semantics;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.utilities.HelperMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Source indices of a UD parse's meaning constructors, as XLE gives them
 * (xleplusglue docs/plans/UD_PARSING_PLAN.md).
 *
 * <p>XLE numbers meaning-constructor nodes, not words: {@code SYN-ID i1..in} on the MC nodes in
 * c-structure order, each MC its own index, and an index resolves to the token of the lexical
 * item that introduced it. In a UD parse every MC comes from a rule, as a {@code GLUE} fact on
 * a node a rule created. That node's <em>anchor</em> is the word it hangs from: follow the edge
 * each node was created with -- its first incoming edge -- back until a word node (one carrying
 * {@code start}). A rule chooses the anchor by where it hangs the GLUE node: a determiner's MC
 * from the determiner ({@code #d DET-SEM #s & #s GLUE ...}), a noun's from the noun, a verb's
 * closure from its event node, which hangs from the verb. Edges added to a node later (a noun
 * phrase's {@code SCOPE} pointing at its clause's node) do not move it.
 *
 * <p>The MC nodes are numbered in the order of their anchors' positions, ties in the order
 * the rules added them. One MC per node: a node carrying several shares one index, which is
 * reported. Each numbered node also gets {@code ANCHOR} to its word: what XLE's path
 * {@code in_set > GLUE > g:: > cproj} is to its constructors, for rules that link a referent
 * ({@code SRC}) to the syntax (a UD {@code DR-GF-LINK}).
 */
public final class UdSourceIndices {
    private static final Logger LOGGER = LoggerFactory.getLogger(UdSourceIndices.class);

    private UdSourceIndices() {
    }

    /** Sets {@code SYN-ID} on every MC node of a UD structure that has none yet. */
    public static void number(LinguisticStructure structure) {
        Map<String, Set<ChoiceVar>> mcNodes = mcNodes(structure);
        if (mcNodes.isEmpty()) {
            return;
        }
        List<GraphConstraint> graph = structure.returnFullGraph();
        Set<String> numbered = new HashSet<>();
        for (GraphConstraint constraint : graph) {
            if ("SYN-ID".equals(constraint.getRelationLabel())) {
                numbered.add(constraint.getFsNode());
            }
        }
        if (numbered.containsAll(mcNodes.keySet())) {
            return;
        }
        Graph index = new Graph(graph);
        List<String> order = new ArrayList<>(mcNodes.keySet());
        Map<String, String> anchors = new LinkedHashMap<>();
        for (String node : order) {
            String anchor = index.anchorOf(node);
            if (anchor == null) {
                LOGGER.error("UD structure '" + structure.local_id + "': the meaning constructor on " + node
                        + " hangs from no word; it gets no source index.");
            } else {
                anchors.put(node, anchor);
            }
        }
        List<String> anchored = order.stream().filter(anchors::containsKey)
                .sorted(Comparator.comparingInt(node -> index.position(anchors.get(node))))
                .toList();
        structure.constraints.removeIf(constraint -> "SYN-ID".equals(constraint.getRelationLabel())
                || "ANCHOR".equals(constraint.getRelationLabel()));
        int next = 1;
        for (String node : anchored) {
            structure.constraints.add(fact(node, "SYN-ID", "i" + next++, mcNodes.get(node)));
            structure.constraints.add(fact(node, "ANCHOR", anchors.get(node), mcNodes.get(node)));
        }
    }

    private static GraphConstraint fact(String node, String relation, String value, Set<ChoiceVar> reading) {
        GraphConstraint constraint = new GraphConstraint();
        constraint.setFsNode(node);
        constraint.setRelationLabel(relation);
        constraint.setFsValue(value);
        constraint.setReading(new LinkedHashSet<>(reading));
        return constraint;
    }

    /** The word an MC node hangs from; null if none. */
    public static String anchorOf(List<GraphConstraint> graph, String node) {
        return new Graph(graph).anchorOf(node);
    }

    /** MC nodes in the order the rules added their GLUE facts, with the reading of each. */
    private static Map<String, Set<ChoiceVar>> mcNodes(LinguisticStructure structure) {
        Map<String, Set<ChoiceVar>> nodes = new LinkedHashMap<>();
        Map<String, Integer> mcs = new LinkedHashMap<>();
        for (GraphConstraint constraint : structure.annotation == null ? List.<GraphConstraint>of() : structure.annotation) {
            if ("GLUE".equals(constraint.getRelationLabel()) && constraint.getFsNode() != null
                    && !HelperMethods.isNodeReference(constraint.getFsValue())) {
                nodes.computeIfAbsent(constraint.getFsNode(), key -> new LinkedHashSet<>())
                        .addAll(constraint.getReading() == null ? Set.of() : constraint.getReading());
                mcs.merge(constraint.getFsNode(), 1, Integer::sum);
            }
        }
        mcs.forEach((node, count) -> {
            if (count > 1) {
                LOGGER.warn("UD structure '" + structure.local_id + "': " + count + " meaning constructors hang on "
                        + node + " and share one source index; hang each on a node of its own.");
            }
        });
        return nodes;
    }

    private static final class Graph {
        /** Per node, the sources of its incoming edges in the order they were added. */
        private final Map<String, List<String>> incoming = new LinkedHashMap<>();
        private final Map<String, Integer> positions = new LinkedHashMap<>();

        Graph(List<GraphConstraint> graph) {
            for (GraphConstraint constraint : graph) {
                String node = constraint.getFsNode();
                Object value = constraint.getFsValue();
                if (node == null || value == null) {
                    continue;
                }
                if ("start".equals(constraint.getRelationLabel())) {
                    positions.put(node, integer(String.valueOf(value)));
                } else if (HelperMethods.isNodeReference(value)) {
                    incoming.computeIfAbsent(String.valueOf(value), key -> new ArrayList<>()).add(node);
                }
            }
        }

        boolean isWord(String node) {
            return positions.containsKey(node);
        }

        int position(String word) {
            return positions.getOrDefault(word, Integer.MAX_VALUE);
        }

        /** Back along the edge each node was created with, to a word; null if there is none. */
        String anchorOf(String node) {
            Set<String> seen = new HashSet<>();
            String current = node;
            while (current != null && seen.add(current)) {
                if (isWord(current)) {
                    return current;
                }
                List<String> sources = incoming.getOrDefault(current, List.of());
                current = sources.isEmpty() ? null : sources.get(0);
            }
            return null;
        }

        private static int integer(String value) {
            String digits = value.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(digits);
        }
    }
}
