package de.ukon.liger.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ukon.liger.analysis.QueryParser.HierarchyParser;
import de.ukon.liger.analysis.QueryParser.HierarchyRegistry;
import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

    @Test
    void testQueryParser6() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS2.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g !(SUBJ) #h CASE 'nom'", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(4, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser7() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS2.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g !(SUBJ>NTYPE) #h", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(4, qpr.result.keySet().size());
        }
    }

    @Test
    void testTemplateInvocationInsideNegationIsExpanded() throws Exception {
        Path mergedGraph = Paths.get("merged-graph.json");
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(mergedGraph.toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        TemplateRegistry templateRegistry = new TemplateParser().parse(
                "GF := SUBJ | OBJ | OBL . " +
                "COARG-PATH(#a,#b,#c) := #a ^(@GF*:~(->PRED)) #b ^(@GF) #c . " +
                "COARG(#a,#b) := @COARG-PATH(#a,#r,#s) & #s !(@GF) #b & id(#r) != id(#b) .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBJ2 > OBL .");

        QueryParser qp = new QueryParser("-(@COARG(#a,#b))", fs, templateRegistry, hierarchyRegistry);
        QueryParserResult qpr = qp.parseQueryWithTemplates("-(@COARG(#a,#b))").get(0);

        assertTrue(qpr.result.isEmpty());
    }

    @Test
    void testExactNegatedBindingPairIsFilteredOnMergedGraph4() throws Exception {
        Path mergedGraph = Paths.get("merged-graph4.json");
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(mergedGraph.toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        TemplateRegistry templateRegistry = new TemplateParser().parse(
                "GF := SUBJ | OBJ | OBL . " +
                "DR-GF-LINK(#a, #d) := #a SRC %a & #b SYN-ID %b & %a == %b & #b ^(in_set>GLUE>g::>cproj) #c phi #d . " +
                "MCN-PATH(#a,#b,#c) := #a ^(@GF*:~(->SUBJ)) #b & #b ^(@GF) #c . " +
                "REFL-BIND(#f,#h) := #f PRON-TYPE 'refl' & @MCN-PATH(#f,#i,#j) & #j !(@GF) #h & superior(GF,#h,#i) . " +
                "COARG-PATH(#a,#b,#c) := #a ^(@GF*:~(->PRED)) #b ^(@GF) #c . " +
                "COARG(#a,#b) := @COARG-PATH(#a,#r,#s) & #s !(@GF) #b & id(#a) != id(#b) . " +
                "DR-PRECEDENCE(#a,#b) := #a NAME %a & #a NODE_TYPE referent & #b NAME %b & #b NODE_TYPE referent & id(%a) < id(%b) .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBJ2 > OBL .");

        String query = "#a ant #a & #a SYNSEM #b & #c SYNSEM #d &@DR-PRECEDENCE(#c,#a) & -(@COARG(#b,#d))";
        QueryParser qp = new QueryParser(query, fs, templateRegistry, hierarchyRegistry);
        QueryParserResult qpr = qp.parseQueryWithTemplates(query).get(0);

        assertTrue(qpr.isSuccess);
        assertTrue(qpr.result.size() > 0);
        assertFalse(containsSignature(qpr, "a=007", "b=2", "c=006", "d=4"));
    }

    @Test
    void testGroundedCoargQueryMatchesMergedGraph4Pair() throws Exception {
        Path mergedGraph = Paths.get("merged-graph4.json");
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(mergedGraph.toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        TemplateRegistry templateRegistry = new TemplateParser().parse(
                "GF := SUBJ | OBJ | OBL . " +
                "COARG-PATH(#a,#b,#c) := #a ^(@GF*:~(->PRED)) #b ^(@GF) #c . " +
                "COARG(#a,#b) := @COARG-PATH(#a,#r,#s) & #s !(@GF) #b & id(#a) != id(#b) .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBJ2 > OBL .");

        QueryParser qp = new QueryParser("@COARG(*2,*4)", fs, templateRegistry, hierarchyRegistry);
        QueryParserResult qpr = qp.parseQueryWithTemplates("@COARG(*2,*4)").get(0);

        assertTrue(qpr.isSuccess);
    }

    private boolean containsSignature(QueryParserResult result, String... parts) {
        for (Set<?> solutionKey : result.result.keySet()) {
            java.util.Map<String, String> binding = new java.util.TreeMap<>();

            for (Object entry : solutionKey) {
                de.ukon.liger.analysis.QueryParser.SolutionKey sk = (de.ukon.liger.analysis.QueryParser.SolutionKey) entry;
                binding.put(sk.variable, sk.reference);
            }

            String signature = binding.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((left, right) -> left + "|" + right)
                    .orElse("");

            boolean matches = true;
            for (String part : parts) {
                if (!signature.contains(part)) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return true;
            }
        }

        return false;
    }
}
