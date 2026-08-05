package de.ukon.liger.syntax.xle.prolog2java;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FsProlog2JavaTest {

    @Test
    void tagsGlueSemanticNodesWithGProjection() {
        PathVariables.initializePathVariables();

        ReadFsProlog fs = ReadFsProlog.readPrologFile(
                new File(PathVariables.testPath + "fs11.pl"),
                new VariableHandler());

        List<GraphConstraint> constraints = FsProlog2Java.fs2List(fs);

        assertTrue(constraints.stream().anyMatch(c -> "GLUE".equals(c.getRelationLabel()) && "g".equals(c.getProj())));
        assertTrue(constraints.stream().anyMatch(c -> "ANT".equals(c.getRelationLabel()) && "g".equals(c.getProj())));
        assertTrue(constraints.stream().anyMatch(c -> "CONS".equals(c.getRelationLabel()) && "g".equals(c.getProj())));
        assertTrue(constraints.stream().anyMatch(c -> "RESOURCE".equals(c.getRelationLabel()) && "g".equals(c.getProj())));

        assertFalse(constraints.stream().anyMatch(c -> Set.of("GLUE", "ANT", "CONS", "RESOURCE").contains(c.getRelationLabel())
                && !"g".equals(c.getProj())));

        assertTrue(constraints.stream().anyMatch(c -> "f".equals(c.getProj())
                && c.getFsNode().startsWith("f")));
        assertTrue(constraints.stream().anyMatch(c -> "c".equals(c.getProj())
                && c.getFsNode().startsWith("c")));
        assertTrue(constraints.stream().anyMatch(c -> "left".equals(c.getRelationLabel())
                && c.getFsNode().startsWith("c")
                && String.valueOf(c.getFsValue()).startsWith("c")));
        assertTrue(constraints.stream().anyMatch(c -> "phi".equals(c.getRelationLabel())
                && c.getFsNode().startsWith("c")
                && String.valueOf(c.getFsValue()).startsWith("f")));
    }
}
