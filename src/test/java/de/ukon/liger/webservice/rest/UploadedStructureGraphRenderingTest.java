package de.ukon.liger.webservice.rest;

import de.ukon.liger.webservice.rest.dtos.LigerMergeResponse;
import de.ukon.liger.webservice.rest.dtos.LigerRuleAnnotation;
import de.ukon.liger.webservice.rest.dtos.LigerRuleAnnotationResponse;
import de.ukon.liger.webservice.rest.dtos.LigerStructureMergeRequest;
import de.ukon.liger.webservice.rest.dtos.LigerStructureRuleRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The LigerWebGraph these two endpoints attach to every structure is a rendering
 * companion: roughly a third of each response, and on /apply_rules_uploaded_structure it
 * is paid once PER RULE BRANCH. The client's discourse/reasoning pipeline consumes only
 * `structureJson` -- it feeds the merged structure straight into the next request -- so it
 * was downloading and discarding the rendering on every branch (~22 MB for a single
 * 90-branch regression item).
 *
 * `includeGraph` lets such a caller opt out. It is opt-OUT rather than opt-in on purpose:
 * an absent flag still renders, so every caller written before the flag existed (the
 * analysis view, which does display these graphs, and the Python workflow trace) keeps the
 * exact response it always got. These tests pin both directions of that default.
 */
class UploadedStructureGraphRenderingTest {

    private static LinkedHashMap<String, Object> constraint(String from, String label, String to) {
        LinkedHashMap<String, Object> c = new LinkedHashMap<>();
        c.put("sourceNode", from);
        c.put("relationLabel", label);
        c.put("targetNode", to);
        c.put("choiceVars", new ArrayList<>());
        return c;
    }

    private static LinkedHashMap<String, Object> structure(String id, String from, String label, String to) {
        LinkedHashMap<String, Object> s = new LinkedHashMap<>();
        s.put("id", id);
        s.put("text", "test");
        s.put("constraints", new ArrayList<>(List.of(constraint(from, label, to))));
        s.put("annotations", new ArrayList<>());
        s.put("choiceSpace", new LinkedHashMap<>());
        return s;
    }

    private static LigerStructureMergeRequest mergeRequest(Boolean includeGraph) {
        LigerStructureMergeRequest request = new LigerStructureMergeRequest(
                structure("syn", "f1", "SUBJ", "f2"),
                structure("drs", "d1", "DRS-COND", "d2"));
        request.includeGraph = includeGraph;
        return request;
    }

    @Test
    void mergeRendersAGraphWhenTheFlagIsAbsent() {
        LigerMergeResponse response = new LigerController().mergeUploadedStructures(mergeRequest(null));

        assertNotNull(response.structureJson, "the merged structure is the point of the call");
        assertNotNull(response.graph, "an absent flag must keep rendering, for callers that predate it");
    }

    @Test
    void mergeRendersAGraphWhenExplicitlyRequested() {
        assertNotNull(new LigerController().mergeUploadedStructures(mergeRequest(true)).graph);
    }

    @Test
    void mergeSkipsTheGraphWhenTheCallerOptsOut() {
        LigerMergeResponse response = new LigerController().mergeUploadedStructures(mergeRequest(false));

        assertNull(response.graph, "opting out must skip the rendering entirely");
        assertNotNull(response.structureJson, "opting out must not cost the caller the structure");
    }

    /** The merged structure a caller opts out of rendering must still be byte-identical to
     *  the one it would have got with rendering on -- otherwise the saving would come at
     *  the price of a different downstream result. */
    @Test
    void optingOutDoesNotChangeTheMergedStructure() {
        LigerMergeResponse rendered = new LigerController().mergeUploadedStructures(mergeRequest(true));
        LigerMergeResponse plain = new LigerController().mergeUploadedStructures(mergeRequest(false));

        assertEquals(rendered.structureJson.toString(), plain.structureJson.toString());
    }

    private static LigerRuleAnnotationResponse applyRules(Boolean includeGraph) throws Exception {
        LigerStructureRuleRequest request = new LigerStructureRuleRequest();
        request.content = new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(structure("syn", "f1", "SUBJ", "f2"));
        request.format = "json";
        request.id = "graph-rendering-test";
        request.ruleString = "";
        request.includeGraph = includeGraph;
        return new LigerController().applyRulesUploadedStructure(request);
    }

    @Test
    void ruleApplicationRendersEveryBranchWhenTheFlagIsAbsent() throws Exception {
        LigerRuleAnnotationResponse response = applyRules(null);

        assertNotNull(response.annotations);
        for (LigerRuleAnnotation annotation : response.annotations) {
            assertNotNull(annotation.graph, "an absent flag must keep rendering every branch");
            assertNotNull(annotation.structureJson);
        }
    }

    @Test
    void ruleApplicationSkipsEveryBranchGraphWhenTheCallerOptsOut() throws Exception {
        LigerRuleAnnotationResponse response = applyRules(false);

        assertNotNull(response.annotations);
        for (LigerRuleAnnotation annotation : response.annotations) {
            assertNull(annotation.graph, "opting out must skip the rendering on EVERY branch");
            assertNotNull(annotation.structureJson, "the branch structure is what the caller came for");
        }
    }
}
