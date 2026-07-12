package de.ukon.liger.test;

import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.analysis.QueryParser.HierarchyRegistry;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuleParserFileIntegrationTest {

    @Test
    void testRuleFileLoadsTemplatesHierarchiesAndNegation() throws Exception {
        RuleParser ruleParser = new RuleParser(new File(Paths.get("src", "test", "resources", "rules", "rulefile_with_sections.txt").toString()));

        TemplateRegistry templateRegistry = ruleParser.getTemplateRegistry();
        HierarchyRegistry hierarchyRegistry = ruleParser.getHierarchyRegistry();

        assertTrue(templateRegistry.getTemplates().containsKey("SUBJEDGE"));
        assertTrue(hierarchyRegistry.contains("GF"));
        assertEquals(1, ruleParser.getRules().size());

        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        LinguisticStructure structure = fs.get(fs.keySet().iterator().next());

        ruleParser.addAnnotation2(structure);

        assertTrue(structure.annotation.stream()
                .map(GraphConstraint::getRelationLabel)
                .anyMatch("FLAG"::equals));
        assertTrue(structure.annotation.stream()
                .map(GraphConstraint::getFsValue)
                .anyMatch("'ok'"::equals));
    }

}
