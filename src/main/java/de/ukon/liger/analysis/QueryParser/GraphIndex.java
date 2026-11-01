package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A structure's facts by label and by node, built once per parser and shared with the parsers of
 * its negations, so that building a query's nodes and attributes is a lookup, not a scan of every
 * fact. Every list is in fact order, as the scans returned them.
 */
final class GraphIndex {

    private final Map<Integer, GraphConstraint> facts;
    private final int size;
    private final Map<String, LinkedHashMap<Integer, GraphConstraint>> factsByLabel = new HashMap<>();
    private final Map<String, LinkedHashMap<Integer, GraphConstraint>> factsByNode = new HashMap<>();
    private final List<String> nodes;

    GraphIndex(Map<Integer, GraphConstraint> facts) {
        this.facts = facts;
        this.size = facts.size();
        Set<String> nodes = new LinkedHashSet<>();
        for (Map.Entry<Integer, GraphConstraint> entry : facts.entrySet()) {
            GraphConstraint fact = entry.getValue();
            factsByLabel.computeIfAbsent(fact.getRelationLabel(), l -> new LinkedHashMap<>()).put(entry.getKey(), fact);
            factsByNode.computeIfAbsent(fact.getFsNode(), n -> new LinkedHashMap<>()).put(entry.getKey(), fact);
            nodes.add(fact.getFsNode());
        }
        // Then the nodes that occur only as a value, in fact order (Node's order)
        for (GraphConstraint fact : facts.values()) {
            if (HelperMethods.isNodeReference(fact.getFsValue())) {
                nodes.add(String.valueOf(fact.getFsValue()));
            }
        }
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
    }

    /** Whether this index still describes these facts: the same map, nothing added or removed. */
    boolean describes(Map<Integer, GraphConstraint> facts) {
        return this.facts == facts && size == facts.size();
    }

    /** The facts with this label; not to be modified. */
    Map<Integer, GraphConstraint> withLabel(String label) {
        LinkedHashMap<Integer, GraphConstraint> found = factsByLabel.get(label);
        return found == null ? Collections.emptyMap() : found;
    }

    boolean hasLabel(String label) {
        return factsByLabel.containsKey(label);
    }

    /** The facts of this node; not to be modified. */
    Map<Integer, GraphConstraint> ofNode(String node) {
        LinkedHashMap<Integer, GraphConstraint> found = factsByNode.get(node);
        return found == null ? Collections.emptyMap() : found;
    }

    /** Every node: the facts' nodes, then the nodes that occur only as a value. */
    List<String> nodes() {
        return nodes;
    }
}
