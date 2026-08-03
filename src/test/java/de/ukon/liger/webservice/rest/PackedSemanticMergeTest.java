package de.ukon.liger.webservice.rest;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import de.ukon.liger.webservice.rest.dtos.LigerMergeResponse;
import de.ukon.liger.webservice.rest.dtos.LigerStructureMergeRequest;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackedSemanticMergeTest {
    @Test
    void mergePreservesSyntacticAndSemanticChoiceContexts() {
        PathVariables.initializePathVariables();
        LinguisticStructure syntax = new XLEoperator(new VariableHandler())
                .fs2Java(Path.of(PathVariables.testPath, "testdirS20.pl").toString())
                .values().iterator().next();

        LinguisticStructure semantic = new LinguisticStructure();
        semantic.local_id = "semantic";
        semantic.constraints = new java.util.ArrayList<>();
        semantic.annotation = new java.util.ArrayList<>();
        semantic.cp = new ChoiceSpace();
        semantic.cp.choices.add(Collections.singleton(new ChoiceVar("M1")));
        GraphConstraint semanticFact = new GraphConstraint();
        semanticFact.setFsNode("m0");
        semanticFact.setRelationLabel("DRS");
        semanticFact.setFsValue("m1");
        semanticFact.setReading(Collections.singleton(new ChoiceVar("M1")));
        semantic.constraints.add(semanticFact);

        LinguisticStructure merged = LinguisticStructureMerger.merge(syntax, semantic);

        assertEquals(syntax.constraints.size() + semantic.constraints.size(), merged.constraints.size());
        assertTrue(merged.constraints.stream().anyMatch(constraint ->
                constraint.getReading().contains(new ChoiceVar("M1"))));
        assertTrue(merged.cp.choices.stream().anyMatch(context ->
                context.contains(new ChoiceVar("M1"))));
    }

    @Test
    void mergeEndpointPacksSemanticAlternativesBeforeMerging() {
        PathVariables.initializePathVariables();
        XLEoperator operator = new XLEoperator(new VariableHandler());
        LinguisticStructure syntax = operator.fs2Java(
                Path.of(PathVariables.testPath, "testdirS20.pl").toString()).values().iterator().next();
        LinguisticStructure first = operator.fs2Java(
                Path.of(PathVariables.testPath, "testdirS20-1.pl").toString()).values().iterator().next();
        LinguisticStructure second = operator.fs2Java(
                Path.of(PathVariables.testPath, "testdirS20-2.pl").toString()).values().iterator().next();

        LinkedHashMap<String, Object> semanticAlternatives = new LinkedHashMap<>();
        semanticAlternatives.put("alternatives", new ArrayList<>(List.of(first.toJson(), second.toJson())));
        LigerMergeResponse response = new LigerController().mergeUploadedStructures(
                new LigerStructureMergeRequest(syntax.toJson(), semanticAlternatives));

        String json = response.structureJson.toString();
        assertTrue(json.contains("M1"));
        assertTrue(json.contains("M2"));
    }
}
