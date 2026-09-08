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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

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

        assertEquals(13, fslist.get(0).annotation.size());
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

    @Test
    void testMultilineTemplateDefinitionIsPreserved() throws Exception {
        Path tempFile = Files.createTempFile("liger-multiline-template", ".txt");
        Files.writeString(tempFile,
                "// HIERARCHIES\n" +
                "GF ::= SUBJ > OBJ > OBL .\n\n" +
                "// TEMPLATES\n" +
                "GF := SUBJ | OBJ | OBL .\n" +
                "REFL-BIND(#f,#h) := #f PRON-TYPE 'refl' & @MCN-PATH(#f,#i) &\n" +
                "                    #i ^(@GF) #j !(@GF) #h & superior(GF,#h,#i) .\n\n" +
                "@REFL-BIND(#a,#b) ==> #a TEST #b.\n");

        RuleParser ruleParser = new RuleParser(tempFile.toFile());

        assertTrue(ruleParser.getTemplateRegistry().getTemplates().containsKey("REFL-BIND"));
        assertEquals(1, ruleParser.getRules().size());
    }

    @Test
    @Disabled("Requires merged-graph7.json and liger-test2.liger fixtures, which were never committed and are missing from disk")
    void testQuestionRuleLeftSideDoesNotOvermatchMergedGraph7() throws Exception {
        Path mergedGraph = Paths.get("merged-graph7.json");
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(mergedGraph.toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        RuleParser ruleParser = new RuleParser(new File("liger-test2.liger"));
        String leftSide = "#a ant #a & #a SYNSEM #b & #c ant #c & #c SYNSEM #d &\n"
                + "@DR-PRECEDENCE(#c,#a) & @COARG(#b,#d) & #a POTENTIAL-ANT #e &\n"
                + "#c POTENTIAL-ANT #f & id(#f) != id(#e)";

        QueryParser qp = new QueryParser(leftSide, fs, ruleParser.getTemplateRegistry(), ruleParser.getHierarchyRegistry());
        List<QueryParserResult> results = qp.parseQueryWithTemplates(leftSide);

        int matchCount = results.stream().mapToInt(result -> result.result.size()).sum();
        assertEquals(0, matchCount, () -> "Unexpected matches: " + results.stream()
                .map(result -> result.result.keySet().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",", "[", "]")))
                .collect(Collectors.joining(" | ")));
    }

    @Test
    @Disabled("Requires merged-graph7.json and liger-test2.liger fixtures, which were never committed and are missing from disk")
    void testQuestionRuleInFileDoesNotFireOnMergedGraph7() throws Exception {
        Path mergedGraph = Paths.get("merged-graph7.json");
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(mergedGraph.toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        RuleParser ruleParser = new RuleParser(new File("liger-test2.liger"));
        LinkedHashSet<LinguisticStructure> input = new LinkedHashSet<>();
        input.add(fs);

        ruleParser.addAnnotation2(input);

        int offendingRuleIndex = -1;
        for (int i = 0; i < ruleParser.getRules().size(); i++) {
            if (ruleParser.getRules().get(i).getRight().contains("POSSIBLE-ANT")
                    && ruleParser.getRules().get(i).getOperator().equals("?=>")) {
                offendingRuleIndex = i;
                break;
            }
        }

        assertTrue(offendingRuleIndex >= 0, "Could not locate the question rule");
        LinkedHashSet<GraphConstraint> annotations = ruleParser.getAddedAnnotationsByRule().get(offendingRuleIndex);
        assertTrue(annotations == null || annotations.isEmpty(),
                "Rule fired with annotations: " + annotations);
    }

}
