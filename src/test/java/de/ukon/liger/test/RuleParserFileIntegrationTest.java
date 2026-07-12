package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.RuleParser.Rule;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.analysis.QueryParser.HierarchyRegistry;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.utilities.PathVariables;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void testRuleParser4() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS4.pl");

        List<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist, Paths.get(PathVariables.testPath, "testRules.txt"));

        rp.addAnnotation2(fslist.get(0));

        assertEquals(11, fslist.get(0).annotation.size());
    }

    @Test
    void testRuleParser5a() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS2.pl");

        for (String key : fs.keySet()) {
            QueryParser qp = new QueryParser("#g !(COMP*>TNS-ASP) #h", fs.get(key));
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            RuleParser rp = new RuleParser(new ArrayList<>());

            Rule r = new Rule("#g !(COMP*>TNS-ASP) #h ==> #g TMP-DOM #h");

            rp.getRules().add(r);

            rp.addAnnotation2(fs.get(key));

            assertEquals(6, fs.get(key).annotation.size());

            assertEquals(6, qpr.result.keySet().size());
        }
    }

    @Test
    void testRuleParser7() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS4.pl");

        List<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist, Paths.get(PathVariables.testPath, "testRules2.txt"));

        rp.addAnnotation2(fslist.get(0));

        assertEquals(11, fslist.get(0).annotation.size());
    }

    @Test
    void testRuleParser8() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS4.pl");

        List<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist, Paths.get(PathVariables.testPath, "testRules3.txt"));

        rp.addAnnotation2(fslist.get(0));

        assertEquals(14, fslist.get(0).annotation.size());
    }

    @Test
    void testQueryParser15() {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS11.pl");

        List<LinguisticStructure> fslist = new ArrayList<>(fs.values());

        RuleParser rp = new RuleParser(fslist);
        rp.setReplace(true);

        Rule r1 = new Rule("#g ADJUNCT #h & #h in_set #i & #g TNS-ASP #j ==> #i ADJ-SEM '#g -o #i'.");
        Rule r2 = new Rule("#g ADJUNCT #h & #h in_set #i & #g NTYPE #a ==> #i ADJ-SEM '#g -o #i'.");

        rp.getRules().add(r1);
        rp.getRules().add(r2);

        String key = fs.keySet().stream().findAny().get();

        rp.addAnnotation2(fs.get(key));

        assertEquals(2, fs.get(key).annotation.size());
    }

}
