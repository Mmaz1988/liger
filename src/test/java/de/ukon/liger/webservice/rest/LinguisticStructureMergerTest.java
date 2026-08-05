package de.ukon.liger.webservice.rest;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LinguisticStructureMergerTest {

    @Test
    void mergesConstraintsAndAnnotationsBeforeRendering() {
        LinguisticStructure syntax = new LinguisticStructure("s1", "syntax", new ArrayList<>(List.of(
                constraint("1", "NAME", "root")
        )));
        syntax.annotation = new ArrayList<>(List.of(
                constraint("1", "SYN-ID", "i1")
        ));
        syntax.cp = emptyChoiceSpace();

        LinguisticStructure drs = new LinguisticStructure("d1", "drs", new ArrayList<>(List.of(
                constraint("10", "NAME", "state")
        )));
        drs.annotation = new ArrayList<>(List.of(
                constraint("10", "SOURCE-ID", "1")
        ));
        drs.cp = emptyChoiceSpace();

        LinguisticStructure merged = LinguisticStructureMerger.merge(syntax, drs);

        assertNotNull(merged);
        assertEquals("s1", merged.local_id);
        assertEquals("syntax", merged.text);
        assertEquals(2, merged.constraints.size());
        assertEquals(2, merged.annotation.size());
        assertNotNull(merged.cp);
    }

    private static GraphConstraint constraint(String fsNode, String relationLabel, String fsValue) {
        return new GraphConstraint(Collections.singleton(new de.ukon.liger.packing.ChoiceVar("1")), fsNode, relationLabel, fsValue, "f", false);
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
