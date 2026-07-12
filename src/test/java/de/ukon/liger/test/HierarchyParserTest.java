package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.HierarchyParser;
import de.ukon.liger.analysis.QueryParser.HierarchyRegistry;
import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HierarchyParserTest {

    @Test
    void testHierarchyParserReadsOrder() {
        HierarchyRegistry registry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        assertTrue(registry.contains("GF"));
        assertEquals(List.of("SUBJ", "OBJ", "OBL"), registry.getHierarchy("GF"));
        assertTrue(registry.isSuperior("GF", "SUBJ", "OBJ"));
        assertTrue(registry.isSuperior("GF", "SUBJ", "OBL"));
        assertTrue(registry.isSuperior("GF", "OBJ", "OBL"));
    }

    @Test
    @Disabled("Temporarily disabled while investigating conjunct ordering for superior()")
    void testSuperiorEnumeratesPairsOnS17() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        HierarchyRegistry registry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("superior(GF,#a,#c)", fs.get(key), registry);
            QueryParserResult result = qp.parseQuery(qp.getQueryList());

            assertTrue(result.isSuccess);
            Set<String> pairs = extractNodePairs(result, "#a", "#c");
            assertTrue(pairs.contains("6>1"));
            assertTrue(pairs.contains("6>3"));
            assertTrue(pairs.contains("1>3"));
        }
    }

    @Test
    void testSuperiorFiltersInLargerQuery() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        HierarchyRegistry registry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("superior(GF,#b,#d) & #a SUBJ #b & #a OBL #d", fs.get(key), registry);
            QueryParserResult result = qp.parseQuery(qp.getQueryList());

            assertTrue(result.isSuccess);
            assertTrue(extractNodePairs(result, "#b", "#d").contains("6>3"));
        }
    }

    @Test
    void testSuperiorAfterBoundGFsMatches() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        HierarchyRegistry registry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        for (String key : fs.keySet()) {
          //  QueryParser baseline = new QueryParser("#a SUBJ #b & #a OBL #d", fs.get(key), registry);
          //  QueryParserResult baselineResult = baseline.parseQuery(baseline.getQueryList());

            QueryParser qp = new QueryParser("#a SUBJ #b & #a OBL #d & superior(GF,#b,#d)", fs.get(key), registry);
            QueryParserResult result = qp.parseQuery(qp.getQueryList());

            assertTrue(result.isSuccess);
            assertTrue(extractNodePairs(result, "#b", "#d").contains("6>3"));
        }
    }

    @Test
    void testSuperiorRejectsInverseOrder() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        HierarchyRegistry registry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("superior(GF,#d,#b) & #a SUBJ #b & #c OBL #d", fs.get(key), registry);
            QueryParserResult result = qp.parseQuery(qp.getQueryList());

            assertTrue(result.result.isEmpty());
        }
    }

    @Test
    void testSuperiorAfterBoundGFsRejectsInverseOrder() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        HierarchyRegistry registry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#a SUBJ #b & #a OBL #d & superior(GF,#d,#b)", fs.get(key), registry);
            QueryParserResult result = qp.parseQuery(qp.getQueryList());

            assertTrue(result.result.isEmpty());
        }
    }

    @Test
    void testSuperiorFailsWhenVariablesMissing() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        HierarchyRegistry registry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#a SUBJ #b & superior(GF,#b,#d)", fs.get(key), registry);

            assertThrows(IllegalArgumentException.class, () -> qp.parseQuery(qp.getQueryList()));
        }
    }

    @Test
    void testMegaFeatureCombination() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");
        TemplateRegistry templateRegistry = new TemplateParser().parse(
                "MEGA() := #a SUBJ #b & #c OBL #d & superior(GF,#b,#d) & -(#a OBL #b) .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser(
                    "@MEGA()",
                    fs.get(key),
                    templateRegistry,
                    hierarchyRegistry);

            QueryParserResult result = qp.parseQueryWithTemplates("@MEGA()").get(0);

            assertTrue(result.isSuccess);
            assertTrue(extractNodePairs(result, "#b", "#d").contains("6>3"));
        }
    }

    @Test
    void testSuperiorInsideTemplateMatchesInline() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        HierarchyRegistry hierarchyRegistry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");
        TemplateRegistry templateRegistry = new TemplateParser().parse(
                "SUPERIORQUERY() := #a SUBJ #b & #c OBL #d & superior(GF,#b,#d) .");

        for (String key : fs.keySet()) {
            QueryParser templateParser = new QueryParser(
                    "@SUPERIORQUERY()",
                    fs.get(key),
                    templateRegistry,
                    hierarchyRegistry);
            QueryParserResult templateResult = templateParser.parseQueryWithTemplates("@SUPERIORQUERY()").get(0);

            QueryParser inlineParser = new QueryParser(
                    "#a SUBJ #b & #c OBL #d & superior(GF,#b,#d)",
                    fs.get(key),
                    hierarchyRegistry);
            QueryParserResult inlineResult = inlineParser.parseQuery(inlineParser.getQueryList());

            assertEquals(normalizeResult(inlineResult), normalizeResult(templateResult));
        }
    }

    @Test
    @Disabled("Nested superior inside negation still needs parser follow-up")
    void testSuperiorInsideNegationTrueAndFalse() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        HierarchyRegistry registry = new HierarchyParser().parse("GF ::= SUBJ > OBJ > OBL .");
        TemplateRegistry templateRegistry = new TemplateParser().parse(
                "SUPNEGTRUE() := -(#a SUBJ #b & #c OBL #d & superior(GF,#d,#b)) . " +
                "SUPNEGFALSE() := -(#a SUBJ #b & #c OBL #d & superior(GF,#b,#d)) .");

        for (String key : fs.keySet()) {
            QueryParser trueNegation = new QueryParser("@SUPNEGTRUE()", fs.get(key), templateRegistry, registry);
            QueryParserResult trueResult = trueNegation.parseQueryWithTemplates("@SUPNEGTRUE()").get(0);
            assertTrue(trueResult.isSuccess);

            QueryParser falseNegation = new QueryParser("@SUPNEGFALSE()", fs.get(key), templateRegistry, registry);
            QueryParserResult falseResult = falseNegation.parseQueryWithTemplates("@SUPNEGFALSE()").get(0);
            assertTrue(falseResult.result.isEmpty());
        }
    }

    @Test
    void testNegationInsideTemplateMatchesInline() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        TemplateRegistry templateRegistry = new TemplateParser().parse(
                "NOOBL() := -(#a OBL #b) .");

        for (String key : fs.keySet()) {
            QueryParser templateParser = new QueryParser(
                    "#a SUBJ #b & @NOOBL()",
                    fs.get(key),
                    templateRegistry);
            QueryParserResult templateResult = templateParser.parseQueryWithTemplates("#a SUBJ #b & @NOOBL()").get(0);

            QueryParser inlineParser = new QueryParser("#a SUBJ #b & -(#a OBL #b)", fs.get(key));
            QueryParserResult inlineResult = inlineParser.parseQuery(inlineParser.getQueryList());

            assertEquals(normalizeResult(inlineResult), normalizeResult(templateResult));
        }
    }

    @Test
    void testNegationTrueAndFalse() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");

        for (String key : fs.keySet()) {
            QueryParser trueParser = new QueryParser("#a SUBJ #b & -(#a OBL #b)", fs.get(key));
            QueryParserResult trueResult = trueParser.parseQuery(trueParser.getQueryList());
            assertTrue(trueResult.isSuccess);

            QueryParser falseParser = new QueryParser("#a SUBJ #b & -(#a SUBJ #b)", fs.get(key));
            QueryParserResult falseResult = falseParser.parseQuery(falseParser.getQueryList());
            assertTrue(falseResult.result.isEmpty());

            QueryParser falseParser1 = new QueryParser("#a SUBJ #b & -(#a OBL)", fs.get(key));
            QueryParserResult falseResult1 = falseParser1.parseQuery(falseParser.getQueryList());
            assertTrue(falseResult1.result.isEmpty());
        }
    }

    private boolean resultContainsRelationPair(QueryParserResult result, String firstLabel, String secondLabel) {
        return extractRelationPairs(result).contains(firstLabel + ">" + secondLabel);
    }

    private Set<String> extractNodePairs(QueryParserResult result, String firstVar, String secondVar) {
        Set<String> pairs = new HashSet<>();
        String normalizedFirstVar = firstVar.startsWith("#") ? firstVar.substring(1) : firstVar;
        String normalizedSecondVar = secondVar.startsWith("#") ? secondVar.substring(1) : secondVar;

        for (Set<?> solutionKey : result.result.keySet()) {
            Map<String, String> refs = new HashMap<>();
            for (Object key : solutionKey) {
                de.ukon.liger.analysis.QueryParser.SolutionKey solutionKeyEntry =
                        (de.ukon.liger.analysis.QueryParser.SolutionKey) key;
                refs.put(solutionKeyEntry.variable, solutionKeyEntry.reference);
            }

            if (refs.containsKey(normalizedFirstVar) && refs.containsKey(normalizedSecondVar)) {
                pairs.add(refs.get(normalizedFirstVar) + ">" + refs.get(normalizedSecondVar));
            }
        }

        return pairs;
    }

    private Set<String> extractRelationPairs(QueryParserResult result) {
        Set<String> pairs = new HashSet<>();

        for (Set<?> solutionKey : result.result.keySet()) {
            List<String> labels = new ArrayList<>();
            for (HashMap<String, HashMap<Integer, de.ukon.liger.syntax.GraphConstraint>> variableBinding : result.result.get(solutionKey).values()) {
                for (HashMap<Integer, de.ukon.liger.syntax.GraphConstraint> referenceBinding : variableBinding.values()) {
                    for (de.ukon.liger.syntax.GraphConstraint constraint : referenceBinding.values()) {
                        labels.add(constraint.getRelationLabel());
                    }
                }
            }

            if (labels.contains("SUBJ") && labels.contains("OBJ")) {
                pairs.add("SUBJ>OBJ");
            }
            if (labels.contains("SUBJ") && labels.contains("OBL")) {
                pairs.add("SUBJ>OBL");
            }
            if (labels.contains("OBJ") && labels.contains("OBL")) {
                pairs.add("OBJ>OBL");
            }
        }

        return pairs;
    }

    private List<String> normalizeResult(QueryParserResult qpr) {
        List<String> out = new ArrayList<>();

        for (Set<?> solutionKey : qpr.result.keySet()) {
            out.add(normalizeSolution(solutionKey, qpr.result.get(solutionKey)));
        }

        out.sort(String::compareTo);
        return out;
    }

    private String normalizeSolution(Set<?> solutionKey,
                                     HashMap<String, HashMap<String, HashMap<Integer, de.ukon.liger.syntax.GraphConstraint>>> binding) {
        List<String> refs = new ArrayList<>();

        for (Object entry : solutionKey) {
            de.ukon.liger.analysis.QueryParser.SolutionKey sk = (de.ukon.liger.analysis.QueryParser.SolutionKey) entry;
            refs.add(sk.reference);
        }

        refs.sort(String::compareTo);

        List<String> constraints = new ArrayList<>();
        for (HashMap<String, HashMap<Integer, de.ukon.liger.syntax.GraphConstraint>> variableBinding : binding.values()) {
            for (HashMap<Integer, de.ukon.liger.syntax.GraphConstraint> referenceBinding : variableBinding.values()) {
                for (de.ukon.liger.syntax.GraphConstraint constraint : referenceBinding.values()) {
                    constraints.add(constraint.toString());
                }
            }
        }

        constraints.sort(String::compareTo);
        return String.join("|", refs) + "::" + String.join(";", constraints);
    }
}
