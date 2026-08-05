package de.ukon.liger.syntax;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeIdPolicyTest {

    private final NodeIdPolicy policy = NodeIdPolicy.defaults();

    @Test
    void recognizesConfiguredTypedNodeIds() {
        assertTrue(policy.isNodeId("f1"));
        assertTrue(policy.isNodeId("c27"));
        assertTrue(policy.isNodeId("d3"));
        assertTrue(policy.isNodeId("g4"));
        assertTrue(policy.isNodeId("a5"));
    }

    @Test
    void mapsConfiguredIdsToClientNodeTypes() {
        assertEquals("input", policy.defaultNodeTypeOf("f1").orElseThrow());
        assertEquals("cnode", policy.defaultNodeTypeOf("c1").orElseThrow());
        assertEquals("dnode", policy.defaultNodeTypeOf("d1").orElseThrow());
        assertEquals("gnode", policy.defaultNodeTypeOf("g1").orElseThrow());
        assertEquals("annotation", policy.defaultNodeTypeOf("a1").orElseThrow());
    }

    @Test
    void leavesUnconfiguredAlphanumericValuesAsValues() {
        assertFalse(policy.isNodeId("x1"));
        assertFalse(policy.isNodeId("i4"));
        assertFalse(policy.isNodeId("val:foo"));
        assertFalse(policy.isNodeId("42"));
        assertFalse(policy.isNodeId("dog"));
        assertFalse(policy.isNodeId("dnode"));
    }

    @Test
    void supportsCustomNamespaces() {
        NodeIdPolicy custom = new NodeIdPolicy(Map.of(
                "x", new NodeIdPolicy.Namespace("external", "external-node")
        ));

        assertTrue(custom.isNodeId("x1"));
        assertEquals("external-node", custom.defaultNodeTypeOf("x1").orElseThrow());
        assertFalse(custom.isNodeId("d1"));
    }

    @Test
    void distinguishesLegacyNumericIdsWithoutTreatingThemAsTypedIds() {
        assertTrue(policy.isLegacyNumericId("001"));
        assertFalse(policy.isNodeId("001"));
        assertFalse(policy.isLegacyNumericId("d1"));
    }
}
