package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class S20PackedUnpackedComparisonTest {
    private static final List<String> QUERIES = List.of(
            "#a PRED %p",
            "#a ^(SUBJ) #b",
            "#a ^(@GF*) #b",
            "#a ^(@GF*) #b ^(@GF) #c"
    );

    @Test
    void packedAndUnpackedS20HaveEquivalentQueryResults() {
        PathVariables.initializePathVariables();
        XLEoperator operator = new XLEoperator(new VariableHandler());
        TemplateRegistry templates = new TemplateParser().parse("GF := SUBJ | OBJ | OBL .");

        LinguisticStructure packed = load(operator, "testdirS20.pl");
        List<LinguisticStructure> unpacked = List.of(
                load(operator, "testdirS20-1.pl"),
                load(operator, "testdirS20-2.pl"));

        assertEquals(2, unpacked.size());
        int[] expectedPacked = {5, 2, 175, 2};
        int[] expectedUnpacked = {10, 4, 350, 4};
        for (int queryIndex = 0; queryIndex < QUERIES.size(); queryIndex++) {
            String query = QUERIES.get(queryIndex);
            int packedResults = query(query, packed, templates);
            int unpackedResults = unpacked.stream()
                    .mapToInt(reading -> query(query, reading, templates))
                    .sum();
            System.out.printf("S20 query=%s packed=%d unpacked=%d%n",
                    query, packedResults, unpackedResults);
            assertFalse(packedResults == 0, query);
            assertFalse(unpackedResults == 0, query);
            assertEquals(expectedPacked[queryIndex], packedResults, query + " packed");
            assertEquals(expectedUnpacked[queryIndex], unpackedResults, query + " unpacked");
        }
    }

    private LinguisticStructure load(XLEoperator operator, String fixture) {
        LinkedHashMap<String, LinguisticStructure> loaded = operator.fs2Java(
                Path.of(PathVariables.testPath, fixture).toString());
        assertEquals(1, loaded.size(), fixture);
        return loaded.values().iterator().next();
    }

    private int query(String query, LinguisticStructure structure, TemplateRegistry templates) {
        QueryParser parser = new QueryParser(query, structure, templates);
        QueryParserResult result = parser.parseQuery(parser.getQueryList());
        return result.result.size();
    }
}
