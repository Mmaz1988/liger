package de.ukon.liger.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.SourceIndexResolverService;
import de.ukon.liger.webservice.rest.SourceTokenIndex;
import de.ukon.liger.webservice.rest.dtos.LigerSourceSpan;
import de.ukon.liger.webservice.rest.dtos.LigerSourceToken;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Fixtures are live LiGER/GSWB output for the lfgxdrt inference grammar, captured 2026-09-17. */
public class SourceIndexResolverTest {

    static LinkedHashMap<String, Object> fixture(String name) throws Exception {
        try (InputStream in = SourceIndexResolverTest.class.getResourceAsStream("/provenance/" + name)) {
            assertNotNull(in, "missing fixture " + name);
            return new ObjectMapper().readValue(in, LinkedHashMap.class);
        }
    }

    @SuppressWarnings("unchecked")
    static LinguisticStructure syntax(String name) throws Exception {
        return LinguisticStructure.parseFromJson((LinkedHashMap<String, Object>) fixture(name).get("syntax"));
    }

    /** Source index of the token with this form (the lowest, if several constructors share it). */
    static int indexOf(SourceTokenIndex index, String form) {
        for (int i = 1; i < 100; i++) {
            if (index.hasSourceIndex(i)
                    && index.tokensFor(List.of(i)).stream().anyMatch(t -> t.form.equals(form))) {
                return i;
            }
        }
        return fail("no source index resolves to '" + form + "'");
    }

    @Test
    void everyMeaningConstructorResolvesToATokenIncludingMorphologicallyAnalysedWords() throws Exception {
        SourceTokenIndex index = new SourceTokenIndex(syntax("black-dog-on-table.json"));
        for (String form : List.of("the", "black", "dog", "on", "table", "barked")) {
            indexOf(index, form);
        }
    }

    @Test
    void offsetsAreZeroBasedAndMatchTheInputText() throws Exception {
        SourceTokenIndex index = new SourceTokenIndex(syntax("black-dog-on-table.json"));
        String text = index.text();
        assertEquals("the black dog on the table barked", text);

        LigerSourceToken dog = index.tokensFor(List.of(indexOf(index, "dog"))).get(0);
        assertEquals(10, dog.start);
        assertEquals(13, dog.end);
        for (int i = 1; i < 100; i++) {
            if (!index.hasSourceIndex(i)) continue;
            for (LigerSourceToken token : index.tokensFor(List.of(i))) {
                assertEquals(token.form, text.substring(token.start, token.end));
            }
        }
    }

    @Test
    void tokensCarryTheirFStructures() throws Exception {
        SourceTokenIndex index = new SourceTokenIndex(syntax("black-dog-on-table.json"));
        LigerSourceToken dog = index.tokensFor(List.of(indexOf(index, "dog"))).get(0);
        LigerSourceToken black = index.tokensFor(List.of(indexOf(index, "black"))).get(0);
        assertFalse(dog.fsNodes.isEmpty());
        assertFalse(black.fsNodes.isEmpty());
        // black is an ADJUNCT inside the noun phrase dog heads
        assertTrue(index.fsSubtree(dog.fsNodes.get(0)).containsAll(black.fsNodes));
        assertFalse(index.fsSubtree(black.fsNodes.get(0)).containsAll(dog.fsNodes));
    }

    @Test
    void groupedSpanKeepsItsTokensAndOuterBounds() throws Exception {
        LinguisticStructure structure = syntax("black-dog-on-table.json");
        SourceTokenIndex index = new SourceTokenIndex(structure);
        List<Integer> group = List.of(indexOf(index, "table"), indexOf(index, "the"), indexOf(index, "black"));

        LigerSourceSpan span = new SourceIndexResolverService().resolve(structure, group);

        // "dog on the" between black and table are other constructors' words: elided, not run in.
        assertEquals("the black … table", span.text);
        assertEquals(0, span.start);
        assertEquals(26, span.end);
        assertEquals(List.of("the", "black", "table"), span.tokens.stream().map(t -> t.form).toList());
    }

    @Test
    void unknownIndexResolvesToAnEmptySpan() throws Exception {
        LigerSourceSpan span = new SourceIndexResolverService().resolve(syntax("proper-name.json"), List.of(999));
        assertEquals("", span.text);
        assertNull(span.start);
        assertTrue(span.tokens.isEmpty());
    }

    /** "The PC-6082 is faster than every ITEL computer." (lfgxdrt inference grammar with the
     *  degree rules, from a stored regression session, 2026-10-06). ITEL's meaning comes from a
     *  LiGER rule, so it has no source index, and the quantifier's label read "every computer". */
    @Test
    void wordsWithoutAConstructorStayInsideAGroupsSpan() throws Exception {
        LinguisticStructure structure = syntax("comparative-every.json");
        SourceTokenIndex index = new SourceTokenIndex(structure);
        List<String> unindexed = index.unindexedTokens().stream().map(t -> t.form).toList();
        assertTrue(unindexed.contains("ITEL"), "ITEL has no constructor of its own: " + unindexed);

        LigerSourceSpan quantifier = new SourceIndexResolverService().resolve(structure,
                List.of(indexOf(index, "every"), indexOf(index, "computer")));
        assertEquals("every ITEL computer", quantifier.text);
        assertEquals(List.of("every", "computer"), quantifier.tokens.stream().map(t -> t.form).toList());

        LigerSourceSpan subject = new SourceIndexResolverService().resolve(structure,
                List.of(indexOf(index, "The"), indexOf(index, "PC-6082")));
        assertEquals("The PC-6082", subject.text);
    }
}
