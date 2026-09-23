package de.ukon.liger.webservice.rest;

import de.ukon.liger.analysis.config.LigerConfigException;
import de.ukon.liger.analysis.lexicon.LexiconRegistry;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.webservice.rest.dtos.GrammarString;
import de.ukon.liger.webservice.rest.dtos.LigerConfigTree;
import de.ukon.liger.webservice.rest.dtos.LigerRuleSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * /load_rules takes either a LiGER config or a single rule file. Both answer with the rule
 * text of both stages and make their lexica the globally active ones.
 */
class LoadRulesTest {

    @TempDir
    Path resources;

    private String previousWorkingDirectory;

    @BeforeEach
    void pointLigerAtTheTempResources() throws IOException {
        previousWorkingDirectory = PathVariables.workingDirectory;
        PathVariables.workingDirectory = resources + "/";
        Files.createDirectories(resources.resolve("rules"));
        Files.createDirectories(resources.resolve("dicts"));
        Files.writeString(resources.resolve("rules/sem.liger"), "#a SUBJ #b ==> #a FLAG 'sem'.\n");
        Files.writeString(resources.resolve("rules/prag.liger"), "#a SUBJ #b ==> #a FLAG 'prag'.\n");
        Files.writeString(resources.resolve("dicts/default.txt"), ">d\n***\n#DEFAULT\nx\n");
        Files.writeString(resources.resolve("dicts/other.txt"), ">d\n***\n#OTHER\nx\n");
        Files.createDirectories(resources.resolve("axioms"));
        Files.writeString(resources.resolve("axioms/basic.p"), "fof(basic, axiom, p).\n");
        Files.writeString(resources.resolve("rules/default.liger.yaml"),
                "semantics:\n  - sem.liger\npragmatics:\n  - prag.liger\nlexica:\n  - ../dicts/default.txt\n"
                        + "axioms:\n  - ../axioms/basic.p\n");
    }

    @AfterEach
    void restore() {
        PathVariables.workingDirectory = previousWorkingDirectory;
        LexiconRegistry.resetToDefault();
    }

    private static LigerRuleSet load(Path path) {
        return new LigerController().loadRules(new GrammarString(path.toString()));
    }

    @Test
    void aConfigYieldsBothStagesAndActivatesItsLexica() throws IOException {
        Path config = resources.resolve("rules/other.liger.yaml");
        Files.writeString(config, "semantics:\n  - sem.liger\npragmatics:\n  - prag.liger\nlexica:\n  - ../dicts/other.txt\n");

        LigerRuleSet set = load(config);

        assertEquals("config", set.kind);
        assertTrue(set.semantics.contains("FLAG 'sem'"));
        assertTrue(set.pragmatics.contains("FLAG 'prag'"));
        assertEquals(List.of(resources.resolve("dicts/other.txt").toRealPath().toString()), set.lexica);
        assertEquals("OTHER", LexiconRegistry.active().lookup("d", "x").orElseThrow());
    }

    @Test
    void aSingleRuleFileIsSemanticsOnlyAndActivatesNoLexica() throws IOException {
        load(resources.resolve("rules/default.liger.yaml"));
        Path single = resources.resolve("rules/single.liger");
        Files.writeString(single, "#a OBJ #b ==> #a FLAG 'single'.\n");

        LigerRuleSet set = load(single);

        assertEquals("rules", set.kind);
        assertEquals("#a OBJ #b ==> #a FLAG 'single'.\n", set.semantics);
        assertNull(set.pragmatics, "nothing is inherited: no pragmatics, so no post-processing");
        assertNull(set.axioms);
        assertEquals(List.of(), set.lexica);
        assertTrue(LexiconRegistry.active().lookup("d", "x").isEmpty());
    }

    @Test
    void aBrokenConfigIsABadRequestAndLeavesTheActiveLexica() throws IOException {
        load(resources.resolve("rules/default.liger.yaml"));
        Path broken = resources.resolve("rules/broken.liger.yaml");
        Files.writeString(broken, "lexica:\n  - ../dicts/missing.txt\n");

        LigerController controller = new LigerController();
        LigerConfigException e = assertThrows(LigerConfigException.class,
                () -> controller.loadRules(new GrammarString(broken.toString())));
        ResponseEntity<Map<String, String>> response = controller.handleConfigError(e);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("missing.txt"));
        assertEquals("DEFAULT", LexiconRegistry.active().lookup("d", "x").orElseThrow());
    }

    @Test
    void loadFileReturnsTextWithoutTouchingTheActiveLexica() throws IOException {
        Path other = resources.resolve("rules/other.liger.yaml");
        Files.writeString(other, "lexica:\n  - ../dicts/other.txt\n");
        load(other);
        List<Path> before = LexiconRegistry.activePaths();
        Path axioms = resources.resolve("axioms.txt");
        Files.writeString(axioms, "fof(a, axiom, p).\n");

        GrammarString text = new LigerController().loadFile(new GrammarString(axioms.toString()));

        assertEquals("fof(a, axiom, p).\n", text.grammar);
        assertEquals(before, LexiconRegistry.activePaths());
    }

    @Test
    void configTreeListsTheFilesPerFieldWithoutTouchingTheActiveLexica() throws IOException {
        Path other = resources.resolve("rules/other.liger.yaml");
        Files.writeString(other, "semantics:\n  - sem.liger\npragmatics:\n  - prag.liger\nlexica:\n  - ../dicts/other.txt\n");
        load(resources.resolve("rules/default.liger.yaml"));
        List<Path> before = LexiconRegistry.activePaths();

        LigerConfigTree tree = new LigerController().configTree(new GrammarString(other.toString()));

        assertEquals(before, LexiconRegistry.activePaths());
        assertEquals("config", tree.kind);
        assertEquals(other.toString(), tree.source);
        assertTrue(Files.isSameFile(resources.resolve("rules"), Path.of(tree.scope)));
        assertEquals(resources.resolve("rules").toRealPath().toString(), tree.scope);
        assertEquals(List.of("semantics", "pragmatics", "lexica", "axioms"),
                tree.sections.stream().map(s -> s.field).toList());

        LigerConfigTree.Section semantics = tree.sections.get(0);
        assertEquals("sem.liger", semantics.files.get(0).name);
        assertEquals(1, semantics.files.get(0).count, "rules in sem.liger");
        assertEquals(1, tree.sections.get(2).files.get(0).count, "labels in other.txt");
        assertNull(tree.projectRoot, "no grammars folder beside these resources");
        assertTrue(semantics.files.get(0).inScope);
        assertTrue(Files.isSameFile(resources.resolve("rules/sem.liger"), Path.of(semantics.files.get(0).path)));
        assertFalse(tree.sections.get(2).files.get(0).inScope, "../dicts is outside the config's folder");
        LigerConfigTree.Section axioms = tree.sections.get(3);
        assertFalse(axioms.present);
        assertTrue(axioms.files.isEmpty());

        // The marker lines /load_rules writes name the tree's paths, so a client can key on them.
        LigerConfigTree.Section pragmatics = tree.sections.get(1);
        LigerRuleSet set = load(other);
        assertTrue(set.pragmatics.startsWith("// ===== " + pragmatics.files.get(0).path + " =====\n"), set.pragmatics);
    }

    @Test
    void listConfigsFindsTheConfigsUnderAGrammar() throws IOException {
        Path grammarDir = resources.resolve("grammar");
        Files.createDirectories(grammarDir.resolve("liger"));
        Files.writeString(grammarDir.resolve("main.lfg"), "");
        Files.writeString(grammarDir.resolve("liger/g.liger.yaml"), "");

        List<String> configs = new LigerController().listConfigs(new GrammarString(grammarDir.resolve("main.lfg").toString()));

        assertEquals(List.of(grammarDir.resolve("liger/g.liger.yaml").toString()), configs);
    }

    @Test
    void configTreeNamesTheProjectsGrammarsFolder() throws IOException {
        // A project inside the temp folder: <project>/liger_resources and <project>/grammars.
        Path project = resources.resolve("project");
        Files.createDirectories(project.resolve("liger_resources"));
        Files.createDirectories(project.resolve("grammars"));
        PathVariables.workingDirectory = project.resolve("liger_resources") + "/";

        LigerConfigTree tree = new LigerController().configTree(
                new GrammarString(resources.resolve("rules/default.liger.yaml").toString()));

        assertEquals(project.resolve("grammars").toRealPath().toString(), tree.projectRoot);
    }
}
