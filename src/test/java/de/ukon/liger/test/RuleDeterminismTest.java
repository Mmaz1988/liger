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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The TAM rules on a sentence with two clauses ("A woman thought that a man saw himself.").
 *
 * Two defects made the meaning constructors vary, and some of them unprovable:
 * - A node a rule introduces only as a value (#a EVAL #b, #c VAR #d) was not counted as used,
 *   so a later rule minted the same id again: a VIEWPOINT node became an EVAL node, a T-REF'
 *   node a VAR node. Which pairs merged depended on the numbering; merged across the two
 *   clauses, GSWB found no reading.
 * - That numbering depended on the order matches were applied in, which was random:
 *   SolutionKey hashes by identity, and the query parser kept matches in HashMaps keyed by it.
 *
  * The fixture is the raw XLE parse (no rules applied) of "A woman thought that a man saw
 * himself." with the lfgxdrt grammar, taken from /apply_rules_xle with an empty rule string.
 */
public class RuleDeterminismTest {

    private static final Path DIR = Paths.get("src", "test", "resources", "determinism");

    /** The fixture after the rules, as LigerController.annotateSentence applies them. */
    private static LinguisticStructure annotated(String rules) throws Exception {
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> json = new ObjectMapper().readValue(
                DIR.resolve("woman_thought_man_saw_himself.json").toFile(), LinkedHashMap.class);
        LinguisticStructure fs = LinguisticStructure.parseFromJson(json);

        RuleParser rp = new RuleParser(new ArrayList<>(List.of(fs)), rules);
        Set<LinguisticStructure> branches = rp.addAnnotation2(new LinkedHashSet<>(Collections.singleton(fs)));
        return branches.stream().findFirst().orElse(fs);
    }

    /** What LigerController.annotateSentence produces for one analysis. */
    private static String meaningConstructors(String rules) throws Exception {
        LinguisticStructure primary = annotated(rules);

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

    /**
     * Every node a rule introduces is a new node. A node that so far only occurred as the VALUE of
     * a fact (an EVAL node, a VAR node) was not counted as used, so the next rule minted the same
     * id again: a clause's VIEWPOINT node became an EVAL node, a T-REF' node a VAR node -- of the
     * same clause or the other one, depending on the numbering.
     */
    @Test
    void introducedNodesAreNeverReused() throws Exception {
        String rules = Files.readString(DIR.resolve("tam_rules_lfgxdrt.liger"));
        LinguisticStructure fs = annotated(rules);

        // In this sentence each of these edges points at a node of its own.
        Set<String> introducing = Set.of("TEMP-REF", "EVAL", "VIEWPOINT", "VAR", "RESTR", "ASP-RESTR'", "T-REF'");
        Map<String, List<String>> edgesByTarget = fs.annotation.stream()
                .filter(c -> introducing.contains(c.getRelationLabel()))
                .collect(Collectors.groupingBy(c -> String.valueOf(c.getFsValue()), LinkedHashMap::new,
                        Collectors.mapping(c -> c.getFsNode() + " " + c.getRelationLabel(), Collectors.toList())));
        Map<String, List<String>> shared = edgesByTarget.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertEquals(Map.of(), shared, "introduced nodes reached by more than one edge");
    }
}
