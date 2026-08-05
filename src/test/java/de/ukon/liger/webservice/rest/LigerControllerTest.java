package de.ukon.liger.webservice.rest;

import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.xle.Fstructure;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import de.ukon.liger.webservice.rest.dtos.LigerStructureQueryResponse;
import de.ukon.liger.webservice.rest.dtos.LigerRuleAnnotation;
import de.ukon.liger.webservice.rest.dtos.LigerRuleAnnotationResponse;
import de.ukon.liger.webservice.rest.dtos.LigerStructureRuleRequest;
import de.ukon.liger.webservice.rest.dtos.LigerSolutionAnnotationResponse;
import de.ukon.liger.webservice.rest.dtos.LigerStructureQueryRequest;
import de.ukon.liger.webservice.rest.dtos.LigerStructureUploadRequest;
import de.ukon.liger.webservice.rest.dtos.LigerGraphComponent;
import de.ukon.liger.webservice.rest.dtos.LigerWebGraph;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LigerControllerTest {

    @Test
    void parseUploadedStructureUsesTheStandardGraphRenderer() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testdirS15.pl");
        String prologContent = Files.readString(prologPath);

        LigerController controller = new LigerController();
        LigerRuleAnnotation response = controller.parseUploadedStructure(
                new LigerStructureUploadRequest(prologContent, "prolog", "uploaded"));

        VariableHandler vh = new VariableHandler();
        XLEoperator xle = new XLEoperator(vh);
        LinkedHashMap<String, LinguisticStructure> parsed = xle.fsString2Java(prologContent, "uploaded");
        LinguisticStructure expectedFs = parsed.values().stream().findFirst().orElseThrow();
        LigerWebGraph expectedGraph = new LigerWebGraph(expectedFs.constraints, expectedFs.annotation);

        assertEquals(normalizeGraph(expectedGraph.graphElements), normalizeGraph(response.graph.graphElements));
    }

    @Test
    void parseUploadedStructureRejectsGraphOnlyJson() {
        LigerController controller = new LigerController();

        ResponseStatusException ex = org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class, () ->
                controller.parseUploadedStructure(new LigerStructureUploadRequest("{\"graphElements\":[]}", "json", "uploaded")));

        assertTrue(ex.getReason() != null && ex.getReason().contains("Graph-only JSON"));
    }

    @Test
    void queryUploadedStructureAcceptsEmbeddedTemplatesAndHierarchies() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testdirS17.pl");
        String prologContent = Files.readString(prologPath);
        String query = "GF ::= SUBJ > OBJ > OBL .\n"
                + "feature-label() := TENSE | PERF .\n"
                + "#a SUBJ #b & #a OBL #d & superior(GF,#b,#d)";

        LigerController controller = new LigerController();
        LigerStructureQueryResponse response = controller.queryUploadedStructure(
                new LigerStructureQueryRequest(prologContent, "prolog", "uploaded", query));

        assertEquals("true", response.success);
        assertTrue(response.matchCount > 0);
        assertTrue(response.graph.graphElements.stream()
                .anyMatch(component -> "query-match".equals(String.valueOf(component.data.get("query_selector")))
                        && !component.data.containsKey("source")
                        && !component.data.containsKey("target")));
        assertTrue(response.graph.graphElements.stream()
                .noneMatch(component -> "query-match".equals(String.valueOf(component.data.get("query_selector")))
                        && component.data.containsKey("source")
                        && component.data.containsKey("target")));
    }

    @Test
    void queryUploadedStructureAcceptsEmbeddedTemplatesAndHierarchiesForS19() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testDirS19.pl");
        String prologContent = Files.readString(prologPath);
        String query = "GF ::= SUBJ > OBJ > OBL > COMP .\n"
                + "GF := SUBJ | OBJ | OBL | COMP .\n"
                + "#a ^(@GF+) #b ^(@GF) #c & #c !(@GF) #d & superior(GF,#d,#b)";

        LigerController controller = new LigerController();
        LigerStructureQueryResponse response = controller.queryUploadedStructure(
                new LigerStructureQueryRequest(prologContent, "prolog", "uploaded", query));

        assertEquals("true", response.success);
        assertEquals(6, response.matchCount);
        assertTrue(response.graph.graphElements.stream()
                .anyMatch(component -> "query-match".equals(String.valueOf(component.data.get("query_selector")))));
    }

    @Test
    void queryUploadedStructureIgnoresStandaloneCommentLinesAroundEmbeddedDeclarations() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testdirS17.pl");
        String prologContent = Files.readString(prologPath);
        String query = "// hierarchies here\n"
                + "GF ::= SUBJ > OBJ > OBL .\n"
                + "\n"
                + "// templates here\n"
                + "REFL-BIND(#f,#h) := #f ^(@GF*:~(->SUBJ)) #i ^(@GF) #j !(@GF) #h.\n"
                + "\n"
                + "#a SUBJ #b";

        LigerController controller = new LigerController();
        LigerStructureQueryResponse response = controller.queryUploadedStructure(
                new LigerStructureQueryRequest(prologContent, "prolog", "uploaded", query));

        assertTrue(response != null);
        assertTrue(response.graph != null);
    }

    @Test
    void queryUploadedStructureExpandsTemplatesWithEmbeddedSuperiorConstraints() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testdirS17.pl");
        String prologContent = Files.readString(prologPath);
        String query = "// hierarchies here\n"
                + "GF ::= SUBJ > OBJ > OBL .\n"
                + "\n"
                + "// templates here\n"
                + "REFL-BIND(#f,#h) := #f ^(@GF*:~(->SUBJ)) #i ^(@GF) #j !(@GF) #h & superior(GF,#h,#j).\n"
                + "\n"
                + "#a ant #a & #a SYNSEM #b & @REFL-BIND(#b,#c)";

        LigerController controller = new LigerController();
        LigerStructureQueryResponse response = controller.queryUploadedStructure(
                new LigerStructureQueryRequest(prologContent, "prolog", "uploaded", query));

        assertTrue(response != null);
        assertTrue(response.matchCount >= 0);
    }

    @Test
    void queryUploadedStructureAcceptsMultilineEmbeddedTemplateDefinitions() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testdirS17.pl");
        String prologContent = Files.readString(prologPath);
        String query = "// hierarchies here\n"
                + "GF ::= SUBJ > OBJ > OBL .\n"
                + "\n"
                + "// templates here\n"
                + "MCN-PATH(#a,#b) := #a ^(@GF*:~(->SUBJ)) #b .\n"
                + "REFL-BIND(#f,#h) := #f PRON-TYPE 'refl' & @MCN-PATH(#f,#i) &\n"
                + "                    #i ^(@GF) #j !(@GF) #h & superior(GF,#h,#i) .\n"
                + "\n"
                + "#a ant #a & #a SYNSEM #b & @REFL-BIND(#b,#c)";

        LigerController controller = new LigerController();
        LigerStructureQueryResponse response = controller.queryUploadedStructure(
                new LigerStructureQueryRequest(prologContent, "prolog", "uploaded", query));

        assertTrue(response != null);
        assertTrue(response.graph != null);
    }

    @Test
    void queryUploadedStructureFiltersTheBadMergedGraph4Binding() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(System.getProperty("user.dir"), "merged-graph4.json");
        String json = Files.readString(prologPath);
        String query = "GF ::= SUBJ > OBJ > OBJ2 > OBL .\n"
                + "GF := SUBJ | OBJ | OBL .\n"
                + "DR-GF-LINK(#a, #d) := #a SRC %a & #b SYN-ID %b & %a == %b & #b ^(in_set>GLUE>g::>cproj) #c phi #d .\n"
                + "MCN-PATH(#a,#b,#c) := #a ^(@GF*:~(->SUBJ)) #b & #b ^(@GF) #c .\n"
                + "REFL-BIND(#f,#h) := #f PRON-TYPE 'refl' & @MCN-PATH(#f,#i,#j) & #j !(@GF) #h & superior(GF,#h,#i) .\n"
                + "COARG-PATH(#a,#b,#c) := #a ^(@GF*:~(->PRED)) #b ^(@GF) #c .\n"
                + "COARG(#a,#b) := @COARG-PATH(#a,#r,#s) & #s !(@GF) #b & id(#a) != id(#b) .\n"
                + "DR-PRECEDENCE(#a,#b) := #a NAME %a & #a NODE_TYPE referent &\n"
                + "                        #b NAME %b & #b NODE_TYPE referent &\n"
                + "\tid(%a) < id(%b) .\n"
                + "#a ant #a & #a SYNSEM #b & #c SYNSEM #d & @DR-PRECEDENCE(#c,#a) & -(@COARG(#b,#d))";

        LigerController controller = new LigerController();
        LigerStructureQueryResponse response = controller.queryUploadedStructure(
                new LigerStructureQueryRequest(json, "json", "merged-graph4", query));

        assertEquals("true", response.success);
        assertEquals(4, response.matchCount);
        assertTrue(response.solutions.stream().noneMatch(solution ->
                solution.signature.contains("a=007")
                        && solution.signature.contains("b=2")
                        && solution.signature.contains("c=006")
                        && solution.signature.contains("d=4")));
    }

    @Test
    void applyRuleRequestXLE2HandlesEmptyRuleString() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testdirS17.pl");
        String prologContent = Files.readString(prologPath);

        LigerController controller = new LigerController();
        LigerSolutionAnnotationResponse response = controller.applyRuleRequestXLE2(
                new de.ukon.liger.webservice.rest.dtos.LigerRequest(prologContent, "", true, null));

        assertTrue(response != null);
    }

    @Test
    void applyRulesToLingStructureHandlesEmptyRuleString() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testdirS17.pl");
        String prologContent = Files.readString(prologPath);

        VariableHandler vh = new VariableHandler();
        XLEoperator xle = new XLEoperator(vh);
        LinkedHashMap<String, LinguisticStructure> parsed = xle.fsString2Java(prologContent, "uploaded");
        LinguisticStructure expectedFs = parsed.values().stream().findFirst().orElseThrow();
        String json = new ObjectMapper().writeValueAsString(expectedFs.toJson());

        LigerController controller = new LigerController();
        LigerRuleAnnotation response = controller.applyRulesToLingStructure(
                new de.ukon.liger.webservice.rest.dtos.LigerRequest(json, "", true, null));

        assertTrue(response != null);
    }

    @Test
    void applyRulesUploadedStructureReturnsAnnotatedGraphAndStructureJsonWithoutSemantics() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testdirS17.pl");
        String prologContent = Files.readString(prologPath);

        LigerController controller = new LigerController();
        LigerRuleAnnotationResponse response = controller.applyRulesUploadedStructure(
                new LigerStructureRuleRequest(prologContent, "prolog", "uploaded", ""));

        assertTrue(response != null);
        assertTrue(response.annotations != null);
        assertFalse(response.annotations.isEmpty());
        assertTrue(response.annotations.get(0).graph != null);
        assertTrue(response.annotations.get(0).structureJson != null);
        assertTrue(response.annotations.get(0).meaningConstructors == null || response.annotations.get(0).meaningConstructors.isBlank());
        assertTrue(response.annotations.get(0).axioms == null || response.annotations.get(0).axioms.isEmpty());
    }

    @Test
    void applyRulesUploadedStructureKeepsFactsAndHighlightsWithEachBranch() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(PathVariables.testPath, "testdirS2.pl");
        String prologContent = Files.readString(prologPath);
        LinguisticStructure structure = new XLEoperator(new VariableHandler())
                .fsString2Java(prologContent, "uploaded")
                .values()
                .stream()
                .findFirst()
                .orElseThrow();
        String json = new ObjectMapper().writeValueAsString(structure.toJson());

        LigerController controller = new LigerController();
        LigerRuleAnnotationResponse response = controller.applyRulesUploadedStructure(
                new LigerStructureRuleRequest(
                        json,
                        "json",
                        "uploaded",
                        "#g !(COMP*>TNS-ASP) #h ?=> #g TMP-DOM #h.\n"));

        assertEquals(6, response.annotations.size());
        for (LigerRuleAnnotation annotation : response.annotations) {
            assertTrue(annotation.graph != null);
            assertTrue(annotation.structureJson != null);
            assertTrue(annotation.addedAnnotationsByRule.containsKey(0));
            assertTrue(annotation.highlightedNodeIdsByRule.containsKey(0));
            assertFalse(annotation.addedAnnotationsByRule.get(0).isEmpty());
            assertFalse(annotation.highlightedNodeIdsByRule.get(0).isEmpty());

            assertTrue(annotation.structureJson.get("annotations").toString().contains("TMP-DOM"));
            assertTrue(annotation.graph.graphElements.stream().anyMatch(component ->
                    component.data != null && "TMP-DOM".equals(component.data.get("label"))));
        }
    }

    @Test
    void translateMeaningConstructorsAddsSyntheticIndicesInSyntacticOrder() throws Exception {
        PathVariables.initializePathVariables();

        Path prologPath = Paths.get(System.getProperty("user.dir"), "fs2.pl");
        String prologContent = Files.readString(prologPath);

        VariableHandler vh = new VariableHandler();
        XLEoperator xle = new XLEoperator(vh);
        LinkedHashMap<String, LinguisticStructure> parsed = xle.fsString2Java(prologContent, "uploaded");
        Fstructure expectedFs = (Fstructure) parsed.values().stream().findFirst().orElseThrow();

        new GlueSemantics().translateMeaningConstructors(expectedFs);

        Map<String, Integer> syntheticIndices = expectedFs.constraints.stream()
                .filter(constraint -> "SYN-ID".equals(constraint.getRelationLabel()))
                .collect(Collectors.toMap(
                        GraphConstraint::getFsNode,
                        constraint -> Integer.parseInt(String.valueOf(constraint.getFsValue()).replaceFirst("^i", ""))
                ));

        assertEquals(5, syntheticIndices.size());
        assertTrue(syntheticIndices.get("g10") < syntheticIndices.get("g25"));
        assertTrue(syntheticIndices.get("g13") < syntheticIndices.get("g25"));
        assertTrue(syntheticIndices.get("g25") < syntheticIndices.get("g33"));
    }

    @Test
    void applyRuleRequestXLE2ExposesSyntheticIndexOnGraphNodes() throws Exception {
        PathVariables.initializePathVariables();

        LigerController controller = new LigerController();
        LigerSolutionAnnotationResponse response = controller.applyRuleRequestXLE2(
                new de.ukon.liger.webservice.rest.dtos.LigerRequest("Kim told a man about himself", "", true, null));

        assertFalse(response.solutions.get(0).meaningConstructors.contains("_null"));
        assertTrue(response.solutions.get(0).graph.graphElements.stream()
                .filter(component -> component.data.containsKey("node_type"))
                .anyMatch(component -> "gnode".equals(component.data.get("node_type"))));
        assertTrue(response.solutions.get(0).graph.graphElements.stream()
                .filter(component -> component.data.containsKey("node_type"))
                .noneMatch(component -> {
                    Object avp = component.data.get("avp");
                    return String.valueOf(component.data.get("id")).matches("f\\d+")
                            && avp instanceof Map<?, ?> map
                            && (map.containsKey("TYPE") || map.containsKey("MEANING"));
                }));

        assertTrue(response.solutions != null && !response.solutions.isEmpty());
        assertTrue(response.solutions.get(0).graph.graphElements.stream()
                .filter(component -> component.data.containsKey("avp"))
                .anyMatch(component -> {
                    Object avp = component.data.get("avp");
                    return avp instanceof Map<?, ?> map && map.containsKey("SYN-ID");
                }));
    }

    private List<String> normalizeGraph(List<LigerGraphComponent> graphElements) {
        return graphElements.stream()
                .map(this::normalizeComponent)
                .sorted()
                .collect(Collectors.toList());
    }

    private String normalizeComponent(LigerGraphComponent component) {
        TreeMap<String, String> normalized = new TreeMap<>();
        for (Map.Entry<String, Object> entry : component.data.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> map) {
                TreeMap<String, String> inner = new TreeMap<>();
                for (Map.Entry<?, ?> innerEntry : map.entrySet()) {
                    inner.put(String.valueOf(innerEntry.getKey()), String.valueOf(innerEntry.getValue()));
                }
                normalized.put(entry.getKey(), inner.toString());
            } else {
                normalized.put(entry.getKey(), String.valueOf(value));
            }
        }
        return normalized.toString();
    }
}
