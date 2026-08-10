package de.ukon.liger.syntax.xle.prolog2java;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.syntax.GraphConstraint;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for FsProlog2Java's Glue-resource node classification (classifyGlueNodes):
 * a variable gets a synthetic g&lt;N&gt; identity only if it is registered via an
 * ANT/CONS/RESOURCE/GLUE edge or a "g::" projection AND never independently appears with
 * structure of its own; otherwise it keeps its native f&lt;N&gt; identity everywhere it's
 * referenced, even where cited as a resource. The first two cases reproduce a bug where a purely
 * self-descriptive TYPE fact was minted as a disconnected f&lt;N&gt; twin instead of anchoring at
 * its existing g&lt;N&gt; id (liger_resources/testFiles/fs11.pl:89-95: var 27's ANT/CONS edges
 * register vars 28/29 as glue resource nodes with no other role, so their RESOURCE/TYPE facts
 * must anchor at g28/g29). The third case is the opposite shape
 * (liger_resources/testFiles/hybrid_glue_test.pl: var 22 is a real s::-projected sigma node with
 * its own SIT/TEMP-REF sub-structure that is *also* cited as a RESOURCE elsewhere) and must keep
 * its native f22 identity consistently, including at the RESOURCE edge that cites it.
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

    @Test
    void aliasedResourceNodeKeepsNativeIdentityEvenWhenCitedAsResourceElsewhere() {
        // Verbatim (minus trailing commas) from hybrid_glue_test.pl:29,42-43,119: var 22 is
        // XLE's own s::-projected sigma node for a TNS-ASP eventuality, with real sub-structure
        // of its own (SIT, TEMP-REF) -- and the same variable is separately cited as a RESOURCE
        // by var 42's meaning constructor, because this grammar's glue formulas literally reuse
        // the eventuality's own sigma-node variable as its resource. var 22 must stay f22
        // everywhere, including as the RESOURCE edge's target -- not become g22 just because it's
        // cited as a resource, and not split into f22-here/g22-there depending on which fact is
        // consulted.
        List<String> facts = List.of(
                "cf(1,eq(proj(var(0),'s::'),var(22)))",
                "cf(1,eq(attr(var(22),'SIT'),var(23)))",
                "cf(1,eq(attr(var(22),'TEMP-REF'),var(8)))",
                "cf(1,eq(attr(var(42),'RESOURCE'),var(22)))"
        );

        List<GraphConstraint> result = convert(facts);

        assertTrue(hasConstraint(result, "f0", "s::", "f22"));
        assertTrue(hasConstraint(result, "f22", "SIT", "f23"));
        assertTrue(hasConstraint(result, "f22", "TEMP-REF", "f8"));
        assertTrue(hasConstraint(result, "f42", "RESOURCE", "f22"),
                "var 22 has independent structure of its own, so even the RESOURCE edge citing it "
                        + "must point at its native f22 identity, not a synthetic g22 copy");
        assertFalse(hasNode(result, "g22"), "var 22 must never appear as g22 anywhere in the output");
    }
}
