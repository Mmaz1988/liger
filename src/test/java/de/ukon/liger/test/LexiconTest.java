package de.ukon.liger.test;

import de.ukon.liger.analysis.LinguisticDictionary;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.analysis.config.LigerConfigException;
import de.ukon.liger.analysis.lexicon.Lexicon;
import de.ukon.liger.analysis.lexicon.LexiconRegistry;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LexiconTest {

    @TempDir
    Path dir;

    @AfterEach
    void resetActiveLexica() {
        LexiconRegistry.resetToDefault();
    }

    private Path write(String name, String content) throws IOException {
        Path file = dir.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    @Test
    void parsesTheDictionaryFormat() throws IOException {
        Path file = write("dict.txt", """
                // comment
                >modals
                ***
                #certain
                certainly

                ***
                #probable
                probably, presumably,
                perhaps

                >pronoun
                ***
                #exclusive
                you, he, she ,it
                ***
                #perception
                see,hear,
                """);

        Map<String, LinkedHashMap<String, List<String>>> dict = LinguisticDictionary.text2Dict(file);

        // A single value on its own line used to be dropped (no comma).
        assertEquals(List.of("certainly"), dict.get("modals").get("certain"));
        // Trailing commas and continuation lines.
        assertEquals(List.of("probably", "presumably", "perhaps"), dict.get("modals").get("probable"));
        // "she ,it" used to be one value, as did "see,hear,".
        assertEquals(List.of("you", "he", "she", "it"), dict.get("pronoun").get("exclusive"));
        assertEquals(List.of("see", "hear"), dict.get("pronoun").get("perception"));
    }

    @Test
    void labelsWithinOneEntryShareItsValues() throws IOException {
        Path file = write("dict.txt", """
                >d
                ***
                #A
                #B
                x, y
                """);

        Map<String, LinkedHashMap<String, List<String>>> dict = LinguisticDictionary.text2Dict(file);
        assertEquals(List.of("x", "y"), dict.get("d").get("A"));
        assertEquals(List.of("x", "y"), dict.get("d").get("B"));
    }

    @Test
    void mergeUnionsLabelsAndEarlierLexicaWinLookups() {
        Lexicon first = new Lexicon(Map.of("d", new LinkedHashMap<>(Map.of("A", List.of("x")))));
        LinkedHashMap<String, List<String>> secondLabels = new LinkedHashMap<>();
        secondLabels.put("B", List.of("x", "y"));
        secondLabels.put("A", List.of("z"));
        Lexicon second = new Lexicon(Map.of("d", secondLabels, "e", new LinkedHashMap<>(Map.of("C", List.of("w")))));

        Lexicon merged = Lexicon.merge(List.of(first, second));

        assertEquals(Optional.of("A"), merged.lookup("d", "x"));
        assertEquals(Optional.of("A"), merged.lookup("d", "z"));
        assertEquals(Optional.of("B"), merged.lookup("d", "y"));
        assertEquals(Optional.of("C"), merged.lookup("e", "w"));
        assertEquals(Optional.empty(), merged.lookup("d", "nothing"));
        assertFalse(merged.hasDictionary("unknown"));
    }

    @Test
    void registryCachesUntilTheFileChanges() throws IOException {
        Path file = write("dict.txt", ">d\n***\n#A\nx\n");

        Lexicon loaded = LexiconRegistry.load(file);
        assertSame(loaded, LexiconRegistry.load(file));

        Files.writeString(file, ">d\n***\n#B\nx\n");
        Files.setLastModifiedTime(file, FileTime.fromMillis(Files.getLastModifiedTime(file).toMillis() + 5000));

        assertEquals(Optional.of("B"), LexiconRegistry.load(file).lookup("d", "x"));
    }

    @Test
    void settingAMissingLexiconFailsAndKeepsTheActiveOnes() throws IOException {
        Path good = write("good.txt", ">d\n***\n#A\nx\n");
        LexiconRegistry.setActive(List.of(good));

        assertThrows(LigerConfigException.class, () -> LexiconRegistry.setActive(List.of(dir.resolve("missing.txt"))));
        assertEquals(List.of(good), LexiconRegistry.activePaths());
    }

    // testDirS17.pl has one TENSE, 'past'. Variables in a rule's output are only substituted
    // under --replace(true), and lex/2 runs as part of that substitution.
    private static LinguisticStructure apply(String rules) {
        LinkedHashMap<String, LinguisticStructure> fs = new QueryParserTest().loadFs("testDirS17.pl");
        LinguisticStructure structure = fs.values().iterator().next();
        RuleParser rp = new RuleParser(new ArrayList<>());
        rp.setRules(rp.parseRuleFile("--replace(true)\n" + rules));
        rp.addAnnotation2(structure);
        return structure;
    }

    private static List<String> valuesOf(LinguisticStructure structure, String label) {
        return structure.annotation.stream()
                .filter(c -> label.equals(c.getRelationLabel()))
                .map(c -> String.valueOf(c.getFsValue()))
                .toList();
    }

    @Test
    void lexUsesTheActiveLexica() throws IOException {
        LexiconRegistry.setActive(List.of(write("tense.txt", ">tense\n***\n#PAST\npast, went\n")));

        // The bound value is 'past'; it is looked up without its quotes.
        assertEquals(List.of("PAST"), valuesOf(apply("#a TENSE %t ==> #a TCLASS lex(%t,tense).\n"), "TCLASS"));
    }

    @Test
    void lexReturnsUndefinedForAValueMissingFromAKnownDictionary() throws IOException {
        LexiconRegistry.setActive(List.of(write("tense.txt", ">tense\n***\n#FUTURE\nwill\n")));

        assertEquals(List.of("undefined"), valuesOf(apply("#a TENSE %t ==> #a TCLASS lex(%t,tense).\n"), "TCLASS"));
    }

    @Test
    void lexiconDirectiveLayersOverTheActiveLexica() throws IOException {
        LexiconRegistry.setActive(List.of(write("global.txt", ">tense\n***\n#GLOBAL\npast\n>other\n***\n#O\npast\n")));
        Path local = write("local.txt", ">tense\n***\n#LOCAL\npast\n");

        LinguisticStructure structure = apply("--lexicon(" + local + ")\n"
                + "#a TENSE %t ==> #a TCLASS lex(%t,tense) & #a OCLASS lex(%t,other).\n");

        // The directive's lexicon is consulted first; the global one still applies.
        assertEquals(List.of("LOCAL"), valuesOf(structure, "TCLASS"));
        assertEquals(List.of("O"), valuesOf(structure, "OCLASS"));
    }

    @Test
    void lexWithAnUnknownDictionaryIsLeftInPlace() throws IOException {
        LexiconRegistry.setActive(List.of(write("tense.txt", ">tense\n***\n#A\npast\n")));

        List<String> values = valuesOf(apply("#a TENSE %t ==> #a TCLASS lex(%t,nosuchdict).\n"), "TCLASS");

        assertEquals(1, values.size());
        assertTrue(values.get(0).contains("nosuchdict"), values.get(0));
    }

    @Test
    void aDirectiveInsideACommentIsNotADirective() {
        RuleParser rp = new RuleParser(new ArrayList<>());
        // Would throw on the missing file if it were read as a directive.
        assertDoesNotThrow(() -> rp.parseRuleFile("// see --lexicon(" + dir.resolve("missing.txt") + ")\n"));
    }
}
