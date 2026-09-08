package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NegationExpression extends QueryExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(NegationExpression.class);

    private final QueryExpression left;
    private final QueryNegation current;

    public NegationExpression(QueryExpression left, QueryNegation current) {
        this.left = left;
        this.current = current;
        setParser(current.getParser());
        setFsIndices(left != null ? left.getFsIndices() : current.getFsIndices());
        calculateSolutions();
    }

    @Override
    public void calculateSolutions() {
        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();

        if (left == null) {
            Solution seed = new Solution();
            if (!matchesNegatedQuery(seed, new HashMap<>())) {
                out.put(seed, new HashMap<>());
            }
            setSolution(out);
            setConjoinedSolutions(new java.util.ArrayList<>());
            return;
        }

        for (Solution key : left.getSolution().keySet()) {
            if (key.isTruthValue()) {
                boolean matched = matchesNegatedQuery(key, left.getSolution().get(key));
                if (matched) {
                    key.setTruthValue(false);
                }
            }
            if (key.isTruthValue()) {
                out.put(key, left.getSolution().get(key));
            }
        }

        setSolution(out);
        setConjoinedSolutions(left.getConjoinedSolutions());
    }

    private boolean matchesNegatedQuery(Solution outerKey,
                                        HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> seedBinding) {
        try {
            QueryParser parser = getParser();
            HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> seedSolution = new HashMap<>();
            if (outerKey != null) {
                seedSolution.put(outerKey.copy(), copyBinding(seedBinding));
            }

            QueryParser nestedParser = new QueryParser(
                    current.getNegatedQuery(),
                    parser.getFsIndices() == null ? new HashMap<>() : new HashMap<>(parser.getFsIndices()),
                    parser.cp,
                    parser.getTemplateRegistry(),
                    parser.getHierarchyRegistry());

            Set<String> queryVars = extractQueryVariables(current.getNegatedQuery());

            boolean matched = nestedParser.parseQueryWithTemplates(
                            current.getNegatedQuery(),
                            seedSolution.isEmpty() ? null : seedSolution)
                    .stream()
                    .flatMap(result -> result.result.keySet().stream())
                    .anyMatch(innerSolution -> innerSolution.isTruthValue() && matchesSeedBinding(innerSolution, outerKey, queryVars));
            if (matched) {
                LOGGER.trace("Negated query matched: {}", current.getNegatedQuery());
            }
            return matched;
        } catch (RuntimeException e) {
            LOGGER.error("Invalid negated query '{}': {}", current.getNegatedQuery(), e.getMessage(), e);
            // An evaluation error must not turn negation into a successful match.
            return true;
        }
    }

    private HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> copyBinding(
            HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> source) {
        HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> copy = new HashMap<>();
        if (source == null) {
            return copy;
        }

        for (java.util.Map.Entry<String, HashMap<String, HashMap<Integer, GraphConstraint>>> variable : source.entrySet()) {
            HashMap<String, HashMap<Integer, GraphConstraint>> references = new HashMap<>();
            for (java.util.Map.Entry<String, HashMap<Integer, GraphConstraint>> reference : variable.getValue().entrySet()) {
                references.put(reference.getKey(), new HashMap<>(reference.getValue()));
            }
            copy.put(variable.getKey(), references);
        }
        return copy;
    }

    private Set<String> extractQueryVariables(String query) {
        Set<String> vars = new HashSet<>();
        if (query == null) {
            return vars;
        }

        Matcher matcher = Pattern.compile("#([A-Za-z][A-Za-z0-9_]*)").matcher(query);
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }

        return vars;
    }

    private boolean matchesSeedBinding(Solution inner, Solution outer, Set<String> queryVars) {
        java.util.HashMap<String, String> innerBindings = new java.util.HashMap<>();
        for (SolutionKey key : inner.getSolutionKeys()) {
            innerBindings.put(key.variable, key.reference);
        }

        java.util.HashMap<String, String> outerBindings = new java.util.HashMap<>();
        for (SolutionKey key : outer.getSolutionKeys()) {
            outerBindings.put(key.variable, key.reference);
        }

        for (java.util.Map.Entry<String, String> entry : outerBindings.entrySet()) {
            if (!queryVars.contains(entry.getKey())) {
                continue;
            }
            String innerValue = innerBindings.get(entry.getKey());
            if (innerValue == null) {
                continue;
            }
            if (!innerValue.equals(entry.getValue())) {
                return false;
            }
        }

        return true;
    }
}
