package de.ukon.liger.semantics;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.VariableHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsituSupportTest {

    @Test
    void parseMeaningConstructorTranslatesInsituFlag() {
        GlueSemanticsParser parser = new GlueSemanticsParser(new VariableHandler());

        String result = parser.parseMeaningConstructor(
                "test : ((%n_e -o %n_t) -o ((%n_e -o %s_t) -o %s_t)) || insitu");

        assertTrue(result.contains("@(INSITU "));
    }

    @Test
    void parseMeaningConstructorRejectsNoscopeAndInsituTogether() {
        GlueSemanticsParser parser = new GlueSemanticsParser(new VariableHandler());

        assertThrows(IllegalArgumentException.class, () ->
                parser.parseMeaningConstructor(
                        "test : ((%n_e -o %n_t) -o ((%n_e -o %s_t) -o %s_t)) || noscope, insitu"));
    }

    @Test
    void parseMCfromPrologIncludesInsituFlag() {
        GlueSemantics semantics = new GlueSemantics();
        Set<ChoiceVar> reading = Collections.singleton(new ChoiceVar());
        List<GraphConstraint> constraints = new ArrayList<>();

        constraints.add(new GraphConstraint(reading, 1, "MEANING", "'test'"));
        constraints.add(new GraphConstraint(reading, 1, "ANT", "2"));
        constraints.add(new GraphConstraint(reading, 1, "CONS", "3"));
        constraints.add(new GraphConstraint(reading, 1, "INSITU", "+"));
        constraints.add(new GraphConstraint(reading, 2, "RESOURCE", "'n_e'"));
        constraints.add(new GraphConstraint(reading, 2, "TYPE", "'t'"));
        constraints.add(new GraphConstraint(reading, 3, "RESOURCE", "'n_t'"));
        constraints.add(new GraphConstraint(reading, 3, "TYPE", "'t'"));

        String result = semantics.parseMCfromProlog("1", constraints);

        assertTrue(result.contains("|| insitu"));
    }

    @Test
    void parseMCfromPrologPreservesInsituForAtomicResource() {
        GlueSemantics semantics = new GlueSemantics();
        Set<ChoiceVar> reading = Collections.singleton(new ChoiceVar());
        List<GraphConstraint> constraints = new ArrayList<>();

        constraints.add(new GraphConstraint(reading, 1, "RESOURCE", "'n_e'"));
        constraints.add(new GraphConstraint(reading, 1, "TYPE", "'t'"));
        constraints.add(new GraphConstraint(reading, 1, "INSITU", "+"));

        String result = semantics.parseMCfromProlog("1", constraints);

        assertTrue(result.contains("|| insitu"));
    }
}
