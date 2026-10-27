package de.ukon.liger.test;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.semantics.SequenceGraphAssembler;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.SourceTokenIndex;
import de.ukon.liger.webservice.rest.dtos.LigerSourceToken;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UD meaning constructors get source indices as XLE's do (xleplusglue
 * docs/plans/UD_PARSING_PLAN.md): one index per constructor, numbered by the position of the
 * word it hangs from -- the node it was created from, followed back to a word -- and resolving
 * to that word's token. "A dog appeared." as the UD rules annotate it.
 */
public class UdSourceIndicesTest {

    private static GraphConstraint fact(String node, String label, String value) {
        GraphConstraint constraint = new GraphConstraint();
        constraint.setFsNode(node);
        constraint.setRelationLabel(label);
        constraint.setFsValue(value);
        constraint.setReading(new LinkedHashSet<>(Set.of(new ChoiceVar("1"))));
        return constraint;
    }

    private static void word(List<GraphConstraint> facts, String node, String form, int start) {
        facts.add(fact(node, "TEXT", form));
        facts.add(fact(node, "token", form));
        facts.add(fact(node, "start", "int(" + start + ")"));
        facts.add(fact(node, "end", "int(" + (start + form.length()) + ")"));
    }

    static LinguisticStructure aDogAppeared() {
        List<GraphConstraint> words = new ArrayList<>();
        word(words, "w1", "A", 1);
        word(words, "w2", "dog", 3);
        word(words, "w3", "appeared", 7);
        words.add(fact("w2", "DET", "w1"));
        words.add(fact("w3", "NSUBJ", "w2"));
        words.add(fact("w0", "ROOT", "w3"));
        LinguisticStructure structure = new LinguisticStructure("S0", "A dog appeared.", words, new ChoiceSpace());
        structure.cp.choices.add(new LinkedHashSet<>(Set.of(new ChoiceVar("1"))));
        structure.syntaxType = LinguisticStructure.UD;
        // In the order the rules add them; the verb's first, the determiner's last.
        List<GraphConstraint> added = structure.annotation;
        added.add(fact("w3", "CLAUSE", "a1"));
        added.add(fact("a1", "GOAL", "a2"));
        added.add(fact("a2", "GLUE", "(\\P.P) : (a1_t -o a2_t)"));
        // Added to the clause node later: it must not make the noun the goal's anchor.
        added.add(fact("w2", "SCOPE", "a1"));
        added.add(fact("w3", "EV", "a3"));
        added.add(fact("a3", "GLUE", "(\\v.([],[appear(v)])) : (a3_v -o a3_t)"));
        added.add(fact("a3", "CLOSURE", "a4"));
        added.add(fact("a4", "GLUE", "(\\V.([e:v],[])+V@e) : ((a3_v -o a3_t) -o a1_t)"));
        added.add(fact("w2", "NOUN-SEM", "a6"));
        added.add(fact("a6", "GLUE", "(\\x.([],[dog(x)])) : (w2_e -o w2_t)"));
        added.add(fact("w1", "DET-SEM", "a7"));
        added.add(fact("a7", "GLUE", "(\\P.(\\Q.([x],[])+P@x+Q@x)) : ((w2_e -o w2_t) -o ((w2_e -o a1_t) -o a1_t))"));
        return structure;
    }

    private static Map<String, String> synIds(LinguisticStructure structure) {
        Map<String, String> ids = new LinkedHashMap<>();
        for (GraphConstraint constraint : structure.returnFullGraph()) {
            if ("SYN-ID".equals(constraint.getRelationLabel())) {
                ids.put(constraint.getFsNode(), String.valueOf(constraint.getFsValue()));
            }
        }
        return ids;
    }

    @Test
    void numbersEachConstructorByTheWordItHangsFrom() {
        LinguisticStructure structure = aDogAppeared();
        new GlueSemantics().annotateSyntheticMcIndices(structure);

        // "A" 1, "dog" 2, then the verb's three in the order the rules added them.
        assertEquals(Map.of("a7", "i1", "a6", "i2", "a2", "i3", "a3", "i4", "a4", "i5"), synIds(structure));
        // And each says which word it hangs from, for linking referents to the syntax.
        Map<String, String> anchors = new LinkedHashMap<>();
        structure.returnFullGraph().stream().filter(c -> "ANCHOR".equals(c.getRelationLabel()))
                .forEach(c -> anchors.put(c.getFsNode(), String.valueOf(c.getFsValue())));
        assertEquals(Map.of("a7", "w1", "a6", "w2", "a2", "w3", "a3", "w3", "a4", "w3"), anchors);
    }

    @Test
    void rendersTheIndicesInTheMeaningConstructors() {
        LinguisticStructure structure = aDogAppeared();
        GlueSemantics semantics = new GlueSemantics();
        semantics.annotateSyntheticMcIndices(structure);
        String mcs = semantics.returnMeaningConstructors(structure, false, false, true);

        assertTrue(mcs.contains("[1] (\\P.(\\Q.([x],[])+P@x+Q@x))"), mcs);
        assertTrue(mcs.contains("[2] (\\x.([],[dog(x)]))"), mcs);
        assertTrue(mcs.contains("[4] (\\v.([],[appear(v)]))"), mcs);
    }

    @Test
    void anIndexResolvesToTheTokenOfItsWord() {
        LinguisticStructure structure = aDogAppeared();
        new GlueSemantics().annotateSyntheticMcIndices(structure);
        SourceTokenIndex tokens = new SourceTokenIndex(structure);

        assertEquals(List.of("A"), tokens.tokensFor(List.of(1)).stream().map(token -> token.form).toList());
        assertEquals(List.of("appeared"), tokens.tokensFor(List.of(5)).stream().map(token -> token.form).toList());
        assertTrue(tokens.unindexedTokens().isEmpty());
    }

    @Test
    void aSequenceOfUdPartsIsUdAndKeepsItsIndicesApart() {
        LinguisticStructure first = aDogAppeared();
        LinguisticStructure second = aDogAppeared();
        GlueSemantics semantics = new GlueSemantics();
        semantics.annotateSyntheticMcIndices(first);
        semantics.annotateSyntheticMcIndices(second);

        LinguisticStructure sequence = SequenceGraphAssembler.assemble(List.of(first, second));

        assertTrue(sequence.isUd());
        Set<String> indices = synIds(sequence).values().stream().collect(Collectors.toSet());
        assertEquals(10, indices.size(), indices.toString());
        SourceTokenIndex tokens = new SourceTokenIndex(sequence);
        assertEquals(List.of("A"), tokens.tokensFor(List.of(6)).stream().map(token -> token.form).toList());
        LigerSourceToken a = tokens.tokensFor(List.of(6)).get(0);
        assertEquals("A", tokens.text().substring(a.start, a.end));
    }
}
