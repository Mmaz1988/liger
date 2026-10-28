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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A conjunct without variables ({@code TENSE Past}) is a condition on the whole structure: some
 * fact has that attribute (and value). It may stand anywhere in a query and binds nothing.
 */
class GroundConjunctTest {

    private static GraphConstraint fact(String node, String label, String value) {
        GraphConstraint constraint = new GraphConstraint();
        constraint.setFsNode(node);
        constraint.setRelationLabel(label);
        constraint.setFsValue(value);
        constraint.setReading(new LinkedHashSet<>(Set.of(new ChoiceVar("1"))));
        return constraint;
    }

    /** "Kim has left." */
    private static LinguisticStructure kimHasLeft() {
        LinguisticStructure fs = new LinguisticStructure("S0", "", new ArrayList<>(List.of(
                fact("w3", "NSUBJ", "w1"),
                fact("w3", "AUX", "w2"),
                fact("w2", "UPOS", "AUX"),
                fact("w2", "VERBFORM", "Fin"))), new ChoiceSpace());
        fs.annotation = List.of();
        return fs;
    }

    private static QueryParserResult query(String query) {
        QueryParser qp = new QueryParser(query, kimHasLeft());
        return qp.parseQuery(qp.getQueryList());
    }

    @Test
    void aConjunctWithoutVariablesMayFollowOthers() {
        QueryParserResult result = query("#v NSUBJ #a & VERBFORM Fin");

        assertTrue(result.isSuccess);
        assertEquals(1, result.result.size());
    }

    @Test
    void itFailsTheQueryWhenNoFactHasIt() {
        assertFalse(query("#v NSUBJ #a & TENSE Past").isSuccess);
        assertFalse(query("#v NSUBJ #a & VERBFORM Inf").isSuccess);
    }

    @Test
    void itMayStandBetweenConjunctsWithVariables() {
        QueryParserResult result = query("#v NSUBJ #a & UPOS AUX & #v AUX #b & #b VERBFORM Fin");

        assertTrue(result.isSuccess);
        assertEquals(1, result.result.size());
    }

    @Test
    void itBindsNothing() {
        QueryParserResult withIt = query("#v AUX #a & VERBFORM Fin");
        QueryParserResult without = query("#v AUX #a");

        assertEquals(bindings(without), bindings(withIt));
    }

    /** Each solution as its variables and the nodes they are bound to. */
    private static List<Map<String, Set<String>>> bindings(QueryParserResult result) {
        List<Map<String, Set<String>>> bindings = new ArrayList<>();
        for (var solution : result.result.values()) {
            Map<String, Set<String>> binding = new TreeMap<>();
            solution.forEach((variable, nodes) -> binding.put(variable, new TreeSet<>(nodes.keySet())));
            bindings.add(binding);
        }
        return bindings;
    }

    @Test
    void aQueryOfConjunctsWithoutVariablesStillMatchesFacts() {
        assertTrue(query("VERBFORM Fin").isSuccess);
        assertFalse(query("VERBFORM Inf").isSuccess);
    }
}
