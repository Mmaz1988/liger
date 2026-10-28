package de.ukon.liger.webservice.rest;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerSequencePartResult;
import de.ukon.liger.webservice.rest.dtos.LigerSequenceRequest;
import de.ukon.liger.webservice.rest.dtos.LigerSolutionAnnotation;
import de.ukon.liger.webservice.rest.dtos.LigerSolutionAnnotationResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One solution per rule branch (xleplusglue docs/plans/UD_PARSING_PLAN.md, section 7): a parse may
 * have several semantics. Each branch of a {@code ?+=>}/{@code ?*=>} rule is a solution with its
 * own key, structure and meaning constructors; branches with the same constructors are one.
 */
class RuleBranchSolutionsTest {

    /** The subject "dog" gets two sites, nodes as a quantifier's scope sites are. */
    private static final String SITES = "--replace(true);\n#v NSUBJ #n ==> #n SITE #a & #n SITE #b.\n";

    /** Each branch picks a site, and "dog" scopes there: one meaning constructor per branch. */
    private static final String DISTINCT = SITES
            + "#n SITE #s ?+=> #n PICK #s.\n"
            + "#n PICK #s ==> #n SEM #m & #m GLUE (\\P.P) : ((#n_e -o #s_t) -o #s_t).\n";

    /** The branches differ in a fact that makes no constructor. */
    private static final String SAME = SITES
            + "#n SITE #s ?+=> #n NOTE #s.\n"
            + "#v NSUBJ #n ==> #n SEM #m & #m GLUE (\\x.([],[dog(x)])) : (#n_e -o #n_t).\n";

    private static GraphConstraint fact(String node, String label, String value) {
        GraphConstraint constraint = new GraphConstraint();
        constraint.setFsNode(node);
        constraint.setRelationLabel(label);
        constraint.setFsValue(value);
        constraint.setReading(new LinkedHashSet<>(Set.of(new ChoiceVar("1"))));
        return constraint;
    }

    private static void word(List<GraphConstraint> facts, String node, String form, int start) {
        facts.add(fact(node, "TEXT", form));
        facts.add(fact(node, "token", form));
        facts.add(fact(node, "start", "int(" + start + ")"));
        facts.add(fact(node, "end", "int(" + (start + form.length()) + ")"));
    }

    /** "A dog barked." as the Stanza service gives it. */
    private static LinguisticStructure aDogBarked() {
        List<GraphConstraint> words = new ArrayList<>();
        word(words, "w1", "A", 1);
        word(words, "w2", "dog", 3);
        word(words, "w3", "barked", 7);
        words.add(fact("w2", "DET", "w1"));
        words.add(fact("w3", "NSUBJ", "w2"));
        words.add(fact("w0", "ROOT", "w3"));
        LinguisticStructure structure = new LinguisticStructure("S0", "A dog barked.", words, new ChoiceSpace());
        structure.cp.choices.add(new LinkedHashSet<>(Set.of(new ChoiceVar("1"))));
        structure.syntaxType = LinguisticStructure.UD;
        return structure;
    }

    private static LigerController.SentenceParser parser() {
        return LigerController.SentenceParser.supplying(List.of(aDogBarked()));
    }

    @Test
    void everyBranchIsASolutionWithItsOwnKeyAndConstructors() throws IOException {
        List<LigerSolutionAnnotation> solutions = new LigerController()
                .annotateSentence(null, "A dog barked.", DISTINCT, parser()).solutions();

        assertEquals(2, solutions.size());
        assertEquals("S0", solutions.get(0).solutionKey, "the first branch keeps the analysis's key");
        assertEquals("S0-b2", solutions.get(1).solutionKey);
        assertNotEquals(solutions.get(0).meaningConstructors, solutions.get(1).meaningConstructors);
        assertNotEquals(solutions.get(0).structureJson, solutions.get(1).structureJson);
        assertEquals("S0-b2", solutions.get(1).sentenceAnalysis.syntax.get(0).synId);
    }

    @Test
    void branchesWithTheSameConstructorsAreOneSolution() throws IOException {
        List<LigerSolutionAnnotation> solutions = new LigerController()
                .annotateSentence(null, "A dog barked.", SAME, parser()).solutions();

        assertEquals(1, solutions.size());
        assertEquals("S0", solutions.get(0).solutionKey);
    }

    @Test
    void aSequenceParsedHereGivesEveryBranchItsOwnPart() throws IOException {
        LigerSequenceRequest request = new LigerSequenceRequest();
        request.sentences = List.of("A dog barked.", "A dog barked.");
        request.ruleString = DISTINCT;

        LigerSolutionAnnotationResponse response = new LigerController().applyRuleRequestXLESequence(request, parser());

        // 2 branches x 2 branches, every combination once, parts numbered across the call.
        assertEquals(4, response.solutions.size());
        Set<String> combinations = new LinkedHashSet<>();
        for (LigerSolutionAnnotation solution : response.solutions) {
            List<LigerSequencePartResult> parts = solution.sequenceParts;
            assertEquals(2, parts.size());
            assertTrue(Set.of("S0", "S1").contains(parts.get(0).syntaxVariantId()), parts.toString());
            assertTrue(Set.of("S2", "S3").contains(parts.get(1).syntaxVariantId()), parts.toString());
            combinations.add(parts.get(0).syntaxVariantId() + "+" + parts.get(1).syntaxVariantId());
        }
        assertEquals(4, combinations.size(), combinations.toString());
    }
}
