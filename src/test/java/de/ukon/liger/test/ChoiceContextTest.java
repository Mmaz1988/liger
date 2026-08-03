package de.ukon.liger.test;

import de.ukon.liger.packing.ChoiceContext;
import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.analysis.QueryParser.Solution;
import de.ukon.liger.analysis.QueryParser.SolutionKey;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChoiceContextTest {
    @Test
    void rootContextIsCompatibleWithBothAlternatives() {
        LinguisticStructure structure = new QueryParserTest().loadFs("testdirS20.pl")
                .values().iterator().next();
        ChoiceSpace choiceSpace = structure.cp;
        Set<ChoiceVar> root = Collections.singleton(new ChoiceVar("1"));
        Set<ChoiceVar> first = Collections.singleton(new ChoiceVar("A1"));
        Set<ChoiceVar> second = Collections.singleton(new ChoiceVar("A2"));

        assertTrue(ChoiceContext.compatible(choiceSpace, root, first));
        assertTrue(ChoiceContext.compatible(choiceSpace, root, second));
        assertFalse(ChoiceContext.compatible(choiceSpace, first, second));
    }

    @Test
    void solutionMergeKeepsAlternativesSeparateAndRejectsOnlyConjunctiveConflict() {
        LinguisticStructure structure = new QueryParserTest().loadFs("testdirS20.pl")
                .values().iterator().next();
        Set<ChoiceVar> first = Collections.singleton(new ChoiceVar("A1"));
        Set<ChoiceVar> second = Collections.singleton(new ChoiceVar("A2"));
        Solution left = new Solution(Collections.singleton(new SolutionKey("a", "f0")));
        Solution right = new Solution(Collections.singleton(new SolutionKey("b", "f1")));
        left.setChoiceContexts(Collections.singleton(first));
        right.setChoiceContexts(Collections.singleton(second));

        assertFalse(Solution.mergeIfCompatible(left, right, structure.cp) != null);
        right.setChoiceContexts(Collections.singleton(Collections.singleton(new ChoiceVar("1"))));
        assertTrue(Solution.mergeIfCompatible(left, right, structure.cp) != null);
    }
}
