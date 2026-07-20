package de.ukon.liger.test;

import de.ukon.liger.analysis.RuleParser.Rule;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.Fstructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuleParserTest {

    @Test
    void testPrologPrint() throws IOException {
        PathVariables.initializePathVariables();
        VariableHandler vh = new VariableHandler();
        XLEoperator xle = new XLEoperator(vh);
        Fstructure fs = (Fstructure) xle.xle2Java(Paths.get(PathVariables.testPath, "testdirS1.pl").toString());
        System.out.println(fs.writeToProlog(false));
    }

    @Test
    void testRuleParser() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS3.pl");

        ArrayList<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' ==> #h SEM 'event'");

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(1, fslist.get(0).annotation.size());
    }

    @Test
    void testRuleParser2() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS3.pl");

        ArrayList<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' ==> #i SEM 'event'");

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(1, fslist.get(0).annotation.size());
    }

    @Test
    void testRuleParser3() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");

        ArrayList<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' ==> #i SEM 'event'");
        Rule r2 = new Rule("#i SEM 'event' ==> #i PAST 'past'");

        rp.getRules().add(r1);
        rp.getRules().add(r2);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(4, fslist.get(0).annotation.size());
    }

    @Test
    void testRuleParser5() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");

        ArrayList<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist);
        rp.setReplace(true);

        Rule r1 = new Rule("#i PRED %i ==> #i SEM 'strip(%i)'");

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(5, fslist.get(0).annotation.size());
    }

    @Test
    void testRuleParser6() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");

        ArrayList<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist);
        rp.setReplace(true);

        Rule r1 = new Rule("#i PRED %i ==> #i SEM 'strip(%i)'");
        Rule r2 = new Rule("#i PRED %i ==> #i SEM 'This string replaced: strip(%i)'");

        rp.getRules().add(r1);
        rp.getRules().add(r2);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(5, fslist.get(0).annotation.size());
    }

    @Test
    void testRuleParserRewrite1() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS3.pl");

        ArrayList<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' =-> #i SEM 'event'", true);

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(1, fslist.get(0).annotation.size());
    }

    @Test
    void testRuleParserRewrite2() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS3.pl");

        ArrayList<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist);

        Rule r1 = new Rule("#g TNS-ASP #h =-> 0", true);

        rp.getRules().add(r1);

        rp.addAnnotation2(fslist.get(0));

        assertEquals(420, fslist.get(0).constraints.size());
    }

    @Test
    void testRuleParserRewriteDeleteRemovesOnlyMatchedEdges() {
        GraphConstraint potential = new GraphConstraint(new LinkedHashSet<>(), "1", "POTENTIAL-ANT", "2", "c", false);
        GraphConstraint possible = new GraphConstraint(new LinkedHashSet<>(), "1", "POSSIBLE-ANT", "2", "c", false);

        LinguisticStructure structure = new LinguisticStructure("test-graph", "test graph", new ArrayList<>());
        structure.constraints.add(potential.copy());
        structure.constraints.add(possible.copy());
        structure.annotation.add(potential.copy());
        structure.annotation.add(possible.copy());

        ArrayList<LinguisticStructure> fslist = new ArrayList<>();
        fslist.add(structure);

        RuleParser rp = new RuleParser(fslist);
        rp.getRules().add(new Rule("#a POTENTIAL-ANT #b =-> 0", true));

        rp.addAnnotation2(structure);

        assertEquals(1, structure.constraints.size());
        assertEquals(1, structure.annotation.size());
        assertEquals("POSSIBLE-ANT", structure.constraints.get(0).getRelationLabel());
        assertEquals("POSSIBLE-ANT", structure.annotation.get(0).getRelationLabel());
    }

    @Test
    void testRuleParserRewriteDeleteSupportsEdgeValueAndAttributeValueFilters() {
        GraphConstraint potential = new GraphConstraint(new LinkedHashSet<>(), "1", "POTENTIAL-ANT", "2", "c", false);
        GraphConstraint possible = new GraphConstraint(new LinkedHashSet<>(), "1", "POSSIBLE-ANT", "2", "c", false);
        GraphConstraint tensePast = new GraphConstraint(new LinkedHashSet<>(), "3", "TENSE", "past", "c", false);
        GraphConstraint tensePres = new GraphConstraint(new LinkedHashSet<>(), "3", "TENSE", "present", "c", false);

        LinguisticStructure edgeStructure = new LinguisticStructure("edge-test", "edge test", new ArrayList<>());
        edgeStructure.constraints.add(potential.copy());
        edgeStructure.constraints.add(possible.copy());

        RuleParser edgeParser = new RuleParser(new ArrayList<>(java.util.Collections.singletonList(edgeStructure)));
        edgeParser.getRules().add(new Rule("edge=POTENTIAL-ANT =-> 0", true));
        edgeParser.addAnnotation2(edgeStructure);

        assertEquals(1, edgeStructure.constraints.size());
        assertEquals("POSSIBLE-ANT", edgeStructure.constraints.get(0).getRelationLabel());

        LinguisticStructure valueStructure = new LinguisticStructure("value-test", "value test", new ArrayList<>());
        valueStructure.constraints.add(tensePast.copy());
        valueStructure.constraints.add(tensePres.copy());

        RuleParser valueParser = new RuleParser(new ArrayList<>(java.util.Collections.singletonList(valueStructure)));
        valueParser.getRules().add(new Rule("value=past =-> 0", true));
        valueParser.addAnnotation2(valueStructure);

        assertEquals(1, valueStructure.constraints.size());
        assertEquals("present", valueStructure.constraints.get(0).getFsValue());

        LinguisticStructure tenseStructure = new LinguisticStructure("tense-test", "tense test", new ArrayList<>());
        tenseStructure.constraints.add(tensePast.copy());
        tenseStructure.constraints.add(tensePres.copy());

        RuleParser tenseParser = new RuleParser(new ArrayList<>(java.util.Collections.singletonList(tenseStructure)));
        tenseParser.getRules().add(new Rule("TENSE past =-> 0", true));
        tenseParser.addAnnotation2(tenseStructure);

        assertEquals(1, tenseStructure.constraints.size());
        assertEquals("present", tenseStructure.constraints.get(0).getFsValue());
    }

    @Test
    void testStrip2() {
        assertEquals("sssasssa", HelperMethods.stripValeue2("sssstrip(semform('a',5,[],[]))sssstrip(semform('a',5,[],[]))"));
    }

    @Test
    void testQuestionArrowBranchesOnMatches() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS2.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g !(COMP*>TNS-ASP) #h ?=> #g TMP-DOM #h"));

        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Set.of(structure)));

        assertEquals(6, branches.size());
        assertTrue(branches.stream().allMatch(branch -> branch != null));
    }

    @Test
    void testQuestionDeleteBranchesOnMatches() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS2.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g !(COMP*>TNS-ASP) #h ?-> 0"));

        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Set.of(structure)));

        assertEquals(6, branches.size());
        assertTrue(branches.stream().allMatch(branch -> branch.constraints.size() <= structure.constraints.size()));
    }

    @Test
    void testSequentialBranchingRulesMultiplyBranches() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS2.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g !(COMP*>TNS-ASP) #h ?=> #g TMP-DOM #h"));
        rp.getRules().add(new Rule("#g !(COMP*>TNS-ASP) #h ?=> #g TMP-DOM2 #h"));

        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Set.of(structure)));

        assertEquals(36, branches.size());
    }

    @Test
    void testChoiceVarCopyPreservesNullPropValue() {
        ChoiceVar original = new ChoiceVar("A");
        ChoiceVar copy = original.copy();

        assertEquals("A", copy.choiceID);
        assertEquals(null, copy.propValue);
    }
}
