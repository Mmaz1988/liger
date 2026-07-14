package de.ukon.liger.webservice.rest;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerGraphComponent;
import de.ukon.liger.webservice.rest.dtos.LigerWebGraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class LigerGraphTranslator {

    private LigerGraphTranslator() {
    }

    static LinguisticStructure translate(LigerWebGraph graph) {
        LinguisticStructure structure = new LinguisticStructure();
        structure.local_id = graph != null && graph.semantics != null && !graph.semantics.isBlank()
                ? graph.semantics
                : "translated-graph";
        structure.text = structure.local_id;
        structure.constraints = new ArrayList<>();
        structure.annotation = new ArrayList<>();
        structure.cp = new ChoiceSpace();

        if (graph == null || graph.graphElements == null || graph.graphElements.isEmpty()) {
            return structure;
        }

        Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (LigerGraphComponent component : graph.graphElements) {
            if (component == null || component.data == null) {
                continue;
            }

            Map<String, Object> data = component.data;
            if (isEdge(data)) {
                edges.add(data);
                continue;
            }

            Object idValue = data.get("id");
            if (idValue == null) {
                continue;
            }

            nodes.put(String.valueOf(idValue), data);
        }

        List<GraphConstraint> constraints = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : nodes.entrySet()) {
            String nodeId = entry.getKey();
            Map<String, Object> data = entry.getValue();
            String nodeProjection = extractProjection(data);
            String nodeType = stringValue(data.get("node_type"));
            String derivedProjection = projectionFromNodeType(nodeType, nodeProjection);

            if (nodeType != null && !nodeType.isBlank()) {
                constraints.add(createConstraint(nodeId, "NODE_TYPE", nodeType, derivedProjection));
            }

            Object avp = data.get("avp");
            if (avp instanceof Map<?, ?> avpMap) {
                for (Map.Entry<?, ?> avpEntry : avpMap.entrySet()) {
                    String relation = String.valueOf(avpEntry.getKey());
                    if (relation.isBlank() || isReservedNodeField(relation)) {
                        continue;
                    }

                    String value = stringValue(avpEntry.getValue());
                    if (value != null && !value.isBlank()) {
                        constraints.add(createConstraint(nodeId, relation, value, derivedProjection));
                    }
                }
            }
        }

        for (Map<String, Object> data : edges) {
            String source = stringValue(data.get("source"));
            String target = stringValue(data.get("target"));
            String label = stringValue(data.get("label"));

            if (source == null || target == null || label == null || label.isBlank()) {
                continue;
            }

            String projection = projectionFromNodeType(
                    stringValue(nodes.get(source) == null ? null : nodes.get(source).get("node_type")),
                    extractProjection(nodes.get(source)));
            constraints.add(createConstraint(source, label, target, projection));
        }

        structure.constraints = dedupeConstraints(constraints);
        return structure;
    }

    private static GraphConstraint createConstraint(String nodeId, String relationLabel, String value, String projection) {
        GraphConstraint constraint = new GraphConstraint();
        Set<ChoiceVar> reading = new LinkedHashSet<>();
        reading.add(new ChoiceVar("1"));
        constraint.setReading(reading);
        constraint.setFsNode(nodeId);
        constraint.setRelationLabel(relationLabel);
        constraint.setFsValue(value);
        constraint.setProj(projection);
        return constraint;
    }

    private static List<GraphConstraint> dedupeConstraints(List<GraphConstraint> constraints) {
        Map<String, GraphConstraint> unique = new LinkedHashMap<>();
        for (GraphConstraint constraint : constraints) {
            unique.putIfAbsent(constraintKey(constraint), constraint);
        }
        return new ArrayList<>(unique.values());
    }

    private static String constraintKey(GraphConstraint constraint) {
        return String.join("|",
                stringValue(constraint.getFsNode()),
                stringValue(constraint.getRelationLabel()),
                stringValue(constraint.getFsValue()),
                stringValue(constraint.getProj()));
    }

    private static boolean isEdge(Map<String, Object> data) {
        return data.containsKey("source") && data.containsKey("target");
    }

    private static boolean isReservedNodeField(String key) {
        return "id".equals(key)
                || "label".equals(key)
                || "node_type".equals(key)
                || "avp".equals(key)
                || "projection".equals(key);
    }

    private static String extractProjection(Map<String, Object> data) {
        if (data == null) {
            return null;
        }

        Object projection = data.get("projection");
        if (projection != null && !String.valueOf(projection).isBlank()) {
            return String.valueOf(projection);
        }

        Object avp = data.get("avp");
        if (avp instanceof Map<?, ?> avpMap) {
            Object nestedProjection = avpMap.get("projection");
            if (nestedProjection != null && !String.valueOf(nestedProjection).isBlank()) {
                return String.valueOf(nestedProjection);
            }
        }

        return null;
    }

    private static String projectionFromNodeType(String nodeType, String fallback) {
        if (nodeType == null || nodeType.isBlank()) {
            return fallback;
        }

        if ("cnode".equals(nodeType)) {
            return "c";
        }

        if ("gnode".equals(nodeType)) {
            return "g";
        }

        return fallback;
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
