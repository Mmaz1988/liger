package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.TemplateExpander;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateParserTest {

    @Test
    void testTemplateExpansionWithArguments() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        TemplateRegistry registry = new TemplateParser().parse("feature-match(#node,#attr,#value) := #node #attr #value .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TNS-ASP #h & @feature-match(#h,PERF,'-_')", fs.get(key), registry);
            List<QueryParserResult> results = qp.parseQueryWithTemplates("#g TNS-ASP #h & @feature-match(#h,PERF,'-_')");

            assertEquals(1, results.size());
            assertTrue(results.get(0).isSuccess);
            assertTrue(results.get(0).result.keySet().size() > 0);
        }
    }

    @Test
    void testTemplateExpansionBranchesOverLabelDisjunction() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        TemplateRegistry registry = new TemplateParser().parse("feature-label() := TENSE | PERF .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TNS-ASP #h & #h @feature-label() '-_'", fs.get(key), registry);
            List<QueryParserResult> results = qp.parseQueryWithTemplates("#g TNS-ASP #h & #h @feature-label() '-_'");

            assertEquals(2, results.size());
            assertTrue(results.stream().anyMatch(r -> r.isSuccess));
        }
    }

    @Test
    void testTemplateParserReadsMultipleDefinitions() {
        TemplateRegistry registry = new TemplateParser().parse(
                "feature-match(#node,#attr,#value) := #node #attr #value . feature-label() := TENSE | PERF .");

        assertTrue(registry.getTemplates().containsKey("feature-match"));
        assertTrue(registry.getTemplates().containsKey("feature-label"));
        assertEquals(3, registry.getTemplate("feature-match").getParameters().size());
        assertEquals(2, registry.getTemplate("feature-label").getAlternatives().size());
    }

    @Test
    void testTemplateExpansionRejectsUnknownTemplate() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        TemplateRegistry registry = new TemplateParser().parse("feature-label() := TENSE | PERF .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TNS-ASP #h & @missing()", fs.get(key), registry);
            assertThrows(IllegalArgumentException.class,
                    () -> qp.parseQueryWithTemplates("#g TNS-ASP #h & @missing()"));
        }
    }

    @Test
    void testTemplateExpansionRejectsArityMismatch() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        TemplateRegistry registry = new TemplateParser().parse("feature-match(#node,#attr,#value) := #node #attr #value .");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g TNS-ASP #h & @feature-match(#h,PERF)", fs.get(key), registry);
            assertThrows(IllegalArgumentException.class,
                    () -> qp.parseQueryWithTemplates("#g TNS-ASP #h & @feature-match(#h,PERF)"));
        }
    }

    @Test
    void testTemplateExpansionSupportsNestedInvocations() {
        TemplateParser parser = new TemplateParser();
        TemplateRegistry registry = parser.parse("feature-label() := TENSE | PERF .");
        TemplateRegistry featureMatchRegistry = parser.parse("feature-match(#node,#value) := #node @feature-label() #value .");
        featureMatchRegistry.getTemplates().values().forEach(registry::addTemplate);

        List<List<String>> expansions = TemplateExpander.expandQuery(
                "#g TNS-ASP #h & @feature-match(#h,'-_' )", registry);

        assertEquals(2, expansions.size());
        assertTrue(expansions.stream().anyMatch(t -> t.contains("TENSE")));
        assertTrue(expansions.stream().anyMatch(t -> t.contains("PERF")));
    }

    @Test
    void testTemplateQueryMatchesInlineQuery() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        TemplateRegistry registry = new TemplateParser().parse(
                "TAM(#g,#h) := #g TNS-ASP #h TENSE 'past' .");

        for (String key : fs.keySet()) {
            QueryParser templateParser = new QueryParser("@TAM(#a,#b)", fs.get(key), registry);
            List<QueryParserResult> templateResults = templateParser.parseQueryWithTemplates("@TAM(#a,#b)");

            QueryParser inlineParser = new QueryParser("#u TNS-ASP #v TENSE 'past'", fs.get(key));
            QueryParserResult inlineResult = inlineParser.parseQuery(inlineParser.getQueryList());

            assertEquals(1, templateResults.size());
            assertEquals(normalizeResult(inlineResult), normalizeResult(templateResults.get(0)));
        }
    }

    @Test
    void testTemplateQueryMatchesInlineQueryInsideLargerQuery() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        TemplateRegistry registry = new TemplateParser().parse(
                "TAM(#g,#h) := #g TNS-ASP #h TENSE 'past' .");

        for (String key : fs.keySet()) {
            QueryParser templateParser = new QueryParser("#r SUBJ #s & @TAM(#a,#b)", fs.get(key), registry);
            List<QueryParserResult> templateResults = templateParser.parseQueryWithTemplates("#r SUBJ #s & @TAM(#a,#b)");

            QueryParser inlineParser = new QueryParser("#r SUBJ #s & #u TNS-ASP #v TENSE 'past'", fs.get(key));
            QueryParserResult inlineResult = inlineParser.parseQuery(inlineParser.getQueryList());

            assertEquals(1, templateResults.size());
            assertEquals(normalizeResult(inlineResult), normalizeResult(templateResults.get(0)));
        }
    }

    @Test
    void testTemplateQueryMatchesInlineQueryInsideLargerQuery2() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        TemplateRegistry registry = new TemplateParser().parse(
                "TAM(#g,#h) := #g TNS-ASP #h TENSE 'past' .");

        for (String key : fs.keySet()) {
            QueryParser templateParser = new QueryParser("#a COMP #b & @TAM(#b,#c)", fs.get(key), registry);
            List<QueryParserResult> templateResults = templateParser.parseQueryWithTemplates("#a COMP #b & @TAM(#b,#c)");

            QueryParser inlineParser = new QueryParser("#f COMP #g & #g TNS-ASP #h TENSE 'past'", fs.get(key));
            QueryParserResult inlineResult = inlineParser.parseQuery(inlineParser.getQueryList());

            assertEquals(1, templateResults.size());
            assertEquals(normalizeResult(inlineResult), normalizeResult(templateResults.get(0)));
        }
    }

    @Test
    void testTemplateInsideUncertaintyMatchesManualExpansion() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS16.pl");
        TemplateRegistry registry = new TemplateParser().parse(
                "GF := COMP | XCOMP .");

        for (String key : fs.keySet()) {
            QueryParser templateParser = new QueryParser("#g ^(@GF*) #h SUBJ #i", fs.get(key), registry);
            QueryParserResult templateResult = templateParser.parseQuery(templateParser.getQueryList());

            List<String> actual = normalizeResult(templateResult);
            QueryParser compParser = new QueryParser("#a ^(COMP) #b SUBJ #c", fs.get(key));
            QueryParserResult compResult = compParser.parseQuery(compParser.getQueryList());

            QueryParser xcompParser = new QueryParser("#a ^(XCOMP) #b SUBJ #c", fs.get(key));
            QueryParserResult xcompResult = xcompParser.parseQuery(xcompParser.getQueryList());

            QueryParser mixedParser = new QueryParser("#a ^(XCOMP>COMP) #b SUBJ #c", fs.get(key));
            QueryParserResult mixedResult = mixedParser.parseQuery(mixedParser.getQueryList());

            assertTrue(actual.containsAll(normalizeResult(compResult)));
            assertTrue(actual.containsAll(normalizeResult(xcompResult)));
            assertTrue(actual.containsAll(normalizeResult(mixedResult)));
        }
    }

    @Test
    void testTemplateInsideUncertaintyAcceptsMixedRepeatedLabels() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS16.pl");
        TemplateRegistry registry = new TemplateParser().parse(
                "GF := COMP | XCOMP .");

        for (String key : fs.keySet()) {
            QueryParser templateParser = new QueryParser("#g ^(@GF*>@GF*) #h SUBJ #i", fs.get(key), registry);
            QueryParserResult templateResult = templateParser.parseQuery(templateParser.getQueryList());

            List<String> actual = normalizeResult(templateResult);
            assertTrue(actual.stream().anyMatch(s -> s.contains("XCOMP") && s.contains("COMP") && s.contains("SUBJ")));
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

    private String normalizeSolution(Set<?> solutionKey, HashMap<String, HashMap<String, HashMap<Integer, de.ukon.liger.syntax.GraphConstraint>>> binding) {
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
