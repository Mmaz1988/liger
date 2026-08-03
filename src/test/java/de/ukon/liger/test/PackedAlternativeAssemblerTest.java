package de.ukon.liger.test;

import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import de.ukon.liger.webservice.rest.PackedAlternativeAssembler;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackedAlternativeAssemblerTest {
    @Test
    void packsS20UnpackedReadingsWithSemanticChoiceContexts() {
        PathVariables.initializePathVariables();
        XLEoperator operator = new XLEoperator(new VariableHandler());
        LinguisticStructure packed = PackedAlternativeAssembler.pack(List.of(
                load(operator, "testdirS20-1.pl"),
                load(operator, "testdirS20-2.pl")));

        assertEquals(1, packed.cp.choiceNodes.size());
        assertTrue(packed.cp.choices.stream().anyMatch(context ->
                context.stream().anyMatch(choice -> "M1".equals(choice.choiceID))));
        assertTrue(packed.cp.choices.stream().anyMatch(context ->
                context.stream().anyMatch(choice -> "M2".equals(choice.choiceID))));
        assertTrue(packed.constraints.stream().anyMatch(constraint ->
                constraint.getReading().stream().anyMatch(choice -> "M1".equals(choice.choiceID))));
        assertTrue(packed.constraints.stream().anyMatch(constraint ->
                constraint.getReading().stream().anyMatch(choice -> "M2".equals(choice.choiceID))));
    }

    private LinguisticStructure load(XLEoperator operator, String fixture) {
        return operator.fs2Java(Path.of(PathVariables.testPath, fixture).toString())
                .values().iterator().next();
    }
}
