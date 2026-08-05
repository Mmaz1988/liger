package de.ukon.liger.webservice.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerMergeResponse;
import de.ukon.liger.webservice.rest.dtos.LigerWebGraph;
import de.ukon.liger.webservice.rest.dtos.LigerStructureMergeRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LigerStructureMergeRequestTest {

    @Test
    void deserializesSyntaxGraphPayload() throws Exception {
        String json = """
                {
                  "syntaxGraph": {
                    "graphElements": [],
                    "semantics": ""
                  },
                  "drs": {
                    "id": "drs-1",
                    "text": "drs",
                    "constraints": [],
                    "annotations": [],
                    "choiceSpace": {}
                  }
                }
                """;

        LigerStructureMergeRequest request = new ObjectMapper().readValue(json, LigerStructureMergeRequest.class);

        assertNotNull(request.syntaxGraph);
        assertEquals(0, request.syntaxGraph.graphElements.size());
        assertEquals("drs-1", request.drs.get("id"));
    }

    @Test
    void parsesSemanticConstraintShape() {
        java.util.LinkedHashMap<String, Object> semanticConstraint = new java.util.LinkedHashMap<>();
        semanticConstraint.put("sourceNode", 2);
        semanticConstraint.put("relationLabel", "NAME");
        semanticConstraint.put("targetNode", "x1");

        java.util.LinkedHashMap<String, Object> json = new java.util.LinkedHashMap<>();
        json.put("id", "drs-1");
        json.put("text", "drs");
        json.put("constraints", java.util.List.of(semanticConstraint));
        json.put("annotations", java.util.List.of());
        json.put("choiceSpace", new java.util.LinkedHashMap<>());

        LinguisticStructure structure = LinguisticStructure.parseFromJson(json);
        GraphConstraint parsedConstraint = structure.constraints.get(0);

        assertEquals("2", parsedConstraint.getFsNode());
        assertEquals("NAME", parsedConstraint.getRelationLabel());
        assertEquals("x1", parsedConstraint.getFsValue());
    }

    @Test
    void preservesCanonicalConditionTermsWithoutPredicateFacts() {
        java.util.LinkedHashMap<String, Object> root = node("d1", "root", "s0");
        java.util.LinkedHashMap<String, Object> referent = node("d2", "referent", "x1");
        java.util.LinkedHashMap<String, Object> condition = node("d1:c1", "condition", "ant");
        java.util.LinkedHashMap<String, Object> json = new java.util.LinkedHashMap<>();
        json.put("id", "drs-1");
        json.put("text", "drs");
        json.put("nodes", java.util.List.of(root, referent, condition));
        json.put("edges", java.util.List.of(
                edge("d1", "IN", "d2"),
                edge("d1", "COND", "d1:c1"),
                edge("d1:c1", "TERM1", "d2")));
        json.put("annotations", java.util.List.of());
        json.put("choiceSpace", new java.util.LinkedHashMap<>());

        LinguisticStructure structure = LinguisticStructure.parseFromJson(json);

        assertTrue(structure.constraints.stream().anyMatch(constraint ->
                "d1:c1".equals(constraint.getFsNode())
                        && "TERM1".equals(constraint.getRelationLabel())
                        && "d2".equals(constraint.getFsValue())));
        assertTrue(structure.constraints.stream().noneMatch(constraint ->
                "ant".equals(constraint.getRelationLabel())));
    }

    @Test
    void renderingDoesNotRemoveAnnotationFactsFromTheStructure() {
        GraphConstraint annotation = new GraphConstraint(
                java.util.Set.of(), "d2", "SEM", "event", null, false);
        java.util.List<GraphConstraint> syntax = new java.util.ArrayList<>();
        java.util.List<GraphConstraint> annotations = new java.util.ArrayList<>(java.util.List.of(annotation));

        new LigerWebGraph(syntax, annotations);

        assertEquals(1, annotations.size());
    }

    private static java.util.LinkedHashMap<String, Object> node(String id, String type, String label) {
        java.util.LinkedHashMap<String, Object> node = new java.util.LinkedHashMap<>();
        node.put("id", id);
        node.put("node_type", type);
        node.put("label", label);
        return node;
    }

    private static java.util.LinkedHashMap<String, Object> edge(String source, String label, String target) {
        java.util.LinkedHashMap<String, Object> edge = new java.util.LinkedHashMap<>();
        edge.put("source", source);
        edge.put("label", label);
        edge.put("target", target);
        return edge;
    }

    @Test
    void mergePreservesPrefixedSemanticNodeIds() {
        java.util.LinkedHashMap<String, Object> name1 = new java.util.LinkedHashMap<>();
        name1.put("sourceNode", "001");
        name1.put("relationLabel", "NAME");
        name1.put("targetNode", "s0");

        java.util.LinkedHashMap<String, Object> type1 = new java.util.LinkedHashMap<>();
        type1.put("sourceNode", "001");
        type1.put("relationLabel", "NODE_TYPE");
        type1.put("targetNode", "root");

        java.util.LinkedHashMap<String, Object> name2 = new java.util.LinkedHashMap<>();
        name2.put("sourceNode", "002");
        name2.put("relationLabel", "NAME");
        name2.put("targetNode", "x1");

        java.util.LinkedHashMap<String, Object> type2 = new java.util.LinkedHashMap<>();
        type2.put("sourceNode", "002");
        type2.put("relationLabel", "NODE_TYPE");
        type2.put("targetNode", "referent");

        java.util.LinkedHashMap<String, Object> edge = new java.util.LinkedHashMap<>();
        edge.put("sourceNode", "001");
        edge.put("relationLabel", "IN");
        edge.put("targetNode", "002");

        java.util.LinkedHashMap<String, Object> drs = new java.util.LinkedHashMap<>();
        drs.put("id", "drs-1");
        drs.put("text", "drs");
        drs.put("constraints", java.util.List.of(type1, name1, type2, name2, edge));
        drs.put("annotations", java.util.List.of());
        drs.put("choiceSpace", new java.util.LinkedHashMap<>());

        LigerController controller = new LigerController();
        LigerStructureMergeRequest request = new LigerStructureMergeRequest();
        request.drs = drs;

        LigerMergeResponse response = controller.mergeUploadedStructures(request);

        assertTrue(response.graph.graphElements.stream().anyMatch(element -> "001".equals(String.valueOf(element.data.get("id")))));
        assertTrue(response.graph.graphElements.stream().anyMatch(element -> "002".equals(String.valueOf(element.data.get("id")))));
        assertTrue(response.graph.graphElements.stream().anyMatch(element -> "001".equals(String.valueOf(element.data.get("id")))
                && "root".equals(String.valueOf(element.data.get("node_type")))
                && "001".equals(String.valueOf(element.data.get("label")))));
        assertTrue(response.graph.graphElements.stream().anyMatch(element -> "002".equals(String.valueOf(element.data.get("id")))
                && "referent".equals(String.valueOf(element.data.get("node_type")))
                && "002".equals(String.valueOf(element.data.get("label")))));
    }

    @Test
    void mergeResponseIncludesMergedStructureJson() {
        java.util.LinkedHashMap<String, Object> syntaxNode = new java.util.LinkedHashMap<>();
        syntaxNode.put("sourceNode", "1");
        syntaxNode.put("relationLabel", "NODE_TYPE");
        syntaxNode.put("targetNode", "input");

        java.util.LinkedHashMap<String, Object> drs = new java.util.LinkedHashMap<>();
        drs.put("id", "drs-1");
        drs.put("text", "drs");
        drs.put("constraints", java.util.List.of(syntaxNode));
        drs.put("annotations", java.util.List.of());
        drs.put("choiceSpace", new java.util.LinkedHashMap<>());

        LigerController controller = new LigerController();
        LigerStructureMergeRequest request = new LigerStructureMergeRequest();
        request.drs = drs;

        LigerMergeResponse response = controller.mergeUploadedStructures(request);

        assertNotNull(response.structureJson);
        assertEquals("drs-1", response.structureJson.get("id"));
        assertTrue(response.structureJson.containsKey("constraints"));
    }
}
