package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

import java.util.HashMap;
import java.util.Set;

final class ValueResolver {

    private ValueResolver() {
    }

    static String resolve(QueryExpression context, Set<SolutionKey> solutionKey, Value value) {
        if (value.idRef) {
            String reference = resolveNodeReference(solutionKey, value.idVar);
            if (reference == null) {
                return null;
            }
            if (!HelperMethods.isInteger(reference)) {
                throw new IllegalArgumentException("id(" + value.idVar + ") must resolve to a numeric node id");
            }
            return reference;
        }

        if (value.var) {
            return lookupBinding(context, solutionKey, value.getQuery());
        }

        return value.getQuery();
    }

    static Integer resolveId(QueryExpression context, Set<SolutionKey> solutionKey, Value value) {
        String resolved = resolve(context, solutionKey, value);
        if (resolved == null) {
            return null;
        }
        if (!HelperMethods.isInteger(resolved)) {
            throw new IllegalArgumentException("Comparison values must resolve to integers: " + resolved);
        }
        return Integer.parseInt(resolved);
    }

    private static String resolveNodeReference(Set<SolutionKey> solutionKey, String variable) {
        if (variable == null || !variable.matches("#[a-z]")) {
            throw new IllegalArgumentException("id() requires a variable reference of the form #[a-z]: " + variable);
        }

        String normalized = variable.substring(1);
        for (SolutionKey key : solutionKey) {
            if (normalized.equals(key.variable)) {
                return key.reference;
            }
        }
        return null;
    }

    private static String lookupBinding(QueryExpression context, Set<SolutionKey> solutionKey, String valueVar) {
        HashMap<String, String> exactMatch = context.getParser().fsValueBindings.get(solutionKey);
        if (exactMatch != null && exactMatch.containsKey(valueVar)) {
            return exactMatch.get(valueVar);
        }

        String bestMatch = null;
        int bestSize = -1;

        for (Set<SolutionKey> candidate : context.getParser().fsValueBindings.keySet()) {
            if (solutionKey.containsAll(candidate) && candidate.size() > bestSize) {
                HashMap<String, String> bindings = context.getParser().fsValueBindings.get(candidate);
                if (bindings != null && bindings.containsKey(valueVar)) {
                    bestMatch = bindings.get(valueVar);
                    bestSize = candidate.size();
                }
            }
        }

        return bestMatch;
    }
}
