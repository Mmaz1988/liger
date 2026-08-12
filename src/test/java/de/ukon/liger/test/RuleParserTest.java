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
    void testRegularRuleAddedFactsAreAssociatedWithTheStructure() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS3.pl");
        LinguisticStructure structure = fs.values().iterator().next();
        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g TNS-ASP #h TENSE 'past' & #h PERF '-_' ==> #h SEM 'event'"));

        rp.addAnnotation2(structure);

        assertEquals(1, rp.getAddedAnnotationsByRule(structure).get(0).size());
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
    void testAddedFactsAreAssociatedWithTheirBranch() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS2.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g !(COMP*>TNS-ASP) #h ?=> #g TMP-DOM #h"));

        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Set.of(structure)));

        assertEquals(6, rp.getAddedAnnotationsByRule().get(0).size());
        assertTrue(branches.stream().allMatch(branch ->
                rp.getAddedAnnotationsByRule(branch).get(0).size() == 1));
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

    /**
     * testdirS1.pl has two nodes carrying both TENSE 'past' and PERF '-_', so the left-hand side has
     * four solutions. The right-hand side only reads #g, which takes two values across them -- the
     * other two solutions repeat an annotation that was already emitted.
     */
    @Test
    void testEquivalentSolutionsAreConflatedIntoOneAnnotation() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g TENSE 'past' & #h PERF '-_' ==> #g LINK #g"));

        rp.addAnnotation2(structure);

        assertEquals(2, structure.annotation.size());
        assertEquals(2, new LinkedHashSet<>(structure.annotation).size());
    }

    /**
     * The conflated duplicates used to survive as a copy tagged with a hard-coded "X1" choice, which
     * downstream consumers read as a genuine additional reading.
     */
    @Test
    void testConflatedSolutionsDoNotLeaveAPhantomReading() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g TENSE 'past' & #h PERF '-_' ==> #g LINK #g"));

        rp.addAnnotation2(structure);

        assertTrue(structure.annotation.stream()
                .flatMap(constraint -> constraint.getReading().stream())
                .noneMatch(choice -> "X1".equals(choice.choiceID)));
    }

    /**
     * A right-hand side variable the left-hand side never bound means the rule introduces a node for
     * each match, so every solution is entitled to its own. These must never be conflated, however
     * alike the resulting facts look.
     */
    @Test
    void testSolutionsIntroducingNewNodesAreNotConflated() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g TENSE 'past' & #h PERF '-_' ?=> #z KEEP +"));

        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Set.of(structure)));

        assertEquals(4, branches.size());

        Set<String> introducedNodes = branches.stream()
                .flatMap(branch -> branch.annotation.stream())
                .filter(constraint -> "KEEP".equals(constraint.getRelationLabel()))
                .map(GraphConstraint::getFsNode)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        assertEquals(4, introducedNodes.size());
    }

    /**
     * Branching rules whose right-hand side is fully bound conflate like any other: two solutions
     * that would fork off identical branches fork off one.
     */
    @Test
    void testBranchingRuleConflatesEquivalentSolutions() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g TENSE 'past' & #h PERF '-_' ?=> #g KEEP +"));

        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Set.of(structure)));

        assertEquals(2, branches.size());
    }

    /**
     * "#g GEND-SEM 'male'" matches exactly one node in testdirS1.pl (John), so the left-hand side has
     * exactly one solution -- isolating whether one solution's introduced node is shared across the
     * right-hand side's conjuncts from whether distinct solutions get distinct nodes (already covered
     * by {@link #testSolutionsIntroducingNewNodesAreNotConflated}).
     */
    @Test
    void testIntroducedNodeIsSharedAcrossRightHandSideConjuncts() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g GEND-SEM 'male' ?=> #z KEEP + & #z MARK 'x'"));

        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Set.of(structure)));

        assertEquals(1, branches.size());

        LinguisticStructure branch = branches.iterator().next();
        Set<String> introduced = branch.annotation.stream()
                .filter(c -> "KEEP".equals(c.getRelationLabel()) || "MARK".equals(c.getRelationLabel()))
                .map(GraphConstraint::getFsNode)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        assertEquals(1, introduced.size());
    }

    /**
     * The same, for the accumulating operator.
     */
    @Test
    void testIntroducedNodeIsSharedAcrossConjunctsForRegularRules() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g GEND-SEM 'male' ==> #z KEEP + & #z MARK 'x'"));

        rp.addAnnotation2(structure);

        Set<String> introduced = structure.annotation.stream()
                .filter(c -> "KEEP".equals(c.getRelationLabel()) || "MARK".equals(c.getRelationLabel()))
                .map(GraphConstraint::getFsNode)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        assertEquals(1, introduced.size());
    }

    /**
     * An introduced node used in a value position is the same node as in a node position.
     */
    @Test
    void testIntroducedNodeIsSharedBetweenNodeAndValuePositions() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        LinguisticStructure structure = fs.values().iterator().next();

        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.getRules().add(new Rule("#g GEND-SEM 'male' ?=> #z KEEP + & #g INTRODUCED #z"));

        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Set.of(structure)));

        assertEquals(1, branches.size());
        LinguisticStructure branch = branches.iterator().next();

        String introducedNode = branch.annotation.stream()
                .filter(c -> "KEEP".equals(c.getRelationLabel()))
                .map(GraphConstraint::getFsNode)
                .findFirst().orElseThrow();

        String referencedNode = branch.annotation.stream()
                .filter(c -> "INTRODUCED".equals(c.getRelationLabel()))
                .map(c -> String.valueOf(c.getFsValue()))
                .findFirst().orElseThrow();

        assertEquals(introducedNode, referencedNode);
    }

    @Test
    void testChoiceVarCopyPreservesNullPropValue() {
        ChoiceVar original = new ChoiceVar("A");
        ChoiceVar copy = original.copy();

        assertEquals("A", copy.choiceID);
        assertEquals(null, copy.propValue);
    }
}
