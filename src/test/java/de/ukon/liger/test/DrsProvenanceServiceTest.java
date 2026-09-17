package de.ukon.liger.test;

import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.DrsProvenanceService;
import de.ukon.liger.webservice.rest.dtos.LigerDrsProvenanceResponse;
import de.ukon.liger.webservice.rest.dtos.LigerProvenanceCondition;
import de.ukon.liger.webservice.rest.dtos.LigerProvenanceReferent;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static de.ukon.liger.test.SourceIndexResolverTest.fixture;
import static org.junit.jupiter.api.Assertions.*;

/** Fixtures are live LiGER/GSWB output for the lfgxdrt inference grammar, captured 2026-09-17.
 *  GSWB's node ids and condition order are not stable across runs, so referents are looked up
 *  by the conditions on them rather than by id or label. */
public class DrsProvenanceServiceTest {

    @SuppressWarnings("unchecked")
    static LigerDrsProvenanceResponse describe(String name) throws Exception {
        LinkedHashMap<String, Object> fixture = fixture(name);
        return new DrsProvenanceService().describe(
                LinguisticStructure.parseFromJson((LinkedHashMap<String, Object>) fixture.get("syntax")),
                LinguisticStructure.parseFromJson((LinkedHashMap<String, Object>) fixture.get("drs")));
    }

    static LigerProvenanceCondition condition(LigerDrsProvenanceResponse response, String symbol) {
        return response.conditions.stream().filter(c -> symbol.equals(c.symbol)).findFirst()
                .orElseGet(() -> fail("no condition " + symbol));
    }

    /** The referent that is the first argument of the given one-place condition. */
    static LigerProvenanceReferent referentOf(LigerDrsProvenanceResponse response, String symbol) {
        String nodeId = condition(response, symbol).args.get(0);
        return response.referents.stream().filter(r -> nodeId.equals(r.nodeId)).findFirst()
                .orElseGet(() -> fail("no referent " + nodeId));
    }

    @Test
    void conditionsMapToTheirOwnTokens() throws Exception {
        LigerDrsProvenanceResponse response = describe("black-dog-on-table.json");

        assertEquals("the black dog on the table barked", response.text);
        assertEquals("dog", condition(response, "dog").text);
        assertArrayEquals(new int[]{10, 13}, condition(response, "dog").spans.get(0));
        assertEquals("black", condition(response, "black").text);
        assertEquals("on", condition(response, "on").text);
        assertEquals("table", condition(response, "table").text);
        assertEquals("barked", condition(response, "bark").text);
        assertEquals("barked", condition(response, "arg1").text);
        assertTrue(response.warnings.isEmpty(), () -> "unexpected warnings " + response.warnings);
    }

    @Test
    void referentDescriptionIsItsNounPhraseIncludingModifiersButNotThePredication() throws Exception {
        LigerDrsProvenanceResponse response = describe("black-dog-on-table.json");

        LigerProvenanceReferent dog = referentOf(response, "dog");
        assertEquals("the black dog on the table", dog.text);
        assertEquals("the black dog on the table", dog.phraseText);
        assertEquals(1, dog.spans.size());
        assertArrayEquals(new int[]{0, 26}, dog.spans.get(0));
        // arg1(e,x) is directly on the dog, but belongs to the clause
        assertTrue(dog.directConditionIds.contains(condition(response, "arg1").nodeId));
        assertFalse(dog.descriptionConditionIds.contains(condition(response, "arg1").nodeId));
        assertTrue(dog.descriptionConditionIds.contains(condition(response, "table").nodeId));

        LigerProvenanceReferent table = referentOf(response, "table");
        assertEquals("the table", table.text);
        assertFalse(table.descriptionConditionIds.contains(condition(response, "on").nodeId));
    }

    @Test
    void eventDescriptionIsItsClauseAndDirectConditionsStayAvailable() throws Exception {
        LigerDrsProvenanceResponse response = describe("black-dog-on-table.json");

        LigerProvenanceReferent event = referentOf(response, "bark");
        assertEquals("the black dog on the table barked", event.text);
        assertEquals(List.of(condition(response, "bark").nodeId, condition(response, "arg1").nodeId).stream().sorted().toList(),
                event.directConditionIds.stream().sorted().toList());
    }

    @Test
    void subjectRelativeClauseBelongsToTheNounPhrase() throws Exception {
        LigerDrsProvenanceResponse response = describe("subject-relative-clause.json");

        LigerProvenanceReferent dog = referentOf(response, "dog");
        // "that" contributes no condition: a gap in the description, present in the phrase
        assertEquals("the dog barked", dog.text);
        assertEquals(2, dog.spans.size());
        assertEquals("the dog that barked", dog.phraseText);
        assertEquals(1, dog.phraseSpans.size());
        assertEquals("barked", referentOf(response, "bark").text);
    }

    @Test
    void objectRelativeClauseBelongsToTheNounPhraseButNotItsSubject() throws Exception {
        LigerDrsProvenanceResponse response = describe("object-relative-clause.json");

        LigerProvenanceReferent dog = referentOf(response, "dog");
        assertEquals("the dog Bill saw", dog.text);
        assertEquals("the dog that Bill saw", dog.phraseText);
        assertFalse(dog.descriptionConditionIds.contains(
                response.conditions.stream().filter(c -> "bark".equals(c.symbol)).findFirst().orElseThrow().nodeId));
        LigerProvenanceReferent bill = response.referents.stream()
                .filter(r -> "Bill".equals(r.text)).findFirst().orElseGet(() -> fail("no referent for Bill"));
        assertNotNull(bill.fsNode);
    }

    @Test
    void lexicalDescriptionKeepsOnlyOnePlaceConditions() throws Exception {
        LigerDrsProvenanceResponse response = describe("black-dog-on-table.json");

        LigerProvenanceReferent dog = referentOf(response, "dog");
        // black(x), dog(x) -- not on(x,y), table(y) or arg1(e,x)
        assertEquals("black dog", dog.lexicalText);
        assertEquals(List.of(condition(response, "black").nodeId, condition(response, "dog").nodeId).stream().sorted().toList(),
                dog.lexicalConditionIds.stream().sorted().toList());
        assertFalse(dog.lexicalFromReferentSource);
        assertEquals("table", referentOf(response, "table").lexicalText);
        assertEquals("barked", referentOf(response, "bark").lexicalText);
    }

    @Test
    void lexicalDescriptionOfARelativisedNounAndOfANameIsTheWordItself() throws Exception {
        LigerDrsProvenanceResponse response = describe("object-relative-clause.json");

        assertEquals("dog", referentOf(response, "dog").lexicalText);
        assertEquals("saw", referentOf(response, "see").lexicalText);
        LigerProvenanceReferent bill = response.referents.stream()
                .filter(r -> "Bill".equals(r.text)).findFirst().orElseGet(() -> fail("no referent for Bill"));
        assertEquals("Bill", bill.lexicalText);
        assertTrue(bill.lexicalFromReferentSource);
    }

    @Test
    void properNameReferentResolvesThroughItsOwnSource() throws Exception {
        LigerDrsProvenanceResponse response = describe("proper-name.json");

        assertTrue(response.referents.stream().anyMatch(r -> "Bill".equals(r.text)),
                () -> "referent texts: " + response.referents.stream().map(r -> r.text).toList());
        // The name condition x1='bill' is compiled without SRC; that must be reported, not dropped silently.
        assertTrue(response.warnings.stream().anyMatch(w -> w.contains("EQ") && w.contains("no SRC")),
                () -> "warnings: " + response.warnings);
    }
}
