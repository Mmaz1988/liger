package de.ukon.liger.test;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A word in a query is an attribute or a value by its position, not by what the structure holds:
 * after a node or at the start of a query or conjunct it is an attribute, anywhere else a value. UD's tags
 * and relations share names (UPOS AUX and the relation AUX, UPOS DET and DET), so a word that is
 * a value somewhere in the structure may be an attribute in the query, and the reverse.
 */
class QueryTokenPositionTest {

    private static GraphConstraint fact(String node, String label, String value) {
        GraphConstraint constraint = new GraphConstraint();
        constraint.setFsNode(node);
        constraint.setRelationLabel(label);
        constraint.setFsValue(value);
        constraint.setReading(new LinkedHashSet<>(Set.of(new ChoiceVar("1"))));
        return constraint;
    }

    private static LinguisticStructure structure(GraphConstraint... facts) {
        LinguisticStructure fs = new LinguisticStructure("S0", "", new ArrayList<>(List.of(facts)), new ChoiceSpace());
        fs.annotation = List.of();
        return fs;
    }

    private static QueryParserResult query(String query, LinguisticStructure fs) {
        QueryParser qp = new QueryParser(query, fs);
        return qp.parseQuery(qp.getQueryList());
    }

    /** "Butch is crazy.": "is" is tagged AUX but attached by cop, so AUX is only a value. */
    private static LinguisticStructure copula() {
        return structure(
                fact("w3", "COP", "w2"),
                fact("w2", "UPOS", "AUX"),
                fact("w2", "VERBFORM", "Fin"));
    }

    /** "Kim has left.": "has" is tagged AUX and attached by aux, so AUX is both. */
    private static LinguisticStructure auxiliary() {
        return structure(
                fact("w3", "AUX", "w2"),
                fact("w2", "UPOS", "AUX"),
                fact("w2", "VERBFORM", "Fin"));
    }

    @Test
    void anAttributeTheStructureHasOnlyAsAValueMatchesNothing() {
        QueryParserResult result = query("#v AUX #a & #a VERBFORM Fin", copula());

        assertFalse(result.isSuccess);
    }

    @Test
    void anAttributeMatchesWhenTheStructureAlsoHasItAsAValue() {
        QueryParserResult result = query("#v AUX #a & #a VERBFORM Fin", auxiliary());

        assertTrue(result.isSuccess);
        assertEquals(1, result.result.size());
    }

    @Test
    void aValueMatchesWhenTheStructureAlsoHasItAsAnAttribute() {
        QueryParserResult result = query("#v AUX #a & #a UPOS AUX", auxiliary());

        assertTrue(result.isSuccess);
        assertEquals(1, result.result.size());
    }

    @Test
    void aQueryMayStartWithAnAttribute() {
        assertTrue(query("VERBFORM Fin", auxiliary()).isSuccess);
        assertFalse(query("AUX w2", copula()).isSuccess);
    }

    @Test
    void aChainedNodeIsFollowedByAnAttribute() {
        QueryParserResult result = query("#v AUX #a UPOS AUX", auxiliary());

        assertTrue(result.isSuccess);
    }
}
