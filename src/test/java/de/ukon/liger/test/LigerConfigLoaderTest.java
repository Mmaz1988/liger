package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryTemplate;
import de.ukon.liger.analysis.RuleParser.Rule;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.analysis.config.ConfigTree;
import de.ukon.liger.analysis.config.LigerConfig;
import de.ukon.liger.analysis.config.LigerConfigException;
import de.ukon.liger.analysis.config.LigerConfigLoader;
import de.ukon.liger.analysis.config.ResolvedRuleSet;
import de.ukon.liger.analysis.lexicon.LexiconRegistry;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class LigerConfigLoaderTest {

    @TempDir
    Path dir;

    private Path write(String name, String content) throws IOException {
        Path file = dir.resolve(name);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
        return file;
    }

    @Test
    void resolvesPathsAgainstTheConfigDirectory() throws IOException {
        write("rules/a.liger", "");
        write("rules/sub/b.liger", "");
        write("dicts/d.txt", "");
        Path config = write("rules/test.liger.yaml", """
                semantics:
                  - a.liger
                pragmatics:
                  - sub/b.liger
                  - a.liger
                lexica:
                  - ../dicts/d.txt
                """);

        LigerConfig loaded = LigerConfigLoader.load(config);

        assertEquals(List.of("a.liger"), loaded.semantics().stream().map(p -> p.getFileName().toString()).toList());
        assertEquals(List.of("b.liger", "a.liger"), loaded.pragmatics().stream().map(p -> p.getFileName().toString()).toList());
        assertTrue(Files.isSameFile(dir.resolve("dicts/d.txt"), loaded.lexica().get(0)));
    }

    @Test
    void aMissingFieldIsNullAndAnEmptyOneIsEmpty() throws IOException {
        LigerConfig loaded = LigerConfigLoader.load(write("c.yaml", "semantics:\npragmatics: []\n"));

        assertEquals(List.of(), loaded.semantics());
        assertEquals(List.of(), loaded.pragmatics());
        assertNull(loaded.lexica());
    }

    @Test
    void assemblesAxiomFilesWithTptpComments() throws IOException {
        write("axioms/a.p", "fof(a, axiom, p).\n");
        write("axioms/b.p", "fof(b, axiom, q).\n");
        Path config = write("rules/c.liger.yaml", """
                axioms:
                  - ../axioms/a.p
                  - ../axioms/b.p
                """);

        ResolvedRuleSet set = LigerConfigLoader.resolve(config, null);

        assertTrue(set.axioms().contains("fof(a, axiom, p)."));
        assertTrue(set.axioms().indexOf("fof(a") < set.axioms().indexOf("fof(b"));
        // TPTP's comment character, not the rule files' //.
        for (String line : set.axioms().split("\n")) {
            assertTrue(line.startsWith("%") || line.startsWith("fof(") || line.isBlank(), line);
        }
    }

    @Test
    void axiomsAreInheritedFromTheDefaultConfigLikePragmatics() throws IOException {
        write("axioms/a.p", "fof(a, axiom, p).\n");
        write("rules/own.liger", "#a X #b ==> #a Y #b.\n");
        Path defaultConfig = write("rules/default.liger.yaml", "axioms:\n  - ../axioms/a.p\n");
        Path single = write("rules/s.liger", "");
        Path withOwn = write("rules/own.liger.yaml", "semantics:\n  - own.liger\naxioms: []\n");

        assertTrue(LigerConfigLoader.resolve(single, defaultConfig).axioms().contains("fof(a"));
        // A config that lists axioms, even none, keeps its own.
        assertEquals("", LigerConfigLoader.resolve(withOwn, defaultConfig).axioms());
    }

    @Test
    void withoutAxiomsAnywhereTheRuleSetHasNone() throws IOException {
        assertNull(LigerConfigLoader.resolve(write("s.liger", ""), dir.resolve("missing.yaml")).axioms());
    }

    @Test
    void rejectsUnknownFields() throws IOException {
        Path config = write("c.yaml", "semantic:\n  - a.liger\n");
        LigerConfigException e = assertThrows(LigerConfigException.class, () -> LigerConfigLoader.load(config));
        assertTrue(e.getMessage().contains("semantic"), e.getMessage());
    }

    @Test
    void rejectsMissingFiles() throws IOException {
        Path config = write("c.yaml", "pragmatics:\n  - nope.liger\n");
        LigerConfigException e = assertThrows(LigerConfigException.class, () -> LigerConfigLoader.load(config));
        assertTrue(e.getMessage().contains("nope.liger") && e.getMessage().contains("pragmatics"), e.getMessage());
    }

    @Test
    void rejectsANonListField() throws IOException {
        Path config = write("c.yaml", "lexica: ../dicts/d.txt\n");
        assertThrows(LigerConfigException.class, () -> LigerConfigLoader.load(config));
    }

    @Test
    void assemblesRuleFilesInOrderSoTemplatesCrossFiles() throws IOException {
        Path templates = write("t.liger", "SUBJEDGE(#a,#b) := #a SUBJ #b .\n");
        Path rules = write("r.liger", "@SUBJEDGE(#a,#b) ==> #a FLAG 'ok'.");

        String assembled = LigerConfigLoader.assemble(List.of(templates, rules));

        assertTrue(assembled.indexOf("SUBJEDGE(#a,#b) :=") < assembled.indexOf("==> #a FLAG"));
        RuleParser rp = new RuleParser(assembled);
        assertEquals(1, rp.getRules().size());
        assertTrue(rp.getTemplateRegistry().getTemplates().containsKey("SUBJEDGE"));
    }

    @Test
    void makesLexiconDirectivesAbsolute() throws IOException {
        Path rules = write("rules/r.liger", "--lexicon(../dicts/d.txt)\n#a SUBJ #b ==> #a FLAG 'ok'.\n");

        String text = LigerConfigLoader.readRuleFile(rules);

        assertTrue(text.contains("--lexicon(" + dir.resolve("rules").toAbsolutePath() + "/../dicts/d.txt)"), text);
    }

    @Test
    void aSingleRuleFileTakesPragmaticsAndLexicaFromTheDefaultConfig() throws IOException {
        write("rules/p.liger", "#a X #b ==> #a Y #b.\n");
        write("dicts/d.txt", "");
        Path defaultConfig = write("rules/default.liger.yaml", "pragmatics:\n  - p.liger\nlexica:\n  - ../dicts/d.txt\n");
        Path single = write("rules/s.liger", "#a SUBJ #b ==> #a FLAG 'ok'.\n");

        ResolvedRuleSet set = LigerConfigLoader.resolve(single, defaultConfig);

        assertEquals(ResolvedRuleSet.RULES, set.kind());
        assertEquals("#a SUBJ #b ==> #a FLAG 'ok'.\n", set.semantics());
        assertTrue(set.pragmatics().contains("#a X #b ==> #a Y #b."));
        assertEquals(1, set.lexica().size());
    }

    @Test
    void aConfigInheritsOnlyTheFieldsItLeavesOut() throws IOException {
        write("rules/p.liger", "#a X #b ==> #a Y #b.\n");
        write("rules/own.liger", "#a OWN #b ==> #a Z #b.\n");
        write("dicts/d.txt", "");
        Path defaultConfig = write("rules/default.liger.yaml", "pragmatics:\n  - p.liger\nlexica:\n  - ../dicts/d.txt\n");
        Path withoutPragmatics = write("rules/a.liger.yaml", "semantics:\n  - own.liger\nlexica: []\n");

        ResolvedRuleSet set = LigerConfigLoader.resolve(withoutPragmatics, defaultConfig);

        assertEquals(ResolvedRuleSet.CONFIG, set.kind());
        assertTrue(set.semantics().contains("#a OWN #b"));
        assertTrue(set.pragmatics().contains("#a X #b"), "pragmatics left out: inherited");
        assertEquals(List.of(), set.lexica(), "lexica given as empty: not inherited");
    }

    @Test
    void withoutADefaultConfigTheLexicaAreLigersDefault() throws IOException {
        Path single = write("s.liger", "");

        ResolvedRuleSet set = LigerConfigLoader.resolve(single, dir.resolve("missing.yaml"));

        assertNull(set.pragmatics());
        assertEquals(LexiconRegistry.defaultPaths(), set.lexica());
    }

    private static List<String> fields(ConfigTree tree) {
        return tree.sections().stream().map(ConfigTree.Section::field).toList();
    }

    @Test
    void theTreeListsAConfigsFilesPerFieldAndMarksInheritedOnes() throws IOException {
        write("rules/own.liger", "");
        write("rules/p.liger", "");
        write("dicts/d.txt", "");
        write("axioms/a.p", "");
        Path defaultConfig = write("rules/default.liger.yaml", "pragmatics:\n  - p.liger\nlexica:\n  - ../dicts/d.txt\n");
        Path config = write("rules/c.liger.yaml", "semantics:\n  - own.liger\naxioms:\n  - ../axioms/a.p\n");

        ConfigTree tree = LigerConfigLoader.tree(config, defaultConfig);

        assertEquals(ResolvedRuleSet.CONFIG, tree.kind());
        assertEquals(List.of("semantics", "pragmatics", "lexica", "axioms"), fields(tree));
        assertEquals(List.of(dir.resolve("rules/own.liger")), tree.section("semantics").files());
        assertNull(tree.section("semantics").inheritedFrom());
        assertEquals(List.of(dir.resolve("rules/p.liger")), tree.section("pragmatics").files());
        assertEquals(defaultConfig.toString(), tree.section("pragmatics").inheritedFrom());
        assertEquals(defaultConfig.toString(), tree.section("lexica").inheritedFrom());
        assertNull(tree.section("axioms").inheritedFrom());
    }

    @Test
    void theTreeOfASingleRuleFileIsThatFileAsSemantics() throws IOException {
        write("rules/p.liger", "");
        Path defaultConfig = write("rules/default.liger.yaml", "pragmatics:\n  - p.liger\n");
        Path single = write("rules/s.liger", "");

        ConfigTree tree = LigerConfigLoader.tree(single, dir.resolve("missing.yaml"));
        assertEquals(ResolvedRuleSet.RULES, tree.kind());
        assertEquals(List.of(single.toAbsolutePath()), tree.section("semantics").files());
        Path relative = Path.of("").toAbsolutePath().relativize(single);
        assertTrue(LigerConfigLoader.tree(relative, null).section("semantics").files().get(0).isAbsolute(),
                "a relative rule-file path comes back absolute");
        assertFalse(tree.section("pragmatics").present());
        assertFalse(tree.section("axioms").present());
        assertEquals(ConfigTree.BUILTIN, tree.section("lexica").inheritedFrom());

        ConfigTree inherited = LigerConfigLoader.tree(single, defaultConfig);
        assertEquals(defaultConfig.toString(), inherited.section("pragmatics").inheritedFrom());
    }

    /** What the client does with the tree when a file is edited: join the files as assemble does. */
    @Test
    void joiningTheTreesFilesReproducesTheResolvedText() throws IOException {
        write("rules/a.liger", "#a X #b ==> #a Y #b.\n");
        write("rules/b.liger", "--lexicon(../dicts/d.txt)\n#a Y #b ==> #a Z #b.");
        write("axioms/a.p", "fof(a, axiom, p).\n");
        Path config = write("rules/c.liger.yaml", """
                semantics:
                  - a.liger
                  - b.liger
                pragmatics: []
                axioms:
                  - ../axioms/a.p
                """);

        ConfigTree tree = LigerConfigLoader.tree(config, null);
        ResolvedRuleSet set = LigerConfigLoader.resolve(config, null);

        assertEquals(set.semantics(), LigerConfigLoader.assemble(tree.section("semantics").files()));
        assertEquals(set.pragmatics(), LigerConfigLoader.assemble(tree.section("pragmatics").files()));
        assertEquals(set.axioms(), LigerConfigLoader.assembleAxioms(tree.section("axioms").files()));
    }

    @Test
    void findsConfigsAnywhereUnderTheGrammarButNotInFileIndexDirs() throws IOException {
        Path grammar = write("g/main.lfg", "");
        write("g/c.liger.yaml", "");
        write("g/liger_rules/sub/d.liger.yaml", "");
        write("g/main.lfg.fileindexdir/x.liger.yaml", "");
        write("g/other.yaml", "");
        write("elsewhere/e.liger.yaml", "");

        List<Path> expected = List.of(dir.resolve("g/c.liger.yaml"), dir.resolve("g/liger_rules/sub/d.liger.yaml"));
        assertEquals(expected, LigerConfigLoader.findConfigs(grammar));
        assertEquals(expected, LigerConfigLoader.findConfigs(dir.resolve("g")));
    }

    /**
     * The pragmatics rules used to be one string in the client (APP_DEFAULTS.graphInspector.rulesText),
     * kept here as legacy_pragmatics_rules.txt. Split into files and chained by the default config,
     * they must parse to the same rules, in the same order, with the same templates and hierarchies.
     */
    @Test
    void theDefaultConfigsPragmaticsMatchTheFormerHardcodedRules() throws IOException {
        Path defaultConfig = Paths.get("liger_resources", "rules", "default.liger.yaml");
        Assumptions.assumeTrue(Files.exists(defaultConfig), "needs liger_resources/rules from xleplusglue");

        String legacy = Files.readString(Paths.get("src", "test", "resources", "rules", "legacy_pragmatics_rules.txt"));
        String assembled = LigerConfigLoader.assemble(LigerConfigLoader.load(defaultConfig).pragmatics());

        RuleParser before = new RuleParser(legacy);
        RuleParser after = new RuleParser(assembled);

        assertEquals(13, before.getRules().size());
        assertEquals(ruleStrings(before), ruleStrings(after));
        assertEquals(templates(before), templates(after));
        assertEquals(before.getHierarchyRegistry().getHierarchies(), after.getHierarchyRegistry().getHierarchies());
        assertEquals(before.getReplace(), after.getReplace());
    }

    private static List<String> ruleStrings(RuleParser parser) {
        List<String> out = new ArrayList<>();
        for (Rule rule : parser.getRules()) {
            out.add(rule.toString().replaceAll("\\s+", " ").strip());
        }
        return out;
    }

    private static Map<String, String> templates(RuleParser parser) {
        Map<String, String> out = new TreeMap<>();
        for (Map.Entry<String, QueryTemplate> e : parser.getTemplateRegistry().getTemplates().entrySet()) {
            out.put(e.getKey(), e.getValue().getParameters() + " " + e.getValue().getAlternatives());
        }
        return out;
    }
}
