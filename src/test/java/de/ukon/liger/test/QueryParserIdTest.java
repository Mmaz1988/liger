package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryParserIdTest {

    @Test
    void testIdExpressionWorksAsValue() {
        LinguisticStructure fs = structureWithValues("'1'");

        QueryParser qp = new QueryParser("#g TYPE 'ref' & #f NUMBER id(#g)", fs);
        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        assertTrue(qpr.isSuccess);
        assertEquals(1, qpr.result.keySet().size());
    }

    @Test
    void testIdExpressionWorksInNumericComparison() {
        LinguisticStructure fs = structureWithValues("'2'");

        QueryParser qp = new QueryParser("#g TYPE 'ref' & #f NUMBER '2' & id(#g) < id(#f)", fs);
        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        assertTrue(qpr.isSuccess);
        assertEquals(1, qpr.result.keySet().size());
    }

    @Test
    void testNumericComparisonWorksForRegularValues() {
        LinguisticStructure fs = structureWithNumericValues("'1'", "'2'");

        QueryParser qp = new QueryParser("#f NUMBER %a & #g NUMBER %b & %a < %b", fs);
        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        assertTrue(qpr.isSuccess);
        assertEquals(1, qpr.result.keySet().size());
    }

    @Test
    void testIdRejectsNonVariableArguments() {
        LinguisticStructure fs = structureWithValues("'1'");

        assertThrows(IllegalStateException.class, () -> new QueryParser("id(1)", fs));
        assertThrows(IllegalStateException.class, () -> new QueryParser("id(%x)", fs));
    }

    private LinguisticStructure structureWithValues(String numberValue) {
        return structureWithNumericValues(numberValue);
    }

    private LinguisticStructure structureWithNumericValues(String... numberValues) {
        Set<ChoiceVar> reading = new HashSet<>(Set.of(new ChoiceVar("1")));
        List<GraphConstraint> constraints = new java.util.ArrayList<>();
        constraints.add(new GraphConstraint(reading, 1, "TYPE", "'ref'"));

        for (int i = 0; i < numberValues.length; i++) {
            constraints.add(new GraphConstraint(reading, i + 2, "NUMBER", numberValues[i]));
        }

        LinguisticStructure fs = new LinguisticStructure("S0", "", constraints, new ChoiceSpace());
        fs.annotation = List.of();
        return fs;
    }
}
