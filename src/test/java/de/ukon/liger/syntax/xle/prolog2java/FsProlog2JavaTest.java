package de.ukon.liger.syntax.xle.prolog2java;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.syntax.GraphConstraint;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for a bug where fs2List's "terminals" branch (atomic leaf attribute facts
 * such as TYPE/MEANING/NOSCOPE/INSITU) unconditionally minted a fresh f&lt;N&gt; node instead of
 * reusing the g&lt;N&gt; id already assigned to that variable by an earlier ANT/CONS/RESOURCE/GLUE
 * or "g::" registering edge -- leaving an orphaned, disconnected f&lt;N&gt; twin of the real,
 * anchored g&lt;N&gt; resource node. Facts below are taken verbatim (minus trailing commas) from
 * liger_resources/testFiles/fs11.pl:89-95, which reproduces the reported shape (var 27's ANT/CONS
 * edges register vars 28/29 as glue resource nodes; their own RESOURCE/TYPE facts must then be
 * anchored at g28/g29, not f28/f29).
 */
class FsProlog2JavaTest {

    private static List<GraphConstraint> convert(List<String> facts) {
        ReadFsProlog fs = new ReadFsProlog("s1", "test sentence", facts, Collections.emptyList(), new ChoiceSpace(), null);
        return FsProlog2Java.fs2List(fs);
    }

    private static boolean hasConstraint(List<GraphConstraint> constraints, String fsNode,
                                          String relation, String fsValue) {
        return constraints.stream().anyMatch(c -> fsNode.equals(c.getFsNode())
                && relation.equals(c.getRelationLabel())
                && fsValue.equals(String.valueOf(c.getFsValue())));
    }

    private static boolean hasNode(List<GraphConstraint> constraints, String fsNode) {
        return constraints.stream().anyMatch(c -> fsNode.equals(c.getFsNode())
                || fsNode.equals(String.valueOf(c.getFsValue())));
    }

    @Test
    void typeFactAnchoredAtGlueNodeWhenRegisteringEdgeComesFirst() {
        List<String> facts = List.of(
                "cf(1,eq(attr(var(27),'ANT'),var(28)))",
                "cf(1,eq(attr(var(27),'CONS'),var(29)))",
                "cf(1,eq(attr(var(28),'RESOURCE'),var(13)))",
                "cf(1,eq(attr(var(28),'TYPE'),'v'))",
                "cf(1,eq(attr(var(29),'RESOURCE'),var(13)))",
                "cf(1,eq(attr(var(29),'TYPE'),'t'))"
        );

        List<GraphConstraint> result = convert(facts);

        assertTrue(hasConstraint(result, "f27", "ANT", "g28"));
        assertTrue(hasConstraint(result, "f27", "CONS", "g29"));
        assertTrue(hasConstraint(result, "g28", "TYPE", "'v'"));
        assertTrue(hasConstraint(result, "g29", "TYPE", "'t'"));
        assertFalse(hasNode(result, "f28"), "var 28 is a glue resource node and must not appear as f28");
        assertFalse(hasNode(result, "f29"), "var 29 is a glue resource node and must not appear as f29");
    }

    @Test
    void typeFactAnchoredAtGlueNodeEvenWhenRegisteringEdgeComesAfter() {
        // Same facts as above, but the TYPE fact for var 28 appears BEFORE the ANT edge that
        // registers var 28 as a glue node. A single forward-pass fix would still see an empty
        // glueNodes set at this point and wrongly emit f28; the two-pass fix pre-scans the whole
        // fact list first, so this must resolve to g28 regardless of order.
        List<String> facts = List.of(
                "cf(1,eq(attr(var(28),'TYPE'),'v'))",
                "cf(1,eq(attr(var(27),'ANT'),var(28)))"
        );

        List<GraphConstraint> result = convert(facts);

        assertTrue(hasConstraint(result, "g28", "TYPE", "'v'"));
        assertTrue(hasConstraint(result, "f27", "ANT", "g28"));
        assertFalse(hasNode(result, "f28"), "var 28 is a glue resource node and must not appear as f28");
    }
}
