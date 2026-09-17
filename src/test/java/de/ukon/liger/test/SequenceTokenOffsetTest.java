package de.ukon.liger.test;

import de.ukon.liger.semantics.SequenceGraphAssembler;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.SourceTokenIndex;
import de.ukon.liger.webservice.rest.dtos.LigerSourceToken;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.ukon.liger.test.SourceIndexResolverTest.syntax;
import static org.junit.jupiter.api.Assertions.*;

/** Token offsets of an assembled sequence index into its joined plain text, like SYN-IDs are
 *  rebased into one numbering. */
public class SequenceTokenOffsetTest {

    private static List<LigerSourceToken> allTokens(LinguisticStructure structure) {
        List<LigerSourceToken> tokens = new ArrayList<>();
        SourceTokenIndex index = new SourceTokenIndex(structure);
        for (int i = 1; i < 200; i++) {
            if (index.hasSourceIndex(i)) {
                for (LigerSourceToken token : index.tokensFor(List.of(i))) {
                    if (!tokens.contains(token)) tokens.add(token);
                }
            }
        }
        tokens.sort((a, b) -> Integer.compare(a.start, b.start));
        return tokens;
    }

    private static void assertTokensMatchText(LinguisticStructure structure) {
        String text = new SourceTokenIndex(structure).text();
        List<LigerSourceToken> tokens = allTokens(structure);
        assertFalse(tokens.isEmpty());
        for (LigerSourceToken token : tokens) {
            assertTrue(token.start >= 0 && token.end <= text.length(), () -> token.form + " out of range in " + text);
            assertEquals(token.form, text.substring(token.start, token.end));
        }
    }

    @Test
    void plainTextUnquotesEachSentence() {
        assertEquals("Bill barked", LinguisticStructure.plainText("'Bill barked'"));
        assertEquals("Bill barked\nthe dog barked", LinguisticStructure.plainText("'Bill barked'\n'the dog barked'"));
        assertEquals("", LinguisticStructure.plainText(null));
    }

    @Test
    void secondSentenceTokensAreShiftedPastTheFirst() throws Exception {
        LinguisticStructure sequence = SequenceGraphAssembler.assemble(
                List.of(syntax("proper-name.json"), syntax("black-dog-on-table.json")));

        String text = new SourceTokenIndex(sequence).text();
        assertEquals("Bill barked\nthe black dog on the table barked", text);
        assertTokensMatchText(sequence);

        List<LigerSourceToken> tokens = allTokens(sequence);
        LigerSourceToken dog = tokens.stream().filter(t -> t.form.equals("dog")).findFirst().orElseThrow();
        assertEquals("Bill barked\n".length() + 10, dog.start);
        assertEquals(2, tokens.stream().filter(t -> t.form.equals("barked")).count());
    }

    @Test
    void appendingToASequenceMatchesAssemblingAllSentencesAtOnce() throws Exception {
        LinguisticStructure a = syntax("proper-name.json");
        LinguisticStructure b = syntax("black-dog-on-table.json");
        LinguisticStructure c = syntax("subject-relative-clause.json");

        LinguisticStructure incremental = SequenceGraphAssembler.assemble(
                List.of(SequenceGraphAssembler.assemble(List.of(a, b)), c));
        LinguisticStructure atOnce = SequenceGraphAssembler.assemble(List.of(
                syntax("proper-name.json"), syntax("black-dog-on-table.json"), syntax("subject-relative-clause.json")));

        assertTokensMatchText(incremental);
        assertTokensMatchText(atOnce);
        assertEquals(new SourceTokenIndex(atOnce).text(), new SourceTokenIndex(incremental).text());
        assertEquals(
                allTokens(atOnce).stream().map(t -> t.form + "@" + t.start + "-" + t.end).toList(),
                allTokens(incremental).stream().map(t -> t.form + "@" + t.start + "-" + t.end).toList());
    }
}
