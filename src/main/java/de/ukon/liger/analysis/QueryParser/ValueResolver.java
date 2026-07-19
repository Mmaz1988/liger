package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

final class ValueResolver {

    private static final Pattern ALPHANUMERIC_ID = Pattern.compile("([a-z]+)(\\d+)");

    private ValueResolver() {
    }

    static String resolve(QueryExpression context, Set<SolutionKey> solutionKey, Value value) {
        if (value.idRef) {
            return resolveIdReference(context, solutionKey, value.idVar);
        }

        if (value.var) {
            return lookupBinding(context, solutionKey, value.getQuery());
        }

        return value.getQuery();
    }

    static int compareIds(String left, String right) {
        IdValue leftId = parseId(left);
        IdValue rightId = parseId(right);

        if (leftId.numeric && rightId.numeric) {
            return Integer.compare(leftId.number, rightId.number);
        }

        if (leftId.numeric != rightId.numeric) {
            throw new IllegalArgumentException("Cannot compare mixed id formats: " + left + " vs " + right);
        }

        if (!leftId.prefix.equals(rightId.prefix)) {
            throw new IllegalArgumentException("Cannot compare ids with different prefixes: " + left + " vs " + right);
        }

        return Integer.compare(leftId.number, rightId.number);
    }

    private static String resolveIdReference(QueryExpression context, Set<SolutionKey> solutionKey, String variable) {
        if (variable == null || !(variable.matches("#[a-z]") || variable.matches("%[a-z]"))) {
            throw new IllegalArgumentException("id() requires a variable reference of the form #[a-z] or %[a-z]: " + variable);
        }

        if (variable.startsWith("#")) {
            String normalized = variable.substring(1);
            for (SolutionKey key : solutionKey) {
                if (normalized.equals(key.variable)) {
                    return key.reference;
                }
            }
            return null;
        }

        return lookupBinding(context, solutionKey, variable);
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

    private static IdValue parseId(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot compare null id values");
        }

        if (HelperMethods.isInteger(value)) {
            return new IdValue(true, "", Integer.parseInt(value));
        }

        Matcher matcher = ALPHANUMERIC_ID.matcher(value);
        if (matcher.matches()) {
            return new IdValue(false, matcher.group(1), Integer.parseInt(matcher.group(2)));
        }

        throw new IllegalArgumentException("Unsupported id format: " + value);
    }

    static final class IdValue {
        final boolean numeric;
        final String prefix;
        final int number;

        IdValue(boolean numeric, String prefix, int number) {
            this.numeric = numeric;
            this.prefix = prefix;
            this.number = number;
        }
    }
}
