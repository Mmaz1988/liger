package de.ukon.liger.analysis.RuleParser;

import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.Solution;
import de.ukon.liger.syntax.GraphConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The protected facts of a deletion rule's left-hand side. {@code =-> 0} deletes every fact its
 * left-hand side matched, so a fact that only says when to delete would be deleted with the rest.
 * A leading {@code +} on a relation label keeps the facts that atom matched:
 *
 * <pre>
 *     #h PRSP #p & #p +IN #x & #x +acc #x =-> 0.
 * </pre>
 *
 * deletes the {@code PRSP} edge and keeps {@code IN} and {@code acc}. The {@code +} goes only on
 * a plain relation label after a node variable ({@code #a +LABEL value}), also inside a chain
 * ({@code #a L1 #b +L2 #c}); it is not supported inside a path such as {@code ^(...)}.
 */
final class ProtectedAtoms {

    // The value is a lookahead, so that in a chain (#a +L1 #b +L2 #c) #b is still there to start the
    // next match.
    private static final Pattern PROTECTED = Pattern.compile("([#*](\\w+))(\\s+)\\+([^\\s()&+]+)(?=\\s+([^\\s()&]+))");
    private static final Pattern NODE_VAR = Pattern.compile("[#*](\\w+)");

    private record Atom(String nodeVariable, String label, String value) {
    }

    private final String query;
    private final List<Atom> atoms;

    private ProtectedAtoms(String query, List<Atom> atoms) {
        this.query = query;
        this.atoms = atoms;
    }

    static ProtectedAtoms of(String left) {
        List<Atom> atoms = new ArrayList<>();
        if (left == null) {
            return new ProtectedAtoms(null, atoms);
        }
        Matcher m = PROTECTED.matcher(left);
        StringBuilder query = new StringBuilder();
        while (m.find()) {
            atoms.add(new Atom(m.group(2), m.group(4), m.group(5)));
            m.appendReplacement(query, Matcher.quoteReplacement(m.group(1) + m.group(3) + m.group(4)));
        }
        m.appendTail(query);
        return new ProtectedAtoms(query.toString(), atoms);
    }

    /** The left-hand side without the {@code +} marks, as the query parser reads it. */
    String query() {
        return query;
    }

    /** Whether {@code candidate} is a fact a protected atom matched in {@code solution}. */
    boolean protects(GraphConstraint candidate, QueryParserResult qpr, Solution solution) {
        for (Atom atom : atoms) {
            if (!atom.label().equals(candidate.getRelationLabel())) {
                continue;
            }
            if (!isBoundTo(qpr, solution, atom.nodeVariable(), candidate.getFsNode())) {
                continue;
            }
            Matcher valueVar = NODE_VAR.matcher(atom.value());
            if (valueVar.matches()) {
                if (isBoundTo(qpr, solution, valueVar.group(1), String.valueOf(candidate.getFsValue()))) {
                    return true;
                }
            } else if (atom.value().startsWith("%")
                    || unquote(atom.value()).equals(unquote(String.valueOf(candidate.getFsValue())))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBoundTo(QueryParserResult qpr, Solution solution, String variable, String node) {
        Map<String, ?> binding = qpr.result.get(solution) == null ? null : qpr.result.get(solution).get(variable);
        return binding != null && binding.containsKey(node);
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
