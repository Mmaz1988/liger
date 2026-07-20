package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void testGraphAwareStarTraversesBeyondLegacyExpansionDepth() {
        LinguisticStructure fs = buildLongChainStructure();

        QueryParser parser = new QueryParser("#a ROOT 'yes' & #a !(LINK*) #b", fs);
        QueryParserResult result = parser.parseQuery(parser.getQueryList());

        assertEquals(7, result.result.size());
    }

    @Test
    void testGraphAwareTemplateStarTraversesOnlyExistingEdges() {
        LinguisticStructure fs = buildLongChainStructure(true);
        TemplateRegistry registry = new TemplateParser().parse("GF := LINK | ALT .");
        String query = "#a ROOT 'yes' & #a !(@GF*) #b";

        QueryParser parser = new QueryParser(query, fs, registry);
        QueryParserResult result = parser.parseQueryWithTemplates(query).get(0);

        assertEquals(7, result.result.size());
    }

    @Test
    void testGraphAwareInsideOutStarTraversesLongChain() {
        LinguisticStructure fs = buildLongChainStructure(true);
        TemplateRegistry registry = new TemplateParser().parse("GF := LINK | ALT .");
        String query = "#a LEAF 'yes' & #a ^(@GF*) #b";

        QueryParser parser = new QueryParser(query, fs, registry);
        QueryParserResult result = parser.parseQueryWithTemplates(query).get(0);

        assertEquals(7, result.result.size());
    }

    @Test
    void testQueryParser14() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS8.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g ^(SUBJ) #h", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser16() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("*0 TNS-ASP #f TENSE 'past'", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertTrue(qpr.isSuccess && qpr.result.size() == 1);
        }
    }

    @Test
    void testOffPathNegatedAttributeBlocksCurrentTargetNode() {
        LinguisticStructure fs = buildOffPathStructure();

        QueryParser qp = new QueryParser("#a !(OBJ:~(-> SUBJ)) #b", fs);
        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        assertEquals(1, qpr.result.size());
    }

    @Test
    void testOffPathAttributeValueMatchesCurrentTargetNode() {
        LinguisticStructure fs = buildOffPathStructure();

        QueryParser qp = new QueryParser("#a !(OBJ:(-> SUBJ +)) #b", fs);
        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        assertEquals(1, qpr.result.size());
    }

    private LinguisticStructure buildSingleNodeStructure() {
        Set<ChoiceVar> reading = new HashSet<>();
        reading.add(new ChoiceVar("1"));

        List<GraphConstraint> constraints = new ArrayList<>();
        constraints.add(new GraphConstraint(reading, 0, "PRON-TYPE", "'reflexive'"));

        return new LinguisticStructure("test", "test", constraints);
    }

    private LinguisticStructure buildOffPathStructure() {
        Set<ChoiceVar> reading = new HashSet<>();
        reading.add(new ChoiceVar("1"));

        List<GraphConstraint> constraints = new ArrayList<>();
        constraints.add(new GraphConstraint(reading, 0, "OBJ", "1"));
        constraints.add(new GraphConstraint(reading, 1, "SUBJ", "+"));
        constraints.add(new GraphConstraint(reading, 0, "OBJ", "2"));
        constraints.add(new GraphConstraint(reading, 2, "CASE", "'nom'"));

        return new LinguisticStructure("test", "test", constraints);
    }

    private LinguisticStructure buildLongChainStructure() {
        return buildLongChainStructure(false);
    }

    private LinguisticStructure buildLongChainStructure(boolean alternatives) {
        Set<ChoiceVar> reading = new HashSet<>();
        reading.add(new ChoiceVar("1"));

        List<GraphConstraint> constraints = new ArrayList<>();
        constraints.add(new GraphConstraint(reading, 0, "ROOT", "'yes'"));
        for (int node = 0; node < 6; node++) {
            String relation = alternatives && node % 2 == 1 ? "ALT" : "LINK";
            constraints.add(new GraphConstraint(reading, node, relation, String.valueOf(node + 1)));
        }
        constraints.add(new GraphConstraint(reading, 6, "LEAF", "'yes'"));

        return new LinguisticStructure("long-chain", "long chain", constraints);
    }
}
