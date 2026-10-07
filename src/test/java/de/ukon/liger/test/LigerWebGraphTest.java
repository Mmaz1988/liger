package de.ukon.liger.test;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.webservice.rest.dtos.LigerGraphComponent;
import de.ukon.liger.webservice.rest.dtos.LigerWebGraph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LigerWebGraphTest {

    private static GraphConstraint fact(String node, String label, String value) {
        GraphConstraint c = new GraphConstraint();
        c.setReading(Set.of(new ChoiceVar("1")));
        c.setFsNode(node);
        c.setRelationLabel(label);
        c.setFsValue(value);
        return c;
    }

    private static Map<?, ?> avpOf(LigerWebGraph graph, String id) {
        for (LigerGraphComponent component : graph.graphElements) {
            if (id.equals(component.data.get("id")) && !component.data.containsKey("source")) {
                return (Map<?, ?>) component.data.get("avp");
            }
        }
        return null;
    }

    /**
     * A rule-added attribute on a node the parse already has used to vanish: the annotation side of
     * the node was dropped whole because the node id already existed.
     */
    @Test
    void ruleAddedAttributeOnAnExistingNodeIsShown() {
        List<GraphConstraint> syntax = List.of(
                fact("d15", "NODE_TYPE", "condition"),
                fact("d15", "NAME", "bind"));
        List<GraphConstraint> annotation = List.of(
                fact("d15", "STATUS", "'acc'"),
                fact("d99", "NAME", "'new'"));

        LigerWebGraph graph = new LigerWebGraph(syntax, annotation);

        Map<?, ?> existing = avpOf(graph, "d15");
        assertNotNull(existing);
        assertEquals("bind", existing.get("NAME"));
        assertEquals("'acc'", existing.get("STATUS"));
        assertEquals(1, graph.graphElements.stream().filter(c -> "d15".equals(c.data.get("id"))).count());
        assertNotNull(avpOf(graph, "d99"));
    }
}
