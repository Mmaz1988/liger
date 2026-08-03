package de.ukon.liger.test;

import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PackedFixtureAuditTest {
    @Test
    void packedFixturesRetainChoiceContexts() {
        PathVariables.initializePathVariables();
        XLEoperator operator = new XLEoperator(new VariableHandler());

        assertFixture(operator, "testdirS11.pl", 478, 1, 3);
        assertFixture(operator, "testdirS12.pl", 4960, 475, 403);
        assertFixture(operator, "testdirS13.pl", 2151, 29, 37);
        assertFixture(operator, "testdirS20.pl", 336, 1, 3);
    }

    private void assertFixture(XLEoperator operator, String fixture, int constraints,
                               int choiceNodes, int choices) {
        Path path = Path.of(PathVariables.testPath, fixture);
        LinkedHashMap<String, LinguisticStructure> structures = operator.fs2Java(path.toString());

        assertEquals(1, structures.size(), fixture);
        LinguisticStructure structure = structures.values().iterator().next();
        assertNotNull(structure.cp, fixture);
        assertEquals(constraints, structure.constraints.size(), fixture);
        assertEquals(choiceNodes, structure.cp.choiceNodes.size(), fixture);
        assertEquals(choices, structure.cp.choices.size(), fixture);
    }
}
