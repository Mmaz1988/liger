package de.ukon.liger.webservice.rest;

import de.ukon.liger.analysis.config.LigerConfigException;
import de.ukon.liger.analysis.lexicon.LexiconRegistry;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.webservice.rest.dtos.GrammarString;
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
        Files.writeString(resources.resolve("rules/default.liger.yaml"),
                "semantics:\n  - sem.liger\npragmatics:\n  - prag.liger\nlexica:\n  - ../dicts/default.txt\n");
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
    void aSingleRuleFileFallsBackToTheDefaultConfig() throws IOException {
        load(resources.resolve("rules/default.liger.yaml"));
        Path single = resources.resolve("rules/single.liger");
        Files.writeString(single, "#a OBJ #b ==> #a FLAG 'single'.\n");

        LigerRuleSet set = load(single);

        assertEquals("rules", set.kind);
        assertEquals("#a OBJ #b ==> #a FLAG 'single'.\n", set.semantics);
        assertTrue(set.pragmatics.contains("FLAG 'prag'"));
        assertEquals("DEFAULT", LexiconRegistry.active().lookup("d", "x").orElseThrow());
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
}
