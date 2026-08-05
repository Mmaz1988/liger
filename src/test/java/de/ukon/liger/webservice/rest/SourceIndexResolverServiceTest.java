package de.ukon.liger.webservice.rest;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerSourceSpan;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceIndexResolverServiceTest {
    private final SourceIndexResolverService resolver = new SourceIndexResolverService();

    @Test
    void resolvesSynIdThroughCprojToSurfaceSpan() {
        LinguisticStructure structure = structure(List.of(
                edge("mc1", "SYN-ID", "i1"),
                edge("glue1", "in_set", "mc1"),
                edge("g1", "GLUE", "glue1"),
                edge("f1", "g::", "g1"),
                edge("c1", "cproj", "f1"),
                edge("c1", "token", "'Kim'"),
                edge("c1", "start", "int(5)"),
                edge("c1", "end", "int(8)")));

        LigerSourceSpan span = resolver.resolve(structure, List.of(1));

        assertEquals("Kim", span.text);
        assertEquals(5, span.start);
        assertEquals(8, span.end);
    }

    @Test
    void combinesSourceIndicesInSurfaceOrder() {
        LinguisticStructure structure = structure(List.of(
                edge("mc1", "SYN-ID", "i1"),
                edge("glue1", "in_set", "mc1"),
                edge("g1", "GLUE", "glue1"),
                edge("f1", "g::", "g1"),
                edge("c1", "cproj", "f1"),
                edge("c1", "token", "'dog'"),
                edge("c1", "start", "int(2)"),
                edge("c1", "end", "int(5)"),
                edge("mc2", "SYN-ID", "i2"),
                edge("glue2", "in_set", "mc2"),
                edge("g2", "GLUE", "glue2"),
                edge("f2", "g::", "g2"),
                edge("c2", "cproj", "f2"),
                edge("c2", "token", "'a'"),
                edge("c2", "start", "int(0)"),
                edge("c2", "end", "int(1)")));

        LigerSourceSpan span = resolver.resolve(structure, List.of(1, 2));

        assertEquals("a dog", span.text);
        assertEquals(0, span.start);
        assertEquals(5, span.end);
    }

    private LinguisticStructure structure(List<GraphConstraint> constraints) {
        return new LinguisticStructure("test", "a dog", new ArrayList<>(constraints));
    }

    private GraphConstraint edge(String source, String relation, String target) {
        return new GraphConstraint(Set.of(), source, relation, target, "f", false);
    }
}
