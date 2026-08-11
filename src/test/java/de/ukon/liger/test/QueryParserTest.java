package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.HierarchyParser;
import de.ukon.liger.analysis.QueryParser.HierarchyRegistry;
import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryParserTest {

    public LinkedHashMap<String, LinguisticStructure> loadFs(String fileName) {
        PathVariables.initializePathVariables();
        VariableHandler vh = new VariableHandler();
        XLEoperator xle = new XLEoperator(vh);
        return xle.fs2Java(Paths.get(PathVariables.testPath, fileName).toString());
    }

    @Test
    void testQueryParser() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS1.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TENSE 'past' & #h PERF '-_'", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());
            assertEquals(4, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParserSupportsFilterOnlyQueries() {
        LinguisticStructure structure = new LinguisticStructure("filter-test", "filter test", new ArrayList<>());
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "1", "POTENTIAL-ANT", "2", "c", false));
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "1", "POSSIBLE-ANT", "2", "c", false));
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "3", "TENSE", "past", "c", false));
        structure.constraints.add(new GraphConstraint(new LinkedHashSet<>(), "3", "TENSE", "present", "c", false));

        QueryParser edgeParser = new QueryParser("edge=POTENTIAL-ANT", structure);
        QueryParserResult edgeResult = edgeParser.parseQuery(edgeParser.getQueryList());
        assertTrue(edgeResult.isSuccess);
        assertEquals(1, edgeResult.result.keySet().size());

        QueryParser valueParser = new QueryParser("value=past", structure);
        QueryParserResult valueResult = valueParser.parseQuery(valueParser.getQueryList());
        assertTrue(valueResult.isSuccess);
        assertEquals(1, valueResult.result.keySet().size());

        QueryParser tenseParser = new QueryParser("TENSE past", structure);
        QueryParserResult tenseResult = tenseParser.parseQuery(tenseParser.getQueryList());
        assertTrue(tenseResult.isSuccess);
        assertEquals(1, tenseResult.result.keySet().size());
    }

    @Test
    void testQueryParser5() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS2.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g !(COMP*>TNS-ASP) #h", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());
            assertEquals(6, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParsera() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS2.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("SUBJ #a", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser2() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS2.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TENSE 'past' & #g TENSE 'past'", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser3() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS1.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TENSE 'past' & #h PERF '-_' & #j SUBJ #i", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(12, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser4a() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS1.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TNS-ASP #h TENSE 'past' & #h PERF '-_'", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser4b() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS2.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' & #h MOOD 'indicative'", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(3, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser4c() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS1.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' & #h MOOD 'indicative'", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser9() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS3.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TENSE %g", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser10() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS1.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TENSE %g & #h TENSE %g", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(4, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser11() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS3.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TENSE %g & #h TENSE %h", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(4, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser12() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS3.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TENSE %g & %g != 'past' & #h PERF '-_'", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2, qpr.result.keySet().size());
        }
    }

    @Test
    void testQueryParser13() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS3.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g PRED %g & strip(%g) == 'John' & #h PERF '-_'", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2, qpr.result.keySet().size());
        }
    }

    @Test
    @Disabled("Requires merged-graph.json fixture, which was never committed and is missing from disk")
    void testQueryParserValueEqualityOnMergedGraph() throws Exception {
        Path mergedGraph = Paths.get("merged-graph.json");
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(mergedGraph.toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        QueryParser qp = new QueryParser("#a SYN-ID %a & #b SRC %b & %a == %b", fs);
        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        assertTrue(qpr.isSuccess);
        assertEquals(4, qpr.result.keySet().size());
        assertEquals(4, qpr.valueBindings.keySet().size());
    }

    @Test
    @Disabled("Requires merged-graph.json fixture, which was never committed and is missing from disk")
    void testInlineAndTemplateQueryOnMergedGraphReturnSameTwoSolutions() throws Exception {
        Path mergedGraph = Paths.get("merged-graph.json");
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(mergedGraph.toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        TemplateRegistry templateRegistry = new TemplateParser().parse(
                "GF := SUBJ | OBJ | OBL . " +
                "MCN-PATH(#a,#b) := #a ^(@GF*:~(->SUBJ)) #b . " +
                "REFL-BIND(#f,#h) := @MCN-PATH(#f,#i) & #i ^(@GF) #j !(@GF) #h & superior(GF,#h,#i) .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        String templateQuery = "#a ant #a & #a SYNSEM #b & @REFL-BIND(#b,#c)";
        String inlineQuery = "#a ant #a & #a SYNSEM #b & #b ^(@GF*:~(->SUBJ)) #d ^(@GF) #e !(@GF) #c & superior(GF,#c,#d)";

        QueryParser templateParser = new QueryParser(templateQuery, fs, templateRegistry, hierarchyRegistry);
        QueryParserResult templateResult = templateParser.parseQueryWithTemplates(templateQuery).get(0);

        QueryParser inlineParser = new QueryParser(inlineQuery, fs, templateRegistry, hierarchyRegistry);
        QueryParserResult inlineResult = inlineParser.parseQuery(inlineParser.getQueryList());

        assertTrue(templateResult.isSuccess);
        assertEquals(2, templateResult.result.keySet().size());
        assertEquals(2, inlineResult.result.keySet().size());
        assertEquals(normalizeResult(inlineResult), normalizeResult(templateResult));
    }

    @Test
    void testQueryParser17() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testdirS15.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("*c11 !(cproj>g::>GLUE>in_set) #s", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertTrue(qpr.isSuccess && qpr.result.size() == 1);
        }
    }

    @Test
    void testQueryParserHybrid() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("hybrid_glue_test.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#a TNS-ASP #b & #a s:: #c SIT #d & #c TEMP-REF #e", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());
            assertEquals(2, qpr.result.keySet().size());
        }
    }

    @Test
    void testInsideOutObjOnS18() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testDirS18.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#a ^(OBJ) #b", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            List<String> normalized = normalizeResult(qpr);
            assertEquals(2, normalized.size());
            assertEquals(List.of(
                    "f0|f1::[[1]] #f1 CASE 'acc';[[1]] #f1 GEND 'masc';[[1]] #f1 NTYPE #f2;[[1]] #f1 NUM 'sg';[[1]] #f1 PERS '3';[[1]] #f1 PRED semform('Bertie',2,[],[]);[[1]] #f1 end int(17);[[1]] #f1 start int(11)",
                    "f3|f4::[[1]] #f4 CASE 'acc';[[1]] #f4 GEND 'masc';[[1]] #f4 NTYPE 'pron';[[1]] #f4 NUM 'sg';[[1]] #f4 PERS '3';[[1]] #f4 PRED semform('pro',4,[],[]);[[1]] #f4 PRON-TYPE 'reflexive';[[1]] #f4 end int(31);[[1]] #f4 start int(24)"
            ), normalized);
        }
    }

    @Test
    void testInsideOutObjPlusOnS18() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testDirS18.pl");
        TemplateRegistry templateRegistry = new TemplateParser().parse("GF := SUBJ | OBJ | OBL .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#a ^(@GF+) #b", fs.get(key), templateRegistry);
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            List<String> normalized = normalizeResult(qpr);
            assertEquals(5, normalized.size());
        }
    }

    @Test
    void testInsideOutObjStarAndAttributeOnS18() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testDirS18.pl");
        TemplateRegistry templateRegistry = new TemplateParser().parse("GF := SUBJ | OBJ | OBL .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser(
                    "#a ^(@GF*) #b ^(@GF) #c & #c !(@GF) #d & superior(GF,#d,#b) & #a PRON-TYPE",
                    fs.get(key),
                    templateRegistry,
                    hierarchyRegistry);
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            List<String> normalized = normalizeResult(qpr);
            assertEquals(2, normalized.size());
        }
    }

    @Test
    void debugInsideOutObjStarIncrementalOnS18() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testDirS18.pl");
        TemplateRegistry templateRegistry = new TemplateParser().parse("GF := SUBJ | OBJ | OBL .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        String[] stages = {
                "#a SUBJ #b",
                "#a !(@GF) #b",
                "#a ^(@GF) #b",
                "#a ^(@GF*) #b",
                "#a ^(@GF*) #b ^(@GF) #c",
                "#a ^(@GF*) #b ^(@GF) #c & #c !(@GF) #d",
                "#a ^(@GF*) #b ^(@GF) #c & #c !(@GF) #d & superior(GF,#d,#b)",
                "#a ^(@GF*) #b ^(@GF) #c & #c !(@GF) #d & superior(GF,#d,#b) & #a PRON-TYPE"
        };

        for (String key : fs.keySet()) {
            for (String stage : stages) {
                QueryParser qp = new QueryParser(stage, fs.get(key), templateRegistry, hierarchyRegistry);
                QueryParserResult result = qp.parseQuery(qp.getQueryList());
                System.out.println("incremental uncertainty: " + stage + " -> "
                        + result.result.size() + " solutions, success=" + result.isSuccess
                        + ", keys=" + result.result.keySet().stream()
                        .map(solution -> solution.stream()
                                .map(solutionKey -> solutionKey.variable + "=" + solutionKey.reference)
                                .sorted()
                                .toList())
                        .toList());
            }
        }
    }

    @Test
    void testInsideOutObjStarOnS18MatchesFiveSolutions() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testDirS18.pl");
        TemplateRegistry templateRegistry = new TemplateParser().parse("GF := SUBJ | OBJ | OBL .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser(
                    "#a ^(@GF*) #b ^(@GF) #c & #c !(@GF) #d & superior(GF,#d,#b)",
                    fs.get(key),
                    templateRegistry,
                    hierarchyRegistry);
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(5, qpr.result.keySet().size());
        }
    }

    @Test
    void testInsideOutObjStarWithOffPathConstraintOnS19() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testDirS19.pl");
        TemplateRegistry templateRegistry = new TemplateParser().parse("GF := SUBJ | OBJ | OBL | COMP .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL > COMP .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser(
                    "#a ^(@GF+:~(->SUBJ)) #b ^(@GF) #c & #c !(@GF) #d & superior(GF,#d,#b)",
                    fs.get(key),
                    templateRegistry,
                    hierarchyRegistry);
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(2, qpr.result.keySet().size());
        }
    }

    @Test
    void testInsideOutObjStarMatchesSixSolutionsOnS19WithOffPathConstraint() {
        LinkedHashMap<String, LinguisticStructure> fs = loadFs("testDirS19.pl");
        TemplateRegistry templateRegistry = new TemplateParser().parse("GF := SUBJ | OBJ | OBL | COMP .");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL > COMP .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser(
                    "#a ^(@GF*:~(->SUBJ)) #b ^(@GF) #c & #c !(@GF) #d & superior(GF,#d,#b)",
                    fs.get(key),
                    templateRegistry,
                    hierarchyRegistry);
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            assertEquals(6, qpr.result.keySet().size());
        }
    }

    private List<String> normalizeResult(QueryParserResult qpr) {
        List<String> out = new ArrayList<>();

        for (Set<?> solutionKey : qpr.result.keySet()) {
            out.add(normalizeSolution(solutionKey, qpr.result.get(solutionKey)));
        }

        out.sort(String::compareTo);
        return out;
    }

    private String normalizeSolution(Set<?> solutionKey, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding) {
        List<String> refs = new ArrayList<>();

        for (Object entry : solutionKey) {
            de.ukon.liger.analysis.QueryParser.SolutionKey sk = (de.ukon.liger.analysis.QueryParser.SolutionKey) entry;
            refs.add(sk.reference);
        }

        refs.sort(String::compareTo);

        List<String> constraints = new ArrayList<>();
        for (HashMap<String, HashMap<Integer, GraphConstraint>> variableBinding : binding.values()) {
            for (HashMap<Integer, GraphConstraint> referenceBinding : variableBinding.values()) {
                for (GraphConstraint constraint : referenceBinding.values()) {
                    constraints.add(constraint.toString());
                }
            }
        }

        constraints.sort(String::compareTo);
        return String.join("|", refs) + "::" + String.join(";", constraints);
    }
}
