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

    public String uncertaintyExpression;

    private QueryExpression left;
    private Uncertainty middle;
    private QueryExpression right;
    private List<String> gf = Arrays.asList("OBL", "OBJ", "SUBJ", "COMP", "XCOMP", "OBJ-TH", "XCOMP-PRED");
    private Set<ChoiceVar> choices = new HashSet<>();

    private static class PathAtom {
        private final Set<String> labels;
        private final boolean star;

        private PathAtom(Set<String> labels, boolean star) {
            this.labels = labels;
            this.star = star;
        }
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
                if (!middle.insideOut) {
                    uncertainty = searchUncertainty(searchQuery, boundIndices);
                } else {
                    uncertainty = searchInsideOutUncertainty(searchQuery, boundIndices);
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
        TemplateRegistry registry = middle.getParser() != null ? middle.getParser().getTemplateRegistry() : null;
        String query = middle.getQuery() == null ? "" : middle.getQuery().trim();
        return Collections.singletonList(query);
    }

    private List<PathAtom> parsePathQuery(String query) {
        List<PathAtom> out = new ArrayList<>();
        TemplateRegistry registry = middle.getParser() != null ? middle.getParser().getTemplateRegistry() : null;

        for (String rawSegment : query.split(">")) {
            String segment = rawSegment.trim();
            boolean star = segment.endsWith("*");
            if (star) {
                segment = segment.substring(0, segment.length() - 1).trim();
            }

            Set<String> labels = new LinkedHashSet<>();
            if (segment.startsWith("@")) {
                String templateName = segment.substring(1).trim();
                if (registry == null) {
                    throw new IllegalArgumentException("Template registry required for path template: " + segment);
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
                labels.add(segment);
            }

            out.add(new PathAtom(labels, star));
        }

        return out;
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

                if (atom.star) {
                    if (atom.labels.contains(result.get(key).getRelationLabel())) {

                        boundVariables.add(key);

                        boolean foundString = true;

                        List<Integer> keys = new ArrayList<>();
                        List<Integer> matchingKeys = new ArrayList<>();

                        for (Integer key2 : right.getFsIndices().keySet()) {
                            if (result.get(key).getFsValue().equals(right.getFsIndices().get(key2).getFsNode())) {
                                keys.add(key2);
                            }
                        }

                        while (foundString) {

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

                if (atom.star) {
                    if (atom.labels.contains(result.get(key).getRelationLabel())
                        || atom.labels.contains("%") ||
                        (atom.labels.contains("GF") && gf.contains(result.get(key).getRelationLabel())))
                        {


                        //TODO
                        //  boundVariables.add(key);

                        boolean foundString = true;

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
                        while (foundString) {


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
