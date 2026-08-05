package de.ukon.liger.webservice.rest;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerWebEdge;
import de.ukon.liger.webservice.rest.dtos.LigerWebGraph;
import de.ukon.liger.webservice.rest.dtos.LigerWebNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LigerGraphTranslatorTest {

    @Test
    void translatesGraphElementsIntoConstraints() {
        LigerWebGraph graph = new LigerWebGraph(new ArrayList<>());

        HashMap<String, String> avp = new HashMap<>();
        avp.put("PRED", "man");
        graph.graphElements.add(new LigerWebNode("1", "input", "1", avp));
        graph.graphElements.add(new LigerWebNode("2", "cnode", "s1"));
        graph.graphElements.add(new LigerWebEdge("edge-1", "1", "2", "ARG0", "edge"));

        LinguisticStructure translated = LigerGraphTranslator.translate(graph);

        assertEquals("translated-graph", translated.local_id);
        assertTrue(translated.constraints.stream().anyMatch(c -> "1".equals(c.getFsNode()) && "NODE_TYPE".equals(c.getRelationLabel()) && "input".equals(c.getFsValue())));
        assertTrue(translated.constraints.stream().anyMatch(c -> "1".equals(c.getFsNode()) && "PRED".equals(c.getRelationLabel()) && "man".equals(c.getFsValue())));
        assertTrue(translated.constraints.stream().anyMatch(c -> "1".equals(c.getFsNode()) && "ARG0".equals(c.getRelationLabel()) && "2".equals(c.getFsValue())));
    }

    @Test
    void linguisticStructureConstructorPreservesTypedNodes() {
        LinguisticStructure structure = new LinguisticStructure();
        structure.constraints = new ArrayList<>();
        structure.annotation = new ArrayList<>();
        structure.cp = new ChoiceSpace();
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "1", "PRED", "dog", "c", false));
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "2", "PRED", "bark", "g", false));
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "3", "PRED", "walk", "f", false));

        LigerWebGraph graph = new LigerWebGraph(structure);

        assertTrue(graph.graphElements.stream().anyMatch(component -> "1".equals(component.data.get("id")) && "cnode".equals(component.data.get("node_type"))));
        assertTrue(graph.graphElements.stream().anyMatch(component -> "2".equals(component.data.get("id")) && "gnode".equals(component.data.get("node_type"))));
        assertTrue(graph.graphElements.stream().anyMatch(component -> "3".equals(component.data.get("id")) && "input".equals(component.data.get("node_type"))));
    }

    @Test
    void linguisticStructureJsonRoundTripPreservesProjectionMetadata() {
        LinguisticStructure structure = new LinguisticStructure();
        structure.local_id = "merged-graph";
        structure.text = "merged graph";
        structure.constraints = new ArrayList<>();
        structure.annotation = new ArrayList<>();
        structure.cp = new ChoiceSpace();
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "1", "PRED", "dog", "c", false));
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "2", "PRED", "bark", "g", false));

        LinguisticStructure parsed = LinguisticStructure.parseFromJson(structure.toJson());
        LigerWebGraph graph = new LigerWebGraph(parsed);

        assertTrue(parsed.constraints.stream().anyMatch(constraint -> "c".equals(constraint.getProj())));
        assertTrue(parsed.constraints.stream().anyMatch(constraint -> "g".equals(constraint.getProj())));
        assertTrue(graph.graphElements.stream().anyMatch(component -> "1".equals(component.data.get("id")) && "cnode".equals(component.data.get("node_type"))));
        assertTrue(graph.graphElements.stream().anyMatch(component -> "2".equals(component.data.get("id")) && "gnode".equals(component.data.get("node_type"))));
    }

    @Test
    void nodeTypeConstraintsDoNotCreateSyntheticTypeIdNodes() {
        LinguisticStructure structure = new LinguisticStructure();
        structure.constraints = new ArrayList<>();
        structure.annotation = new ArrayList<>();
        structure.cp = new ChoiceSpace();
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), 10, "NODE_TYPE", "c", "c"));
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), 10, "PRED", "dog", "c"));

        LigerWebGraph graph = new LigerWebGraph(structure);

        assertTrue(graph.graphElements.stream().anyMatch(component -> "10".equals(component.data.get("id")) && "cnode".equals(component.data.get("node_type"))));
        assertTrue(graph.graphElements.stream().noneMatch(component -> "cnode".equals(component.data.get("id")) || "input".equals(component.data.get("id")) || "gnode".equals(component.data.get("id"))));
    }

    @Test
    void nodeTypeConstraintsPopulateNodeDataInsteadOfAvp() {
        LinguisticStructure structure = new LinguisticStructure();
        structure.constraints = new ArrayList<>();
        structure.annotation = new ArrayList<>();
        structure.cp = new ChoiceSpace();
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), 11, "NODE_TYPE", "root"));
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), 11, "PRED", "dog"));

        LigerWebGraph graph = new LigerWebGraph(structure);

        assertTrue(graph.graphElements.stream().anyMatch(component -> "11".equals(component.data.get("id")) && "root".equals(component.data.get("node_type"))));
        assertTrue(graph.graphElements.stream().anyMatch(component -> "11".equals(component.data.get("id"))
                && component.data.containsKey("avp")
                && !((java.util.Map<?, ?>) component.data.get("avp")).containsKey("node_type")));
    }

    @Test
    void canonicalNodeAvpGraphParsesWithoutTurningMetadataIntoEdges() {
        LinkedHashMap<String, Object> graph = new LinkedHashMap<>();
        graph.put("id", "semantic");
        graph.put("text", "");
        graph.put("nodes", List.of(
                new LinkedHashMap<>(Map.of(
                        "id", "d1",
                        "node_type", "state",
                        "label", "s1",
                        "avp", Map.of("NAME", "s1", "NODE_TYPE", "state"))),
                new LinkedHashMap<>(Map.of(
                        "id", "d2",
                        "node_type", "condition",
                        "label", "dog",
                        "avp", Map.of("NAME", "dog", "NODE_TYPE", "condition")))));
        graph.put("edges", List.of(new LinkedHashMap<>(Map.of(
                "source", "d1", "target", "d2", "label", "COND"))));
        graph.put("annotations", List.of());
        graph.put("choiceSpace", new LinkedHashMap<>());

        LinguisticStructure parsed = LinguisticStructure.parseFromJson(graph);
        LigerWebGraph rendered = new LigerWebGraph(parsed);

        assertTrue(rendered.graphElements.stream().anyMatch(component ->
                "d1".equals(component.data.get("id"))
                        && "s1".equals(((Map<?, ?>) component.data.get("avp")).get("NAME"))));
        assertTrue(rendered.graphElements.stream()
                .filter(component -> component.data.containsKey("node_type"))
                .noneMatch(component -> "NAME".equals(component.data.get("label"))
                        || "NODE_TYPE".equals(component.data.get("label"))));
        assertTrue(rendered.graphElements.stream().anyMatch(component ->
                "COND".equals(component.data.get("label"))));
        assertEquals(1, rendered.graphElements.stream()
                .filter(component -> "edge".equals(component.data.get("edge_type")))
                .count());
    }

    @Test
    void canonicalConditionTermsAreNotRewrittenAsPredicateEdges() {
        LinkedHashMap<String, Object> graph = new LinkedHashMap<>();
        graph.put("id", "semantic");
        graph.put("text", "");
        graph.put("nodes", List.of(
                new LinkedHashMap<>(Map.of(
                        "id", "d1", "node_type", "state", "label", "s1",
                        "avp", Map.of("NAME", "s1", "NODE_TYPE", "state"))),
                new LinkedHashMap<>(Map.of(
                        "id", "d2", "node_type", "referent", "label", "x1",
                        "avp", Map.of("NAME", "x1", "NODE_TYPE", "referent"))),
                new LinkedHashMap<>(Map.of(
                        "id", "d3", "node_type", "condition", "label", "dog",
                        "avp", Map.of("NAME", "dog", "NODE_TYPE", "condition")))));
        graph.put("edges", List.of(
                new LinkedHashMap<>(Map.of("source", "d1", "target", "d3", "label", "COND")),
                new LinkedHashMap<>(Map.of("source", "d3", "target", "d2", "label", "TERM1"))));
        graph.put("annotations", List.of());
        graph.put("choiceSpace", new LinkedHashMap<>());

        LinguisticStructure parsed = LinguisticStructure.parseFromJson(graph);

        assertTrue(parsed.constraints.stream().anyMatch(constraint ->
                "d3".equals(constraint.getFsNode()) && "TERM1".equals(constraint.getRelationLabel())));
        assertTrue(parsed.constraints.stream().noneMatch(constraint ->
                "dog".equals(constraint.getRelationLabel())));
    }

    @Test
    void annotationIdsUseAnnotationNodeType() {
        LinguisticStructure structure = new LinguisticStructure();
        structure.constraints = new ArrayList<>();
        structure.annotation = new ArrayList<>();
        structure.cp = new ChoiceSpace();
        structure.annotation.add(new GraphConstraint(new LinkedHashSet<>(), "f1", "PRED", "dog", "f", false));
        structure.annotation.add(new GraphConstraint(new LinkedHashSet<>(), "f1", "REL", "a1", "f", false));
        structure.annotation.add(new GraphConstraint(new LinkedHashSet<>(), "a1", "LABEL", "new", "a", false));

        LigerWebGraph graph = new LigerWebGraph(structure);

        assertTrue(graph.graphElements.stream().anyMatch(component ->
                "a1".equals(component.data.get("id"))
                        && "annotation".equals(component.data.get("node_type"))));
    }

    @Test
    void parallelEdgesBetweenSameNodesArePreserved() {
        LinguisticStructure structure = new LinguisticStructure();
        structure.constraints = new ArrayList<>();
        structure.annotation = new ArrayList<>();
        structure.cp = new ChoiceSpace();
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "1", "POTENTIAL-ANT", "2", "c", false));
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "1", "POSSIBLE-ANT", "2", "c", false));

        LigerWebGraph graph = new LigerWebGraph(structure);

        long edgeCount = graph.graphElements.stream()
                .filter(component -> "edge".equals(component.data.get("edge_type")))
                .count();

        assertEquals(2, edgeCount);
        assertTrue(graph.graphElements.stream().anyMatch(component -> "POTENTIAL-ANT".equals(component.data.get("label"))));
        assertTrue(graph.graphElements.stream().anyMatch(component -> "POSSIBLE-ANT".equals(component.data.get("label"))));
    }

}
