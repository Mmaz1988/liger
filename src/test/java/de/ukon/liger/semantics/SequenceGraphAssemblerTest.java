package de.ukon.liger.semantics;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.packing.ChoiceNode;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerWebGraph;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SequenceGraphAssemblerTest {

    @Test
    void offsetsIdsAndLinksSentenceRoots() {
        LinguisticStructure first = sentence("first", "c1", "f1", "i1");
        LinguisticStructure second = sentence("second", "c1", "f1", "i1");

        LinguisticStructure sequence = SequenceGraphAssembler.assemble(List.of(first, second));

        List<GraphConstraint> all = sequence.constraints;
        assertTrue(all.stream().anyMatch(c -> "NEXT".equals(c.getRelationLabel())
                && "c1".equals(c.getFsNode())
                && "c2".equals(c.getFsValue())));
        assertTrue(all.stream().anyMatch(c -> "SYN-ID".equals(c.getRelationLabel())
                && "i1".equals(c.getFsValue())));
        assertTrue(all.stream().anyMatch(c -> "SYN-ID".equals(c.getRelationLabel())
                && "i2".equals(c.getFsValue())));

        LigerWebGraph webGraph = new LigerWebGraph(sequence.constraints, sequence.annotation);
        assertTrue(webGraph.graphElements.stream().anyMatch(element ->
                "NEXT".equals(element.data.get("label"))
                        && "c1".equals(element.data.get("source"))
                        && "c2".equals(element.data.get("target"))));
    }

    @Test
    void avoidsCollisionWhenSourceIdsStartAtZero() {
        LinguisticStructure first = sentence("first", "c0", "f0", "i1");
        LinguisticStructure second = sentence("second", "c0", "f0", "i1");

        LinguisticStructure sequence = SequenceGraphAssembler.assemble(List.of(first, second));

        assertTrue(sequence.constraints.stream().anyMatch(c -> "NEXT".equals(c.getRelationLabel())
                && "c0".equals(c.getFsNode())
                && "c1".equals(c.getFsValue())));
        assertTrue(sequence.constraints.stream().anyMatch(c -> "SYN-ID".equals(c.getRelationLabel())
                && "i2".equals(c.getFsValue())));
    }

    @Test
    void rebasesSemanticSourceIdsWithSyntaxIds() {
        LinguisticStructure first = sentence("first", "c1", "f1", "i1");
        LinguisticStructure second = sentence("second", "c1", "f1", "i1");
        first.constraints.add(new GraphConstraint(Collections.emptySet(), "d1", "SRC", "i1", "d", false));
        second.constraints.add(new GraphConstraint(Collections.emptySet(), "d1", "SRC", "i1", "d", false));

        LinguisticStructure sequence = SequenceGraphAssembler.assemble(List.of(first, second));

        assertTrue(sequence.constraints.stream().anyMatch(c -> "SRC".equals(c.getRelationLabel())
                && "i1".equals(c.getFsValue())));
        assertTrue(sequence.constraints.stream().anyMatch(c -> "SRC".equals(c.getRelationLabel())
                && "i2".equals(c.getFsValue())));
    }

    @Test
    void preservesNullConstraintValuesDuringRebasing() {
        LinguisticStructure first = sentence("first", "c1", "f1", "i1");
        first.constraints.add(new GraphConstraint(Collections.emptySet(), "g1", "GLUE", null, "g", false));

        LinguisticStructure sequence = SequenceGraphAssembler.assemble(List.of(first));

        assertTrue(sequence.constraints.stream().anyMatch(c -> "GLUE".equals(c.getRelationLabel())
                && c.getFsValue() == null));
    }

    @Test
    void rebasesCompoundIdsPackedChoicesAndRetainsProvenanceWithoutAliasing() {
        LinguisticStructure first = sentence("first", "drsState1", "semanticRef1", "i1");
        LinguisticStructure second = sentence("second", "drsState1", "semanticRef1", "i1");
        configurePackedChoice(first.cp, "A1");
        configurePackedChoice(second.cp, "A1");
        first.constraints.get(0).setReading(Set.of(new ChoiceVar("A1")));
        second.constraints.get(0).setReading(Set.of(new ChoiceVar("A1")));

        SequenceGraphAssembler.AssemblyResult assembled = SequenceGraphAssembler.assembleDetailed(List.of(
                new SequenceGraphAssembler.Part("S1", "syntax-1", "solution-1", "premise", first),
                new SequenceGraphAssembler.Part("S2", "syntax-2", "solution-2", "premise", second)));

        assertTrue(assembled.structure().constraints.stream().anyMatch(c -> "NEXT".equals(c.getRelationLabel())
                && "drsState1".equals(c.getFsNode()) && "drsState2".equals(c.getFsValue())));
        LigerWebGraph graph = new LigerWebGraph(assembled.structure().constraints, assembled.structure().annotation);
        assertTrue(graph.graphElements.stream().anyMatch(element -> "NEXT".equals(element.data.get("label"))
                && "drsState1".equals(element.data.get("source"))
                && "drsState2".equals(element.data.get("target"))));
        assertTrue(assembled.structure().constraints.stream().anyMatch(c -> c.getReading().stream()
                .anyMatch(choice -> "s1_A1".equals(choice.choiceID))));
        assertTrue(assembled.structure().constraints.stream().anyMatch(c -> c.getReading().stream()
                .anyMatch(choice -> "s2_A1".equals(choice.choiceID))));
        assertTrue(assembled.structure().cp.allVariables.containsAll(List.of("s1_A", "s2_A")));
        assertEquals(2, assembled.provenance().size());
        assertEquals("solution-2", assembled.provenance().get(1).solutionKey());
        assertEquals("drsState2", assembled.provenance().get(1).rootId());
        assertTrue(assembled.structure().annotation.stream().anyMatch(c -> "SENTENCE-ID".equals(c.getRelationLabel())
                && "S2".equals(c.getFsValue())));
        assertTrue(assembled.structure().annotation.stream().noneMatch(c ->
                "SOURCE-INDEX".equals(c.getRelationLabel()) || "NLI-SIDE".equals(c.getRelationLabel())));

        first.constraints.get(0).setFsValue("mutated");
        first.cp.allVariables.add("MUTATED");
        assertTrue(assembled.structure().constraints.stream().noneMatch(c -> "mutated".equals(c.getFsValue())));
        assertTrue(!assembled.structure().cp.allVariables.contains("MUTATED"));
    }

    private static void configurePackedChoice(ChoiceSpace cp, String choiceId) {
        ChoiceVar root = new ChoiceVar("1");
        ChoiceVar alternative = new ChoiceVar(choiceId);
        cp.rootChoice = new LinkedHashSet<>(Set.of(root));
        cp.choiceNodes = new ArrayList<>(List.of(new ChoiceNode(Set.of(root), Set.of(alternative))));
        cp.choices = new LinkedHashSet<>(Set.of(Set.of(alternative)));
        cp.allVariables = new ArrayList<>(List.of("A"));
    }

    private static LinguisticStructure sentence(String text,
                                                String root,
                                                String fsNode,
                                                String synId) {
        ChoiceVar choice = new ChoiceVar("1");
        LinguisticStructure structure = new LinguisticStructure(
                text,
                text,
                new ArrayList<>(List.of(
                        new GraphConstraint(Collections.singleton(choice), root, "CAT", "ROOT", "c", true),
                        new GraphConstraint(Collections.singleton(choice), fsNode, "SYN-ID", synId, "f", false)
                )),
                emptyChoiceSpace());
        return structure;
    }

    private static ChoiceSpace emptyChoiceSpace() {
        ChoiceSpace cp = new ChoiceSpace();
        cp.choiceNodes = new ArrayList<>();
        cp.choices = new HashSet<>();
        cp.allVariables = new ArrayList<>();
        cp.rootChoice = new HashSet<>();
        return cp;
    }
}
