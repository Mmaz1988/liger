package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

import java.util.HashMap;
import java.util.Set;

public class ComparisonExpression extends QueryExpression {

    private final Value left;
    private final Comparison middle;
    private final Value right;

    public ComparisonExpression(Value left, Comparison middle, Value right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
        setParser(middle.getParser());
        calculateSolutions();
    }

    @Override
    public void calculateSolutions() {
        HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();

        for (Set<SolutionKey> key : left.getSolution().keySet()) {
            Integer leftValue = resolveNumericValue(key, left);
            Integer rightValue = resolveNumericValue(key, right);

            if (leftValue == null || rightValue == null) {
                continue;
            }

            boolean matches = switch (middle.operator) {
                case LT -> leftValue < rightValue;
                case GT -> leftValue > rightValue;
                case LE -> leftValue <= rightValue;
                case GE -> leftValue >= rightValue;
            };

            if (matches) {
                out.put(key, left.getSolution().get(key));
            }
        }

        setFsIndices(left.getFsIndices());
        setConjoinedSolutions(left.getConjoinedSolutions());
        setSolution(out);
    }

    private Integer resolveNumericValue(Set<SolutionKey> solutionKey, Value value) {
        String resolved = ValueResolver.resolve(this, solutionKey, value);
        if (resolved == null) {
            return null;
        }

        if ((resolved.startsWith("'") && resolved.endsWith("'")) ||
                (resolved.startsWith("\"") && resolved.endsWith("\""))) {
            resolved = resolved.substring(1, resolved.length() - 1);
        }

        if (!HelperMethods.isInteger(resolved)) {
            throw new IllegalArgumentException("Comparison values must resolve to integers: " + resolved);
        }

        return Integer.parseInt(resolved);
    }
}
