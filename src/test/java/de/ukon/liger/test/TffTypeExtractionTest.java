package de.ukon.liger.test;

import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.reasoning.AxiomExtractor;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.GrammarAxioms;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TFF_TYPE values are TFF type declaration bodies, collected next to the grammar axioms so the
// Vampire service can declare a symbol over a non-$i sort (a degree predicate, a threshold
// function) once per run. The value carries ':', '*', '>' and '$', which no other rule value
// had to survive.
class TffTypeExtractionTest {

    private static LinguisticStructure annotated(String rules) {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testdirS1.pl");
        List<LinguisticStructure> structures = new ArrayList<>(fs.values());
        LinguisticStructure structure = structures.get(0);
        RuleParser parser = new RuleParser(structures, rules, true); // replace mode, as --replace(true) in the grammar files
        parser.addAnnotation2(structure);
        return structure;
    }

    @Test
    void typeDeclarationsSurviveTheRuleParserWithTheirNamesSubstituted() {
        LinguisticStructure structure = annotated(
                "#i PRED %i & #i NTYPE #j ==> #i TFF_TYPE strip(%i): ($i * $int) > $o &"
                        + " #j TFF_TYPE th_strip(%i): $i > $int.");

        GrammarAxioms axioms = new AxiomExtractor().extractGrammarAxioms(structure);

        assertEquals(4, axioms.tffTypes.size(), "two declarations for each of the two names: " + axioms.tffTypes);
        for (String declaration : axioms.tffTypes) {
            assertTrue(!declaration.contains("%i") && !declaration.contains("strip("),
                    "the name is substituted: " + declaration);
            assertTrue(declaration.endsWith("($i * $int) > $o") || declaration.endsWith("$i > $int"),
                    "the signature is kept as written: " + declaration);
        }
        assertTrue(axioms.tptp.isEmpty() && axioms.prolog.isEmpty(), "types are not axioms");
    }

    @Test
    void aDeclarationIsCollectedOnce() {
        LinguisticStructure structure = annotated(
                "#i PRED %i ==> #i TFF_TYPE fast: ($i * $int) > $o.");

        GrammarAxioms axioms = new AxiomExtractor().extractGrammarAxioms(structure);

        assertEquals(List.of("fast: ($i * $int) > $o"), axioms.tffTypes,
                "one declaration, though every PRED node carries it");
    }
}
