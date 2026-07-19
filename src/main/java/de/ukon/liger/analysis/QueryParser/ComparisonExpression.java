package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;

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
            String leftValue = resolveComparableValue(key, left);
            String rightValue = resolveComparableValue(key, right);

            if (leftValue == null || rightValue == null) {
                continue;
            }

            int comparison;
            try {
                comparison = ValueResolver.compareIds(leftValue, rightValue);
            } catch (IllegalArgumentException e) {
                continue;
            }
            boolean matches = switch (middle.operator) {
                case LT -> comparison < 0;
                case GT -> comparison > 0;
                case LE -> comparison <= 0;
                case GE -> comparison >= 0;
            };

            if (matches) {
                out.put(key, left.getSolution().get(key));
            }
        }

        setFsIndices(left.getFsIndices());
        setConjoinedSolutions(left.getConjoinedSolutions());
        setSolution(out);
    }

    private String resolveComparableValue(Set<SolutionKey> solutionKey, Value value) {
        String resolved = ValueResolver.resolve(this, solutionKey, value);
        if (resolved == null) {
            return null;
        }

        if ((resolved.startsWith("'") && resolved.endsWith("'")) ||
                (resolved.startsWith("\"") && resolved.endsWith("\""))) {
            resolved = resolved.substring(1, resolved.length() - 1);
        }

        return resolved;
    }
}
