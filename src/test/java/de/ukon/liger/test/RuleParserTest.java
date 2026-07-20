package de.ukon.liger.test;

import de.ukon.liger.analysis.RuleParser.Rule;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.packing.ChoiceVar;
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
