package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryTemplate;
import de.ukon.liger.analysis.RuleParser.Rule;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.analysis.config.ConfigTree;
import de.ukon.liger.analysis.config.LigerConfig;
import de.ukon.liger.analysis.config.LigerConfigException;
import de.ukon.liger.analysis.config.LigerConfigLoader;
import de.ukon.liger.analysis.config.ResolvedRuleSet;
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

        ResolvedRuleSet set = LigerConfigLoader.resolve(config);

        assertTrue(set.axioms().contains("fof(a, axiom, p)."));
        assertTrue(set.axioms().indexOf("fof(a") < set.axioms().indexOf("fof(b"));
        // TPTP's comment character, not the rule files' //.
        for (String line : set.axioms().split("\n")) {
            assertTrue(line.startsWith("%") || line.startsWith("fof(") || line.isBlank(), line);
        }
    }

    @Test
    void aConfigWithoutAxiomsHasNone() throws IOException {
        write("rules/own.liger", "#a X #b ==> #a Y #b.\n");
        Path without = write("rules/own.liger.yaml", "semantics:\n  - own.liger\n");
        Path empty = write("rules/empty.liger.yaml", "semantics:\n  - own.liger\naxioms: []\n");

        assertNull(LigerConfigLoader.resolve(without).axioms());
        assertEquals("", LigerConfigLoader.resolve(empty).axioms());
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
    void aSingleRuleFileIsSemanticsOnly() throws IOException {
        Path single = write("rules/s.liger", "#a SUBJ #b ==> #a FLAG 'ok'.\n");

        ResolvedRuleSet set = LigerConfigLoader.resolve(single);

        assertEquals(ResolvedRuleSet.RULES, set.kind());
        assertEquals("#a SUBJ #b ==> #a FLAG 'ok'.\n", set.semantics());
        assertNull(set.pragmatics(), "no pragmatics: such a rule set cannot be post-processed");
        assertNull(set.axioms());
        assertEquals(List.of(), set.lexica());
    }

    /** Nothing is inherited, from a default config or anywhere else. */
    @Test
    void aConfigGetsExactlyWhatItLists() throws IOException {
        write("rules/own.liger", "#a OWN #b ==> #a Z #b.\n");
        Path config = write("rules/a.liger.yaml", "semantics:\n  - own.liger\n");

        ResolvedRuleSet set = LigerConfigLoader.resolve(config);

        assertEquals(ResolvedRuleSet.CONFIG, set.kind());
        assertTrue(set.semantics().contains("#a OWN #b"));
        assertNull(set.pragmatics());
        assertEquals(List.of(), set.lexica());
    }

    private static List<String> fields(ConfigTree tree) {
        return tree.sections().stream().map(ConfigTree.Section::field).toList();
    }

    @Test
    void theTreeListsAConfigsFilesPerField() throws IOException {
        write("liger/semantics/own.liger", "");
        write("liger/dicts/d.txt", "");
        Path config = write("liger/c.liger.yaml", "semantics:\n  - semantics/own.liger\nlexica:\n  - dicts/d.txt\n");

        ConfigTree tree = LigerConfigLoader.tree(config);

        assertEquals(ResolvedRuleSet.CONFIG, tree.kind());
        assertEquals(List.of("semantics", "pragmatics", "lexica", "axioms"), fields(tree));
        assertEquals(List.of(dir.resolve("liger/semantics/own.liger")), tree.section("semantics").files());
        assertFalse(tree.section("pragmatics").present());
        assertEquals(List.of(dir.resolve("liger/dicts/d.txt")), tree.section("lexica").files());
        assertFalse(tree.section("axioms").present());
    }

    @Test
    void theScopeDefaultsToTheConfigsFolderAndMarksFilesOutsideIt() throws IOException {
        write("g/liger/semantics/in.liger", "");
        write("g/shared/out.liger", "");
        Path config = write("g/liger/c.liger.yaml", "semantics:\n  - semantics/in.liger\n  - ../shared/out.liger\n");

        ConfigTree tree = LigerConfigLoader.tree(config);

        assertTrue(Files.isSameFile(dir.resolve("g/liger"), tree.scope()));
        List<Path> files = tree.section("semantics").files();
        assertTrue(tree.inScope(files.get(0)));
        assertFalse(tree.inScope(files.get(1)), "../ leaves the scope even though it is lexically under it");
    }

    @Test
    void theScopeFieldWidensTheBoundaryWithoutChangingHowPathsResolve() throws IOException {
        write("g/liger/semantics/in.liger", "");
        write("g/shared/out.liger", "");
        Path config = write("g/liger/c.liger.yaml", "scope: ..\nsemantics:\n  - ../shared/out.liger\n");

        ConfigTree tree = LigerConfigLoader.tree(config);

        assertTrue(Files.isSameFile(dir.resolve("g"), tree.scope()));
        assertTrue(tree.inScope(tree.section("semantics").files().get(0)));
    }

    @Test
    void rejectsAScopeThatIsNotAFolder() throws IOException {
        Path config = write("c.liger.yaml", "scope: nowhere\n");

        LigerConfigException e = assertThrows(LigerConfigException.class, () -> LigerConfigLoader.load(config));
        assertTrue(e.getMessage().contains("nowhere"), e.getMessage());
    }

    @Test
    void theTreeOfASingleRuleFileIsThatFileAsSemantics() throws IOException {
        Path single = write("rules/s.liger", "");

        ConfigTree tree = LigerConfigLoader.tree(single);
        assertEquals(ResolvedRuleSet.RULES, tree.kind());
        assertEquals(List.of(single.toAbsolutePath()), tree.section("semantics").files());
        assertEquals(single.toAbsolutePath().getParent(), tree.scope());
        Path relative = Path.of("").toAbsolutePath().relativize(single);
        assertTrue(LigerConfigLoader.tree(relative).section("semantics").files().get(0).isAbsolute(),
                "a relative rule-file path comes back absolute");
        assertFalse(tree.section("pragmatics").present());
        assertFalse(tree.section("lexica").present());
        assertFalse(tree.section("axioms").present());
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

        ConfigTree tree = LigerConfigLoader.tree(config);
        ResolvedRuleSet set = LigerConfigLoader.resolve(config);

        assertEquals(set.semantics(), LigerConfigLoader.assemble(tree.section("semantics").files()));
        assertEquals(set.pragmatics(), LigerConfigLoader.assemble(tree.section("pragmatics").files()));
        assertEquals(set.axioms(), LigerConfigLoader.assembleAxioms(tree.section("axioms").files()));
    }

    @Test
    void findsAGrammarsConfigsInItsFolderAndItsLigerFolderOnly() throws IOException {
        Path grammar = write("g/main.lfg", "");
        write("g/c.liger.yaml", "");
        write("g/liger/d.liger.yaml", "");
        write("g/liger/deeper/x.liger.yaml", "");
        write("g/other/y.liger.yaml", "");
        write("g/other.yaml", "");

        List<Path> expected = List.of(dir.resolve("g/c.liger.yaml"), dir.resolve("g/liger/d.liger.yaml"));
        assertEquals(expected, LigerConfigLoader.findConfigs(grammar));
        assertEquals(expected, LigerConfigLoader.findConfigs(dir.resolve("g")));
    }

    /** A loose grammar beside a grammar folder (grammars/dev) is not offered the folder's config. */
    @Test
    void aLooseGrammarIsNotOfferedANeighboursConfig() throws IOException {
        Path loose = write("dev/loose.lfg", "");
        write("dev/big/liger/big.liger.yaml", "");

        assertEquals(List.of(), LigerConfigLoader.findConfigs(loose));
    }

    /**
     * The pragmatics rules used to be one string in the client (APP_DEFAULTS.graphInspector.rulesText),
     * kept here as legacy_pragmatics_rules.txt. Split into files and chained by the lfgxdrt grammar's
     * config, they must parse to the same rules, in the same order, with the same templates and
     * hierarchies.
     */
    @Test
    void theLfgxdrtConfigsPragmaticsMatchTheFormerHardcodedRules() throws IOException {
        Path lfgxdrtConfig = Paths.get("grammars", "dev", "lfgxdrt_inference_grammar", "liger", "lfgxdrt.liger.yaml");
        Assumptions.assumeTrue(Files.exists(lfgxdrtConfig), "needs grammars/ from xleplusglue");

        String legacy = Files.readString(Paths.get("src", "test", "resources", "rules", "legacy_pragmatics_rules.txt"));
        String assembled = LigerConfigLoader.assemble(LigerConfigLoader.load(lfgxdrtConfig).pragmatics());

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
