package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class NegationExpression extends QueryExpression {

    private final QueryExpression left;
    private final QueryNegation right;

    public NegationExpression(QueryExpression left, QueryNegation right) {
        this.left = left;
        this.right = right;
        if (left != null) {
            setParser(left.getParser());
            setFsIndices(left.getFsIndices());
            setNodeVar(left.getNodeVar());
            setConjoinedSolutions(left.getConjoinedSolutions());
        } else {
            setParser(right.getParser());
            setFsIndices(right.getFsIndices());
        }
        calculateSolutions();
    }

    @Override
    public void calculateSolutions() {
        HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();

        HashMap<Set<SolutionKey>, HashMap<String, String>> previousBindings = copyValueBindings(getParser().fsValueBindings);

        QueryParserResult negatedResult;
        try {
            negatedResult = getParser().parseQuery(right.getNegatedQueryList());
        } finally {
            getParser().fsValueBindings = previousBindings;
        }

        try {
            if (left == null) {
                if (negatedResult.result.isEmpty()) {
                    out.put(new HashSet<>(), new HashMap<>());
                }
            } else {
                for (Set<SolutionKey> key : left.getSolution().keySet()) {
                    boolean blocked = false;

                    for (Set<SolutionKey> negatedKey : negatedResult.result.keySet()) {
                        if (areCompatible(key, negatedKey)) {
                            blocked = true;
                            break;
                        }
                    }

                    if (!blocked) {
                        out.put(key, left.getSolution().get(key));
                    }
                }
            }
        } finally {
            getParser().fsValueBindings = previousBindings;
        }

        setSolution(out);
        if (left != null) {
            setConjoinedSolutions(left.getConjoinedSolutions());
        }
    }

    private boolean areCompatible(Set<SolutionKey> leftKey, Set<SolutionKey> rightKey) {
        HashMap<String, Set<String>> leftBindings = toBindingMap(leftKey);
        HashMap<String, Set<String>> rightBindings = toBindingMap(rightKey);

        if (leftBindings.isEmpty() || rightBindings.isEmpty()) {
            return true;
        }

        Set<String> commonKeys = new HashSet<>(leftBindings.keySet());
        commonKeys.retainAll(rightBindings.keySet());

        if (commonKeys.isEmpty()) {
            return true;
        }

        for (String key : commonKeys) {
            if (!leftBindings.get(key).equals(rightBindings.get(key))) {
                return false;
            }
        }

        return true;
    }

    private HashMap<String, Set<String>> toBindingMap(Set<SolutionKey> keySet) {
        HashMap<String, Set<String>> bindings = new HashMap<>();

        for (SolutionKey key : keySet) {
            bindings.computeIfAbsent(key.variable, ignored -> new HashSet<>()).add(key.reference);
        }

        return bindings;
    }

    private HashMap<Set<SolutionKey>, HashMap<String, String>> copyValueBindings(
            HashMap<Set<SolutionKey>, HashMap<String, String>> input) {

        HashMap<Set<SolutionKey>, HashMap<String, String>> copy = new HashMap<>();

        for (Set<SolutionKey> key : input.keySet()) {
            copy.put(key, new HashMap<>(input.get(key)));
        }

        return copy;
    }
}
