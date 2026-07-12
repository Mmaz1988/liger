package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UncertaintyQuantifierTest {

    @Test
    void testStarAllowsZeroLengthAndPlusRequiresEdgesInsideOut() {
        LinguisticStructure fs = buildSingleNodeStructure();

        QueryParser starParser = new QueryParser("#a PRON-TYPE & #a ^(OBJ*) #b", fs);
        QueryParserResult starResult = starParser.parseQuery(starParser.getQueryList());

        QueryParser plusParser = new QueryParser("#a PRON-TYPE & #a ^(OBJ+) #b", fs);
        QueryParserResult plusResult = plusParser.parseQuery(plusParser.getQueryList());

        assertEquals(1, starResult.result.size());
        assertEquals(0, plusResult.result.size());
    }

    @Test
    void testStarAllowsZeroLengthAndPlusRequiresEdgesRegular() {
        LinguisticStructure fs = buildSingleNodeStructure();

        QueryParser starParser = new QueryParser("#a PRON-TYPE & #a !(OBJ*) #b", fs);
        QueryParserResult starResult = starParser.parseQuery(starParser.getQueryList());

        QueryParser plusParser = new QueryParser("#a PRON-TYPE & #a !(OBJ+) #b", fs);
        QueryParserResult plusResult = plusParser.parseQuery(plusParser.getQueryList());

        assertEquals(1, starResult.result.size());
        assertEquals(0, plusResult.result.size());
    }

    private LinguisticStructure buildSingleNodeStructure() {
        Set<ChoiceVar> reading = new HashSet<>();
        reading.add(new ChoiceVar("1"));

        List<GraphConstraint> constraints = new ArrayList<>();
        constraints.add(new GraphConstraint(reading, 0, "PRON-TYPE", "'reflexive'"));

        return new LinguisticStructure("test", "test", constraints);
    }
}
