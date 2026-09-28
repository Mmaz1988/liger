package de.ukon.liger.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The same parse and the same rules must give the same meaning constructors every time.
 *
 * They did not: with two clauses, each TAM rule matches twice, and the query parser returned
 * the matches keyed by {@code Solution}s whose hash is their {@code SolutionKey}s' identity
 * hashes -- so the matches were applied in an order that changed from call to call. The
 * nodes the rules introduce were then wired to the two clauses differently, and for some
 * orders GSWB found no reading at all (xleplusglue docs/bug_reports, 2026-09-28).
 *
 * The fixture is the raw XLE parse (no rules applied) of "A woman thought that a man saw
 * himself." with the lfgxdrt grammar, taken from /apply_rules_xle with an empty rule string.
 */
public class RuleDeterminismTest {

    private static final Path DIR = Paths.get("src", "test", "resources", "determinism");

    /** What LigerController.annotateSentence produces for one analysis. */
    private static String meaningConstructors(String rules) throws Exception {
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(
                DIR.resolve("woman_thought_man_saw_himself.json").toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        RuleParser rp = new RuleParser(new ArrayList<>(List.of(fs)), rules);
        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Collections.singleton(fs)));
        LinguisticStructure primary = branches.stream().findFirst().orElse(fs);

        GlueSemantics sem = new GlueSemantics();
        sem.annotateSyntheticMcIndices(primary);
        return sem.returnMeaningConstructors(primary, false, false, true);
    }

    @Test
    void tamRulesGiveTheSameMeaningConstructorsEveryTime() throws Exception {
        String rules = Files.readString(DIR.resolve("tam_rules_lfgxdrt.liger"));

        Set<String> outputs = new LinkedHashSet<>();
        for (int run = 0; run < 15; run++) {
            outputs.add(meaningConstructors(rules));
        }

        assertEquals(1, outputs.size(),
                "15 runs over the same parse gave " + outputs.size() + " different results");
    }
}
