package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class QueryFixtureBaselineTest {
    @Test
    void runCommonQueriesOnInitialFixtures() {
        QueryParserTest loader = new QueryParserTest();
        String[] fixtures = {"testdirS11.pl", "testdirS12.pl", "testdirS13.pl", "testdirS20.pl"};
        int[][] expectedResults = {
                {1, 7, 1, 1},
                {1, 85, 24, 24},
                {1, 38, 10, 10},
                {1, 5, 2, 2}
        };
        String[] queries = {
                "edge=PRED",
                "#a PRED %p",
                "#a ^(SUBJ) #b",
                "#a !(SUBJ) #b"
        };

        for (int fixtureIndex = 0; fixtureIndex < fixtures.length; fixtureIndex++) {
            String fixture = fixtures[fixtureIndex];
            LinkedHashMap<String, LinguisticStructure> structures = loader.loadFs(fixture);
            for (LinguisticStructure structure : structures.values()) {
                for (int queryIndex = 0; queryIndex < queries.length; queryIndex++) {
                    String query = queries[queryIndex];
                    long start = System.nanoTime();
                    QueryParser parser = new QueryParser(query, structure);
                    QueryParserResult result = parser.parseQuery(parser.getQueryList());
                    long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
                    System.out.printf("%s query=%s results=%d elapsedMs=%d%n",
                            fixture, query, result.result.size(), elapsedMs);
                    assertFalse(result.result.isEmpty(), fixture + " query=" + query);
                    assertEquals(expectedResults[fixtureIndex][queryIndex], result.result.size(),
                            fixture + " query=" + query);
                }
            }
        }
    }

    @Test
    void runLongTemplateQueriesOnInitialFixtures() {
        QueryParserTest loader = new QueryParserTest();
        TemplateRegistry templates = new TemplateParser().parse("GF := SUBJ | OBJ | OBL .");
        String[] fixtures = {"testdirS11.pl", "testdirS12.pl", "testdirS13.pl", "testdirS20.pl"};
        int[][] expectedResults = {
                {3, 154, 3, 3},
                {42, 1010, 64, 68},
                {22, 667, 22, 22},
                {2, 175, 2, 2}
        };
        String[] queries = {
                "#a ^(@GF) #b",
                "#a ^(@GF*) #b",
                "#a ^(@GF+) #b",
                "#a ^(@GF*) #b ^(@GF) #c"
        };

        for (int fixtureIndex = 0; fixtureIndex < fixtures.length; fixtureIndex++) {
            String fixture = fixtures[fixtureIndex];
            LinkedHashMap<String, LinguisticStructure> structures = loader.loadFs(fixture);
            for (LinguisticStructure structure : structures.values()) {
                for (int queryIndex = 0; queryIndex < queries.length; queryIndex++) {
                    String query = queries[queryIndex];
                    long start = System.nanoTime();
                    QueryParser parser = new QueryParser(query, structure, templates);
                    QueryParserResult result = parser.parseQuery(parser.getQueryList());
                    long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
                    System.out.printf("%s query=%s results=%d elapsedMs=%d%n",
                            fixture, query, result.result.size(), elapsedMs);
                    assertFalse(result.result.isEmpty(), fixture + " query=" + query);
                    assertEquals(expectedResults[fixtureIndex][queryIndex], result.result.size(),
                            fixture + " query=" + query);
                }
            }
        }
    }
}
