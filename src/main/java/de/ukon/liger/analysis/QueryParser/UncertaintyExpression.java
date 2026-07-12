/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UncertaintyExpression extends QueryExpression {

    private static final int MAX_UNCERTAINTY_REPEAT = 16;

    public String uncertaintyExpression;

    private QueryExpression left;
    private Uncertainty middle;
    private QueryExpression right;
    private List<String> gf = Arrays.asList("OBL", "OBJ", "SUBJ", "COMP", "XCOMP", "OBJ-TH", "XCOMP-PRED");
    private Set<ChoiceVar> choices = new HashSet<>();

    private enum OffPathDirection {
        TO_VALUE,
        TO_ORIGIN
    }

    private static class PathAtom {
        private final Set<String> labels;
        private final Quantifier quantifier;
        private final List<OffPathConstraint> offPathConstraints;

        private PathAtom(Set<String> labels, Quantifier quantifier, List<OffPathConstraint> offPathConstraints) {
            this.labels = labels;
            this.quantifier = quantifier;
            this.offPathConstraints = offPathConstraints;
        }

        private String render(String label) {
            StringBuilder out = new StringBuilder(label);
            for (OffPathConstraint constraint : offPathConstraints) {
                out.append(":").append(constraint.render());
            }
            return out.toString();
        }
    }

    private static class OffPathConstraint {
        private final boolean negated;
        private final OffPathDirection direction;
        private final String attribute;
        private final String value;

        private OffPathConstraint(boolean negated, OffPathDirection direction, String attribute, String value) {
            this.negated = negated;
            this.direction = direction;
            this.attribute = attribute;
            this.value = value;
        }

        private String render() {
            StringBuilder out = new StringBuilder();
            if (negated) {
                out.append("~");
            }
            out.append("(");
            out.append(direction == OffPathDirection.TO_VALUE ? "->" : "<-");
            out.append(" ");
            out.append(attribute);
            if (value != null && !value.isBlank()) {
                out.append(" ").append(value);
            }
            out.append(")");
            return out.toString();
        }
    }

    private enum Quantifier {
        EXACT,
        ONE_OR_MORE,
        ZERO_OR_MORE
    }

    public UncertaintyExpression(QueryExpression left, Uncertainty middle, QueryExpression right) {

        setNodeVar(left.getNodeVar());
        this.left = left;
        this.middle = middle;
        this.right = right;
        setParser(this.right.getParser());
        setFsIndices(new HashMap<>());

        calculateSolutions();
    }


    @Override
    public void calculateSolutions() {

        HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();
        List<String> searchQueries = getExpandedQueries();


        for (Set<SolutionKey> key : left.getSolution().keySet()) {
            String nodeVar = left.getNodeVar();
            String nodeRef = left.getSolution().get(key).get(nodeVar).keySet().stream().findAny().get();
            HashMap<Integer, GraphConstraint> boundIndices = new HashMap<>();

            for (Integer key2 : middle.getFsIndices().keySet()) {
                if (middle.getFsIndices().get(key2).getFsNode().equals(nodeRef)) {
                    boundIndices.put(key2, middle.getFsIndices().get(key2));
                }
            }

            HashMap<Integer, GraphConstraint> uncertainty = new HashMap<>();

            for (String searchQuery : searchQueries) {
                if (searchQuery.isBlank()) {
                    Set<String> usedKeys = new HashSet<>();
                    usedKeys.add(nodeRef);

                    HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out2 =
                            mapUsedKeys(usedKeys, uncertainty, right.getNodeVar());

                    right.setSolution(out2);

                    for (Set<SolutionKey> key2 : out2.keySet()) {
                        HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new HashMap<>();

                        for (String key3 : left.getSolution().get(key).keySet()) {
                            binding.put(key3, left.getSolution().get(key).get(key3));
                        }

                        binding.put(right.getNodeVar(), out2.get(key2).get(right.getNodeVar()));

                        Set<SolutionKey> newKey = new HashSet<>();
                        newKey.addAll(key);
                        newKey.addAll(key2);

                        out.put(newKey, binding);

                        for (String bkey2 : binding.get(right.getNodeVar()).keySet()) {
                            getFsIndices().putAll(binding.get(right.getNodeVar()).get(bkey2));
                        }
                    }

                    continue;
                }

                if (!middle.insideOut) {
                    uncertainty = searchUncertainty(searchQuery, boundIndices);
                } else {
                    uncertainty = searchInsideOutUncertainty(searchQuery, boundIndices);
                }

                if (uncertainty.isEmpty() && allowsZeroLength(searchQuery)) {
                    Set<String> usedKeys = new HashSet<>();
                    usedKeys.add(nodeRef);

                    HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out2 =
                            mapUsedKeys(usedKeys, uncertainty, right.getNodeVar());

                    right.setSolution(out2);

                    for (Set<SolutionKey> key2 : out2.keySet()) {
                        HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new HashMap<>();

                        for (String key3 : left.getSolution().get(key).keySet()) {
                            binding.put(key3, left.getSolution().get(key).get(key3));
                        }

                        binding.put(right.getNodeVar(), out2.get(key2).get(right.getNodeVar()));

                        Set<SolutionKey> newKey = new HashSet<>();
                        newKey.addAll(key);
                        newKey.addAll(key2);

                        out.put(newKey, binding);

                        for (String bkey2 : binding.get(right.getNodeVar()).keySet()) {
                            getFsIndices().putAll(binding.get(right.getNodeVar()).get(bkey2));
                        }
                    }

                    continue;
                }

                if (!uncertainty.keySet().isEmpty()) {


                    Set<String> usedKeys = new HashSet<>();

                    for (Integer key3 : uncertainty.keySet()) {
                        if (right.getFsIndices().containsKey(key3)) {
                            usedKeys.add(right.getFsIndices().get(key3).getFsNode());
                        }
                    }

                    HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out2 =
                            mapUsedKeys(usedKeys, uncertainty, right.getNodeVar());

                    right.setSolution(out2);

                    for (Set<SolutionKey> key2 : out2.keySet()) {

                        HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new HashMap<>();

                        for (String key3 : left.getSolution().get(key).keySet()) {
                            binding.put(key3, left.getSolution().get(key).get(key3));

                        }

                        binding.put(right.getNodeVar(), out2.get(key2).get(right.getNodeVar()));


                        Set<SolutionKey> newKey = new HashSet<>();
                        newKey.addAll(key);
                        newKey.addAll(key2);

                        out.put(newKey, binding);

                        //Update fsIndices

                        for (String bkey2 : binding.get(right.getNodeVar()).keySet()) {
                            getFsIndices().putAll(binding.get(right.getNodeVar()).get(bkey2));
                        }


                    }

                }
            }
        }

        setConjoinedSolutions(left.getConjoinedSolutions());
        setSolution(out);
        //  getParser().fsNodeBindings = out;
    }

    private List<String> getExpandedQueries() {
        String query = middle.getQuery() == null ? "" : middle.getQuery().trim();
        if (query.isBlank()) {
            return Collections.singletonList("");
        }

        TemplateRegistry registry = middle.getParser() != null ? middle.getParser().getTemplateRegistry() : null;
        int maxRepeat = Math.min(4, Math.max(1, middle.getFsIndices().size()));

        List<PathAtom> atoms = parsePathQuery(query, registry);
        List<String> expanded = new ArrayList<>();
        expandPathQueries(atoms, 0, new ArrayList<>(), maxRepeat, expanded);

        return new ArrayList<>(new LinkedHashSet<>(expanded));
    }

    private void expandPathQueries(List<PathAtom> atoms, int index, List<String> prefix, int maxRepeat, List<String> out) {
        if (index >= atoms.size()) {
            out.add(String.join(">", prefix));
            return;
        }

        PathAtom atom = atoms.get(index);
        int minRepeat = atom.quantifier == Quantifier.ZERO_OR_MORE ? 0 : 1;
        int max = atom.quantifier == Quantifier.EXACT ? 1 : maxRepeat;

        for (int repeat = minRepeat; repeat <= max; repeat++) {
            if (repeat == 0) {
                expandPathQueries(atoms, index + 1, prefix, maxRepeat, out);
                continue;
            }

            expandLabelCombinations(new ArrayList<>(atom.labels), repeat, new ArrayList<>(), combination -> {
                for (String label : combination) {
                    prefix.add(atom.render(label));
                }
                expandPathQueries(atoms, index + 1, prefix, maxRepeat, out);
                for (int i = 0; i < combination.size(); i++) {
                    prefix.remove(prefix.size() - 1);
                }
            });
        }
    }

    private void expandLabelCombinations(List<String> labels, int repeat, List<String> current, java.util.function.Consumer<List<String>> consumer) {
        if (current.size() == repeat) {
            consumer.accept(new ArrayList<>(current));
            return;
        }

        for (String label : labels) {
            current.add(label);
            expandLabelCombinations(labels, repeat, current, consumer);
            current.remove(current.size() - 1);
        }
    }

    private List<PathAtom> parsePathQuery(String query) {
        TemplateRegistry registry = middle.getParser() != null ? middle.getParser().getTemplateRegistry() : null;
        return parsePathQuery(query, registry);
    }

    private List<PathAtom> parsePathQuery(String query, TemplateRegistry registry) {
        List<PathAtom> out = new ArrayList<>();

        for (String rawSegment : splitTopLevel(query, '>')) {
            String segment = rawSegment.trim();
            List<String> segmentParts = splitSegmentAndOffPaths(segment);
            String basePart = segmentParts.get(0).trim();
            Quantifier quantifier = Quantifier.EXACT;
            if (basePart.endsWith("*")) {
                basePart = basePart.substring(0, basePart.length() - 1).trim();
                quantifier = Quantifier.ZERO_OR_MORE;
            } else if (basePart.endsWith("+")) {
                basePart = basePart.substring(0, basePart.length() - 1).trim();
                quantifier = Quantifier.ONE_OR_MORE;
            }

            Set<String> labels = new LinkedHashSet<>();
            if (basePart.startsWith("@")) {
                String templateName = basePart.substring(1).trim();
                if (registry == null) {
                    throw new IllegalArgumentException("Template registry required for path template: " + basePart);
                }
                QueryTemplate template = registry.getTemplate(templateName);
                if (template == null) {
                    throw new IllegalArgumentException("Unknown template: " + templateName);
                }
                for (List<String> alternative : template.getAlternatives()) {
                    if (alternative.size() != 1) {
                        throw new IllegalArgumentException("Path templates must expand to one label: " + templateName);
                    }
                    labels.add(alternative.get(0));
                }
            } else {
                labels.add(basePart);
            }

            List<OffPathConstraint> offPathConstraints = new ArrayList<>();
            for (int i = 1; i < segmentParts.size(); i++) {
                String offPath = segmentParts.get(i).trim();
                if (!offPath.isBlank()) {
                    offPathConstraints.add(parseOffPathConstraint(offPath));
                }
            }

            out.add(new PathAtom(labels, quantifier, offPathConstraints));
        }

        return out;
    }

    private List<String> splitSegmentAndOffPaths(String segment) {
        List<String> out = new ArrayList<>();
        if (segment == null || segment.isBlank()) {
            return out;
        }

        StringBuilder current = new StringBuilder();
        int parenDepth = 0;
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);

            if (inQuote) {
                current.append(c);
                if (c == quoteChar) {
                    inQuote = false;
                }
                continue;
            }

            if (c == '\'' || c == '"') {
                inQuote = true;
                quoteChar = c;
                current.append(c);
                continue;
            }

            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth = Math.max(0, parenDepth - 1);
            }

            if (c == ':' && parenDepth == 0 && startsOffPathClause(segment, i + 1)) {
                out.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        out.add(current.toString());
        return out;
    }

    private boolean startsOffPathClause(String segment, int index) {
        int i = index;
        while (i < segment.length() && Character.isWhitespace(segment.charAt(i))) {
            i++;
        }

        if (i >= segment.length()) {
            return false;
        }

        if (segment.charAt(i) == '~') {
            i++;
            while (i < segment.length() && Character.isWhitespace(segment.charAt(i))) {
                i++;
            }
        }

        return i < segment.length() && segment.charAt(i) == '(';
    }

    private List<String> splitTopLevel(String input, char delimiter) {
        List<String> out = new ArrayList<>();
        if (input == null || input.isBlank()) {
            return out;
        }

        StringBuilder current = new StringBuilder();
        int parenDepth = 0;
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inQuote) {
                current.append(c);
                if (c == quoteChar) {
                    inQuote = false;
                }
                continue;
            }

            if (c == '\'' || c == '"') {
                inQuote = true;
                quoteChar = c;
                current.append(c);
                continue;
            }

            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth = Math.max(0, parenDepth - 1);
            }

            if (c == delimiter && parenDepth == 0) {
                out.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        out.add(current.toString());
        return out;
    }

    private OffPathConstraint parseOffPathConstraint(String offPath) {
        String clause = offPath.trim();
        boolean negated = false;
        if (clause.startsWith("~")) {
            negated = true;
            clause = clause.substring(1).trim();
        }

        if (clause.startsWith("(") && clause.endsWith(")")) {
            clause = clause.substring(1, clause.length() - 1).trim();
        }

        OffPathDirection direction;
        if (clause.startsWith("->")) {
            direction = OffPathDirection.TO_VALUE;
            clause = clause.substring(2).trim();
        } else if (clause.startsWith("<-")) {
            direction = OffPathDirection.TO_ORIGIN;
            clause = clause.substring(2).trim();
        } else {
            throw new IllegalArgumentException("Invalid off-path constraint: " + offPath);
        }

        List<String> tokens = QueryParser.tokenizeQuery(clause);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Invalid off-path constraint: " + offPath);
        }

        String attribute = tokens.get(0);
        String value = null;
        if (tokens.size() > 1) {
            int valueStart = 1;
            if ("=".equals(tokens.get(1))) {
                valueStart = 2;
            }
            if (valueStart < tokens.size()) {
                value = String.join(" ", tokens.subList(valueStart, tokens.size()));
            }
        }

        return new OffPathConstraint(negated, direction, attribute, value);
    }

    private boolean matchesOffPathConstraints(PathAtom atom,
                                              GraphConstraint current,
                                              HashMap<Integer, GraphConstraint> graph,
                                              boolean insideOut) {
        for (OffPathConstraint constraint : atom.offPathConstraints) {
            String nodeRef;
            if (constraint.direction == OffPathDirection.TO_VALUE) {
                nodeRef = String.valueOf(insideOut ? current.getFsNode() : current.getFsValue());
            } else {
                nodeRef = String.valueOf(insideOut ? current.getFsValue() : current.getFsNode());
            }
            boolean matches = nodeMatchesConstraint(nodeRef, constraint, graph);
            if (constraint.negated ? matches : !matches) {
                return false;
            }
        }
        return true;
    }

    private boolean nodeMatchesConstraint(String nodeRef, OffPathConstraint constraint, HashMap<Integer, GraphConstraint> graph) {
        for (GraphConstraint gc : graph.values()) {
            if (!nodeRef.equals(gc.getFsNode())) {
                continue;
            }
            if (!constraint.attribute.equals(gc.getRelationLabel())) {
                continue;
            }

            if (constraint.value == null || constraint.value.isBlank()) {
                return true;
            }

            if (normalizeComparableValue(String.valueOf(gc.getFsValue())).equals(normalizeComparableValue(constraint.value))) {
                return true;
            }
        }

        return false;
    }

    private String normalizeComparableValue(String value) {
        if (value == null) {
            return "";
        }

        String normalized = HelperMethods.stripValeue2(value.trim());
        if ((normalized.startsWith("'") && normalized.endsWith("'")) ||
                (normalized.startsWith("\"") && normalized.endsWith("\""))) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

    public HashMap<Integer, GraphConstraint> searchUncertainty(HashMap<Integer, GraphConstraint> in) {
        return searchUncertainty(parsePathQuery(middle.getQuery()), in);
    }

    public HashMap<Integer, GraphConstraint> searchUncertainty(String searchQuery, HashMap<Integer, GraphConstraint> in) {
        return searchUncertainty(parsePathQuery(searchQuery), in);
    }

    private HashMap<Integer, GraphConstraint> searchUncertainty(List<PathAtom> search, HashMap<Integer, GraphConstraint> in) {

        HashMap<Integer, GraphConstraint> result = new HashMap<>();

        List<Integer> boundVariables = new ArrayList<>();

        for (PathAtom atom : search) {

            if (result.keySet().isEmpty()) {
                result = in;
            }

            HashMap<Integer, GraphConstraint> currentResult = new HashMap<>();

            for (Integer key : result.keySet()) {

                if (atom.quantifier != Quantifier.EXACT) {
                    if (atom.quantifier == Quantifier.ZERO_OR_MORE) {
                        if (matchesOffPathConstraints(atom, result.get(key), right.getFsIndices(), false)) {
                            currentResult.put(key, result.get(key));
                        }
                    }

                    if (atom.labels.contains(result.get(key).getRelationLabel())) {
                        if (!matchesOffPathConstraints(atom, result.get(key), right.getFsIndices(), false)) {
                            continue;
                        }

                        boundVariables.add(key);

                        boolean foundString = true;
                        int repeatCount = 0;

                        List<Integer> keys = new ArrayList<>();
                        List<Integer> matchingKeys = new ArrayList<>();

                        for (Integer key2 : right.getFsIndices().keySet()) {
                            if (result.get(key).getFsValue().equals(right.getFsIndices().get(key2).getFsNode())) {
                                keys.add(key2);
                            }
                        }

                        while (foundString && repeatCount < MAX_UNCERTAINTY_REPEAT) {

                            keys.removeIf(next -> !atom.labels.contains(right.getFsIndices().get(next).getRelationLabel()));

                            if (!keys.isEmpty()) {
                                matchingKeys.addAll(keys);
                                List<Integer> newKeys = new ArrayList<>();

                                //if (left.getFsIndices().get(key).getFsValue().equals(right.getFsIndices().get(key2).getFsNode()))
                                for (Integer key3 : keys) {
                                    for (Integer key4 : right.getFsIndices().keySet()) {
                                        if (right.getFsIndices().get(key3).getFsValue().equals(right.getFsIndices().get(key4).getFsNode())) {
                                            newKeys.add(key4);
                                        }
                                    }
                                }
                                keys = newKeys;
                            } else {
                                foundString = false;
                            }

                            repeatCount++;
                        }

                        HashMap<Integer, GraphConstraint> newResult = new HashMap<>(right.getFsIndices());
                        for (Integer r : new ArrayList<>(newResult.keySet())) {
                            if (!matchingKeys.contains(r)) {
                                newResult.remove(r);
                            }
                        }

                        currentResult.putAll(newResult);
                        // result = newResult;


                    }
                } else if (atom.labels.contains(result.get(key).getRelationLabel())) {
                    if (!matchesOffPathConstraints(atom, result.get(key), right.getFsIndices(), false)) {
                        continue;
                    }

                    if (search.get(0) == atom) {
                        boundVariables.add(key);
                    }

                    HashMap<Integer, GraphConstraint> newResult = new HashMap<>();

                    for (Integer key2 : right.getFsIndices().keySet()) {
                        if (result.get(key).getFsValue().equals(right.getFsIndices().get(key2).getFsNode())) {
                            newResult.put(key2, right.getFsIndices().get(key2));
                        } else {


                            if (!HelperMethods.isInteger(result.get(key).getFsValue())) {
                                newResult.put(key, result.get(key));
                            }
                        }
                    }

                    currentResult.putAll(newResult);
                }
            }

            //  getParser().getVarAssignment().get(getNodeVar()).removeIf(i -> !boundVariables.contains(i));

            for (Integer key : currentResult.keySet())
            {
                choices.addAll(currentResult.get(key).getReading());
            }

            if (currentResult.isEmpty()) {
                return currentResult;
            } else {
                result = currentResult;
            }

        }

        return result;
    }

    public HashMap<Integer, GraphConstraint> searchInsideOutUncertainty(HashMap<Integer, GraphConstraint> in) {
        return searchInsideOutUncertainty(parsePathQuery(middle.getQuery()), in);
    }

    public HashMap<Integer, GraphConstraint> searchInsideOutUncertainty(String searchQuery, HashMap<Integer, GraphConstraint> in) {
        return searchInsideOutUncertainty(parsePathQuery(searchQuery), in);
    }

    private boolean allowsZeroLength(String searchQuery) {
        List<PathAtom> atoms = parsePathQuery(searchQuery);
        return !atoms.isEmpty() && atoms.stream().allMatch(atom -> atom.quantifier == Quantifier.ZERO_OR_MORE);
    }

    private HashMap<Integer, GraphConstraint> searchInsideOutUncertainty(List<PathAtom> search, HashMap<Integer, GraphConstraint> in) {


        HashMap<Integer, GraphConstraint> result = new HashMap<>();

        HashMap<Integer, GraphConstraint> inverseFsIndices = calculateInsideOutIndices(in, middle.getFsIndices());


        for (int i = 0; i < search.size(); i++) {

            PathAtom atom = search.get(i);

            if (result.keySet().isEmpty()) {
                result = inverseFsIndices;
            }

            HashMap<Integer, GraphConstraint> currentResult = new HashMap<>();

            for (Integer key : result.keySet()) {

                if (atom.quantifier != Quantifier.EXACT) {
                    if (atom.quantifier == Quantifier.ZERO_OR_MORE) {
                        if (matchesOffPathConstraints(atom, result.get(key), right.getFsIndices(), true)) {
                            currentResult.put(key, result.get(key));
                        }
                    }

                    if (atom.labels.contains(result.get(key).getRelationLabel())
                        || atom.labels.contains("%") ||
                        (atom.labels.contains("GF") && gf.contains(result.get(key).getRelationLabel())))
                        {

                        if (!matchesOffPathConstraints(atom, result.get(key), right.getFsIndices(), true)) {
                            continue;
                        }


                        //TODO
                        //  boundVariables.add(key);

                        boolean foundString = true;
                        int repeatCount = 0;

                        List<Integer> keys = new ArrayList<>();
                        List<Integer> matchingKeys = new ArrayList<>();

                        Set<String> unspecRelation = new HashSet<>();

                        for (Integer key2 : right.getFsIndices().keySet()) {
                            if (result.get(key).getFsNode().equals(right.getFsIndices().get(key2).getFsNode())) {
                                keys.add(key2);
                                if (atom.labels.contains("%")) {
                                    unspecRelation.add(right.getFsIndices().get(key).getRelationLabel());
                                }
                            }
                        }
                        while (foundString && repeatCount < MAX_UNCERTAINTY_REPEAT) {


                            if (atom.labels.contains("%"))
                            {
                                keys.removeIf(next -> !unspecRelation.contains(right.getFsIndices().get(next).getRelationLabel()));
                            } else if (atom.labels.contains("GF"))
                            {
                                keys.removeIf(next -> !gf.contains(right.getFsIndices().get(next).getRelationLabel()));
                            } else {
                                keys.removeIf(next -> !atom.labels.contains(right.getFsIndices().get(next).getRelationLabel()));
                            }


                     //       keys.removeIf(next -> !right.getFsIndices().get(next).getRelationLabel().equals(starMatcher.group(1)));


                            if (!keys.isEmpty()) {
                                matchingKeys.addAll(keys);
                                List<Integer> newKeys = new ArrayList<>();

                                //if (left.getFsIndices().get(key).getFsValue().equals(right.getFsIndices().get(key2).getFsNode()))
                                for (Integer key3 : keys) {
                                    for (Integer key4 : right.getFsIndices().keySet()) {
                                        if (right.getFsIndices().get(key3).getFsNode().equals(right.getFsIndices().get(key4).getFsValue())) {
                                            newKeys.add(key4);
                                        }
                                    }
                                }
                                keys = newKeys;
                            } else {
                                foundString = false;
                            }

                            repeatCount++;
                        }

                        HashMap<Integer, GraphConstraint> newResult = new HashMap<>(right.getFsIndices());
                        for (Integer r : new ArrayList<>(newResult.keySet())) {
                            if (!matchingKeys.contains(r)) {
                                newResult.remove(r);
                            }
                        }

                        currentResult.putAll(newResult);
                        // result = newResult;


                    }
                } else if (atom.labels.contains(result.get(key).getRelationLabel())) {
                    if (!matchesOffPathConstraints(atom, result.get(key), right.getFsIndices(), true)) {
                        continue;
                    }

                    HashMap<Integer, GraphConstraint> newResult = new HashMap<>();

                    if (i != search.size() - 1) {
                        for (Integer key2 : right.getFsIndices().keySet()) {
                            if (result.get(key).getFsNode().equals(right.getFsIndices().get(key2).getFsValue())) {
                                newResult.put(key2, right.getFsIndices().get(key2));
                            }
                        }
                    } else {
                        for (Integer key2 : right.getFsIndices().keySet()) {
                            if (result.get(key).getFsNode().equals(right.getFsIndices().get(key2).getFsNode())) {
                                newResult.put(key2, right.getFsIndices().get(key2));
                            }
                        }
                    }
                    currentResult.putAll(newResult);
                } else if (atom.labels.contains("%")) {
                    HashMap<Integer, GraphConstraint> newResult = new HashMap<>();

                    for (Integer key2 : right.getFsIndices().keySet()) {
                        if (result.get(key).getFsNode().equals(right.getFsIndices().get(key2).getFsNode())) {

                            newResult.put(key2, right.getFsIndices().get(key2));

                        }
                        currentResult.putAll(newResult);
                    }

                } else if (atom.labels.contains("GF")) {
                    HashMap<Integer, GraphConstraint> newResult = new HashMap<>();

                    for (Integer key2 : right.getFsIndices().keySet()) {
                        if (gf.contains(result.get(key).getRelationLabel()) &&
                                result.get(key).getFsNode().equals(right.getFsIndices().get(key2).getFsNode())) {

                            newResult.put(key2, right.getFsIndices().get(key2));

                        }
                        currentResult.putAll(newResult);
                    }
                }
            }

            for (Integer key : currentResult.keySet())
            {
                choices.addAll(currentResult.get(key).getReading());
            }

            if (currentResult.isEmpty()) {
                        return currentResult;
                    } else {
                        result = currentResult;
                    }
                }



            return result;



    }
}
