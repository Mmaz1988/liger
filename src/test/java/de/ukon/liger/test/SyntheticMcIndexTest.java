package de.ukon.liger.test;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The source index a meaning constructor carries ({@code [k]}, which becomes the DRS
 * referent's {@code SRC}) must be the node's {@code SYN-ID} -- not its position in whatever
 * order the meaning-constructor nodes happen to be visited in.
 *
 * The two used to be independent: {@code SYN-ID}s were assigned in c-structure traversal
 * order, while the source index was a positional counter over an ordering that silently
 * degraded to node-name order for any structure that was not an {@link
 * de.ukon.liger.syntax.xle.Fstructure} -- i.e. for every structure supplied back to LiGER
 * as JSON. The fixtures below deliberately give the nodes names that sort in the opposite
 * order to their {@code SYN-ID}s, so a positional counter cannot pass.
 */
public class SyntheticMcIndexTest {

    private static GraphConstraint constraint(String node, String label, String value) {
        GraphConstraint constraint = new GraphConstraint();
        constraint.setFsNode(node);
        constraint.setRelationLabel(label);
        constraint.setFsValue(value);
        constraint.setReading(new LinkedHashSet<>(Set.of(new ChoiceVar("1"))));
        return constraint;
    }

    /** Two glue nodes whose names sort as g11 &lt; g4, numbered the other way round. */
    private static LinguisticStructure structure(String firstSynId, String secondSynId) {
        List<GraphConstraint> constraints = new ArrayList<>();
        constraints.add(constraint("s", "in_set", "g11"));
        constraints.add(constraint("s", "in_set", "g4"));
        constraints.add(constraint("g4", "SYN-ID", firstSynId));
        constraints.add(constraint("g11", "SYN-ID", secondSynId));
        constraints.add(constraint("g4", "MEANING", "'kim'"));
        constraints.add(constraint("g4", "RESOURCE", "'f4'"));
        constraints.add(constraint("g4", "TYPE", "'e'"));
        constraints.add(constraint("g11", "MEANING", "'sleep'"));
        constraints.add(constraint("g11", "RESOURCE", "'f11'"));
        constraints.add(constraint("g11", "TYPE", "'t'"));
        return new LinguisticStructure("test", "kim sleeps", constraints);
    }

    private static List<String> meaningConstructors(LinguisticStructure fs) {
        Map<Set<ChoiceVar>, Set<String>> semantics = new GlueSemantics().translateMeaningConstructors(fs);
        return semantics.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Test
    void sourceIndexFollowsSynIdRatherThanNodeNameOrder() {
        List<String> mcs = meaningConstructors(structure("i1", "i2"));

        assertEquals(2, mcs.size(), mcs.toString());
        assertTrue(mcs.stream().anyMatch(mc -> mc.startsWith("[1] ") && mc.contains("kim")), mcs.toString());
        assertTrue(mcs.stream().anyMatch(mc -> mc.startsWith("[2] ") && mc.contains("sleep")), mcs.toString());
    }

    /**
     * A structure that is part of a sequence carries {@code SYN-ID}s shifted by the offset of
     * the parts before it. Its meaning constructors must carry the shifted numbers too --
     * this is what lets a sentence be parsed once, on its own, and later merged into a
     * discourse without being re-parsed.
     */
    @Test
    void sourceIndexFollowsShiftedSynIdsOfASequencePart() {
        List<String> mcs = meaningConstructors(structure("i18", "i19"));

        assertEquals(2, mcs.size(), mcs.toString());
        assertTrue(mcs.stream().anyMatch(mc -> mc.startsWith("[18] ") && mc.contains("kim")), mcs.toString());
        assertTrue(mcs.stream().anyMatch(mc -> mc.startsWith("[19] ") && mc.contains("sleep")), mcs.toString());
    }

    /** A supplied structure's numbering is its identity -- it must survive untouched. */
    @Test
    void existingSynIdsAreNotRenumbered() {
        LinguisticStructure fs = structure("i18", "i19");

        new GlueSemantics().annotateSyntheticMcIndices(fs);

        Map<String, String> synIds = fs.constraints.stream()
                .filter(constraint -> "SYN-ID".equals(constraint.getRelationLabel()))
                .collect(Collectors.toMap(GraphConstraint::getFsNode,
                        constraint -> String.valueOf(constraint.getFsValue())));
        assertEquals(Map.of("g4", "i18", "g11", "i19"), synIds);
    }
}
