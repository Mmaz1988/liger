package de.ukon.liger.semantics;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.Fstructure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Regression test for a merge bug where a leaf node reached only via a projection edge
 * (e.g. the EV node hanging off the sigma/"s::" projection of the f-structure root) kept
 * its original, un-rebased ID across merged structures, causing two unrelated sentences'
 * event variables to collapse onto the same node ID. Mirrors the real "a man appeared" /
 * "he smiled" sequence-merge shape: ROOT --SUBJ--> ARG, ROOT --s::--> SIGMA, SIGMA --EV--> EV.
 */
class SequenceGraphAssemblerTest {

    private static GraphConstraint constraint(String fsNode, String relation, String fsValue, boolean root) {
        return new GraphConstraint(new LinkedHashSet<>(), fsNode, relation, fsValue, "f", root);
    }

    private static LinguisticStructure sentence(String localId, String text, String pred) {
        List<GraphConstraint> constraints = new ArrayList<>();
        constraints.add(constraint("f1", "SUBJ", "f2", true));
        constraints.add(constraint("f2", "PRED", "'" + pred + "'", false));
        constraints.add(constraint("f1", "s::", "f4", false));
        constraints.add(constraint("f4", "EV", "f5", false));
        return new LinguisticStructure(localId, text, constraints, new ChoiceSpace());
    }

    @Test
    void evNodeUnderSigmaProjectionIsRebasedDistinctlyPerStructure() {
        LinguisticStructure sentence1 = sentence("s1", "a man appeared.", "man");
        LinguisticStructure sentence2 = sentence("s2", "he smiled.", "he");

        Fstructure merged = SequenceGraphAssembler.assemble(List.of(sentence1, sentence2));

        List<GraphConstraint> evConstraints = merged.constraints.stream()
                .filter(c -> "EV".equals(c.getRelationLabel()))
                .toList();
        assertEquals(2, evConstraints.size());

        String ev1 = String.valueOf(evConstraints.get(0).getFsValue());
        String ev2 = String.valueOf(evConstraints.get(1).getFsValue());
        assertNotEquals(ev1, ev2,
                "EV nodes from two different source sentences must not collapse onto the same ID");

        // The EV node's source (the sigma node reached via s::) must stay in sync with its own
        // rebasing, i.e. the EV constraint's source node must equal the s:: constraint's target.
        List<GraphConstraint> sigmaConstraints = merged.constraints.stream()
                .filter(c -> "s::".equals(c.getRelationLabel()))
                .toList();
        assertEquals(2, sigmaConstraints.size());
        assertEquals(String.valueOf(sigmaConstraints.get(0).getFsValue()), evConstraints.get(0).getFsNode());
        assertEquals(String.valueOf(sigmaConstraints.get(1).getFsValue()), evConstraints.get(1).getFsNode());

        // SUBJ/root-level nodes must still be rebased distinctly, as before this fix.
        List<GraphConstraint> subjConstraints = merged.constraints.stream()
                .filter(c -> "SUBJ".equals(c.getRelationLabel()))
                .toList();
        assertEquals(2, subjConstraints.size());
        assertNotEquals(subjConstraints.get(0).getFsNode(), subjConstraints.get(1).getFsNode());
    }
}
