package de.ukon.liger.webservice.rest;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerNliOverlayRequest;
import de.ukon.liger.webservice.rest.dtos.LigerNliRuleRequest;
import de.ukon.liger.webservice.rest.dtos.LigerNliStructureRequest;
import de.ukon.liger.webservice.rest.dtos.LigerNliStructureResponse;
import de.ukon.liger.webservice.rest.dtos.LigerRuleAnnotationResponse;
import de.ukon.liger.webservice.rest.dtos.LigerSequenceAssemblyResponse;
import de.ukon.liger.webservice.rest.dtos.LigerUploadedSequenceRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LigerNliCompositionTest {

    @Test
    void exposesOnlyTheMostRecentSentenceMeaningConstructorSet() {
        assertEquals("{\nsecond : b\n}", LigerController.latestMeaningConstructors(List.of(
                "{\nfirst : a\n}", "{\nsecond : b\n}")));
    }

    @Test
    void assemblesCombinesOverlaysAndAppliesRulesAtTheNliBoundary() {
        LigerController controller = new LigerController();
        LigerSequenceAssemblyResponse premise = controller.assembleUploadedSequence(
                sequenceRequest("Q0", "premise", "P1", structure("q", "c1"),
                        "P2", structure("q", "c1")));
        LigerSequenceAssemblyResponse conclusion = controller.assembleUploadedSequence(
                sequenceRequest("P0", "conclusion", "H1", structure("p", "c1")));

        assertEquals(List.of("P1", "P2"), premise.provenance.stream()
                .map(LigerSequenceAssemblyResponse.Provenance::sentenceId).toList());
        assertTrue(premise.structureJson.toString().contains("NEXT"));
        assertEquals("c2", premise.rebasedIds.get("1:c1"));

        LigerNliStructureRequest combine = new LigerNliStructureRequest();
        combine.id = "N17:assignment-4";
        combine.premise = premise.structureJson;
        combine.conclusion = conclusion.structureJson;
        LigerNliStructureResponse combined = controller.combineNliStructures(combine);

        assertNotNull(combined.premiseRoot);
        assertNotNull(combined.conclusionRoot);
        assertTrue(combined.directional);
        assertEquals("NLI_BOUNDARY", combined.boundaryRelation);
        assertEquals(1, countRelation(combined, "NLI_BOUNDARY"));

        LinguisticStructure semanticOverlay = LinguisticStructure.parseFromJson(combined.structureJson);
        semanticOverlay.constraints.add(new GraphConstraint(Set.of(new ChoiceVar("1")), "d1",
                "NLI_BOUNDARY", "d2", "d", false));
        semanticOverlay.annotation.add(new GraphConstraint(Set.of(new ChoiceVar("1")), combined.conclusionRoot,
                "SEMANTIC-MARKER", "kept", "d", false));
        LigerNliOverlayRequest overlay = new LigerNliOverlayRequest();
        overlay.id = combine.id;
        overlay.syntax = combined.structureJson;
        overlay.semantics = semanticOverlay.toJson();
        LigerNliStructureResponse enriched = controller.overlayNliStructures(overlay);
        assertEquals(1, countRelation(enriched, "NLI_BOUNDARY"));
        assertTrue(enriched.structureJson.toString().contains("SEMANTIC-MARKER"));

        LigerNliRuleRequest rules = new LigerNliRuleRequest();
        rules.id = combine.id;
        rules.structure = enriched.structureJson;
        rules.ruleString = "";
        LigerRuleAnnotationResponse ruled = controller.applyRulesNli(rules);
        assertEquals(1, ruled.annotations.size());
        assertEquals(1, ruled.annotations.get(0).structureJson.toString().split("NLI_BOUNDARY", -1).length - 1);
    }

    private static int countRelation(LigerNliStructureResponse response, String relation) {
        return (int) response.graph.graphElements.stream()
                .filter(element -> relation.equals(element.data.get("label"))).count();
    }

    private static LigerUploadedSequenceRequest sequenceRequest(String id, String side,
                                                                 Object... sentenceAndStructures) {
        LigerUploadedSequenceRequest request = new LigerUploadedSequenceRequest();
        request.id = id;
        request.side = side;
        for (int i = 0; i < sentenceAndStructures.length; i += 2) {
            LigerUploadedSequenceRequest.Part part = new LigerUploadedSequenceRequest.Part();
            part.sentenceId = String.valueOf(sentenceAndStructures[i]);
            part.syntaxVariantId = "syntax-" + part.sentenceId;
            part.solutionKey = "solution-" + part.sentenceId;
            part.structure = ((LinguisticStructure) sentenceAndStructures[i + 1]).toJson();
            request.structures.add(part);
        }
        return request;
    }

    private static LinguisticStructure structure(String id, String root) {
        ChoiceSpace cp = new ChoiceSpace();
        cp.rootChoice = new LinkedHashSet<>(Set.of(new ChoiceVar("1")));
        cp.choiceNodes = new ArrayList<>();
        cp.choices = new LinkedHashSet<>();
        cp.allVariables = new ArrayList<>();
        return new LinguisticStructure(id, id, new ArrayList<>(List.of(
                new GraphConstraint(Set.of(new ChoiceVar("1")), root, "CAT", "ROOT", "c", true)
        )), cp);
    }
}
