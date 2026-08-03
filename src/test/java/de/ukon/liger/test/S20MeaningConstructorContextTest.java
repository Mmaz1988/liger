package de.ukon.liger.test;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class S20MeaningConstructorContextTest {
    @Test
    void packedMeaningConstructorsRetainChoiceContexts() {
        PathVariables.initializePathVariables();
        XLEoperator operator = new XLEoperator(new VariableHandler());
        GlueSemantics semantics = new GlueSemantics();

        LinguisticStructure packed = load(operator, "testdirS20.pl");
        Map<Set<ChoiceVar>, Set<String>> packedConstructors =
                semantics.translateMeaningConstructors(packed, true);

        assertFalse(packedConstructors.isEmpty());
        assertEquals(2, packedConstructors.keySet().stream()
                .filter(context -> context.stream().anyMatch(choice -> "A1".equals(choice.choiceID)
                        || "A2".equals(choice.choiceID)))
                .count());
    }

    private LinguisticStructure load(XLEoperator operator, String fixture) {
        LinkedHashMap<String, LinguisticStructure> loaded = operator.fs2Java(
                Path.of(PathVariables.testPath, fixture).toString());
        assertEquals(1, loaded.size(), fixture);
        return loaded.values().iterator().next();
    }
}
