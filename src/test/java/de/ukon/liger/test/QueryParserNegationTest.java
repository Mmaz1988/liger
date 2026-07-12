package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryParserNegationTest {

    @Test
    void testNegationBlocksCompatibleEmbedding() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TNS-ASP #h & -(#h PERF '-_')", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(0, qpr.result.size());
        }
    }

    @Test
    void testLeadingNegationSucceedsWithoutMatches() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("-(#g MOOD 'subjunctive')", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertTrue(qpr.isSuccess);
            assertEquals(1, qpr.result.size());
            assertTrue(qpr.result.keySet().stream().findAny().get().isEmpty());
        }
    }
}
