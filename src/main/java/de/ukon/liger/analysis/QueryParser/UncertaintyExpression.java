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

            if (!middle.insideOut) {
                uncertainty = searchUncertainty(boundIndices);
            } else {
                uncertainty = searchInsideOutUncertainty(boundIndices);
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


        setSolution(out);
        //  getParser().fsNodeBindings = out;
    }


    public HashMap<Integer, GraphConstraint> searchUncertainty(HashMap<Integer, GraphConstraint> in) {

        Deque<String> search = new LinkedList<String>(Arrays.asList(middle.getQuery().split(">")));
        Pattern starPattern = Pattern.compile("(.*)\\*");

        HashMap<Integer, GraphConstraint> result = new HashMap<>();

        List<Integer> boundVariables = new ArrayList<>();

        for (String element : search) {


            Matcher starMatcher = starPattern.matcher(element);

            if (result.keySet().isEmpty()) {
                result = in;
            }

            HashMap<Integer, GraphConstraint> currentResult = new HashMap<>();

            for (Integer key : result.keySet()) {

                if (starMatcher.matches()) {
                    if (result.get(key).getRelationLabel().equals(starMatcher.group(1))) {

                        boundVariables.add(key);

                        boolean foundString = true;

                        List<Integer> keys = new ArrayList<>();
                        List<Integer> allKeys = new ArrayList<>();

                        for (Integer key2 : right.getFsIndices().keySet()) {
                            if (result.get(key).getFsValue().equals(right.getFsIndices().get(key2).getFsNode())) {
                                keys.add(key2);
                            }
                        }

                        allKeys.addAll(keys);

                        //Keys is the first result of LABEL* it contains references to relevant graph constraints for further iterations

                        // List<Integer> finalKeys = keys;
                        //  result.keySet().removeIf(resultKey -> !finalKeys.contains(resultKey));
                        //keys.removeIf(resultKey -> !result.keySet().contains(resultKey)


                        while (foundString) {

                            keys.removeIf(next -> !right.getFsIndices().get(next).getRelationLabel().equals(starMatcher.group(1)));

                            if (!keys.isEmpty()) {
                                List<Integer> newKeys = new ArrayList<>();

                                //if (left.getFsIndices().get(key).getFsValue().equals(right.getFsIndices().get(key2).getFsNode()))
                                for (Integer key3 : keys) {
                                    for (Integer key4 : right.getFsIndices().keySet()) {
                                        if (right.getFsIndices().get(key3).getFsValue().equals(right.getFsIndices().get(key4).getFsNode())) {
                                            newKeys.add(key4);
                                        }
                                    }
                                }
                                allKeys.addAll(newKeys);
                                keys = newKeys;
                            } else {
                                foundString = false;
                            }
                        }

                        HashMap<Integer, GraphConstraint> newResult = new HashMap<>(right.getFsIndices());
                        newResult.keySet().removeIf(r -> !allKeys.contains(r));

                        currentResult.putAll(newResult);
                        // result = newResult;


                    }
                } else if (result.get(key).getRelationLabel().equals(element)) {

                    if (search.getFirst().equals(element)) {
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

        List<String> search = new LinkedList<String>(Arrays.asList(middle.getQuery().split(">")));
        Pattern starPattern = Pattern.compile("(.*)\\*");


        HashMap<Integer, GraphConstraint> result = new HashMap<>();

        HashMap<Integer, GraphConstraint> inverseFsIndices = calculateInsideOutIndices(in, middle.getFsIndices());


        for (int i = 0; i < search.size(); i++) {

            String element = search.get(i);
            Matcher starMatcher = starPattern.matcher(element);

            if (result.keySet().isEmpty()) {
                result = inverseFsIndices;
            }

            HashMap<Integer, GraphConstraint> currentResult = new HashMap<>();

            for (Integer key : result.keySet()) {

                if (starMatcher.matches()) {
                    if (result.get(key).getRelationLabel().equals(starMatcher.group(1))
                        || starMatcher.group(1).equals("%") ||
                        (starMatcher.group(1).equals("GF") && gf.contains(result.get(key).getRelationLabel())))
                        {


                        //TODO
                        //  boundVariables.add(key);

                        boolean foundString = true;

                        List<Integer> keys = new ArrayList<>();
                        List<Integer> allKeys = new ArrayList<>();

                        Set<String> unspecRelation = new HashSet<>();

                        for (Integer key2 : right.getFsIndices().keySet()) {
                            if (result.get(key).getFsNode().equals(right.getFsIndices().get(key2).getFsNode())) {
                                keys.add(key2);
                                if (starMatcher.group(1).equals("%")) {
                                    unspecRelation.add(right.getFsIndices().get(key).getRelationLabel());
                                }
                            }
                        }

                        allKeys.addAll(keys);

                        //Keys is the first result of LABEL* it contains references to relevant graph constraints for further iterations

                        // List<Integer> finalKeys = keys;
                        //  result.keySet().removeIf(resultKey -> !finalKeys.contains(resultKey));
                        //keys.removeIf(resultKey -> !result.keySet().contains(resultKey)


                        while (foundString) {


                            if (starMatcher.group(1).equals("%"))
                            {
                                keys.removeIf(next -> !unspecRelation.contains(right.getFsIndices().get(next).getRelationLabel()));
                            } else if (starMatcher.group(1).equals("GF"))
                            {
                                keys.removeIf(next -> !gf.contains(right.getFsIndices().get(next).getRelationLabel()));
                            } else {
                                keys.removeIf(next -> !right.getFsIndices().get(next).getRelationLabel().equals(starMatcher.group(1)));
                            }


                     //       keys.removeIf(next -> !right.getFsIndices().get(next).getRelationLabel().equals(starMatcher.group(1)));


                            if (!keys.isEmpty()) {
                                List<Integer> newKeys = new ArrayList<>();

                                //if (left.getFsIndices().get(key).getFsValue().equals(right.getFsIndices().get(key2).getFsNode()))
                                for (Integer key3 : keys) {
                                    for (Integer key4 : right.getFsIndices().keySet()) {
                                        if (right.getFsIndices().get(key3).getFsNode().equals(right.getFsIndices().get(key4).getFsValue())) {
                                            newKeys.add(key4);
                                        }
                                    }
                                }
                                allKeys.addAll(newKeys);
                                keys = newKeys;
                            } else {
                                foundString = false;
                            }
                        }

                        HashMap<Integer, GraphConstraint> newResult = new HashMap<>(right.getFsIndices());
                        newResult.keySet().removeIf(r -> !allKeys.contains(r));

                        currentResult.putAll(newResult);
                        // result = newResult;


                    }
                } else if (result.get(key).getRelationLabel().equals(element)) {
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
                } else if (element.equals("%")) {
                    HashMap<Integer, GraphConstraint> newResult = new HashMap<>();

                    for (Integer key2 : right.getFsIndices().keySet()) {
                        if (result.get(key).getFsNode().equals(right.getFsIndices().get(key2).getFsNode())) {

                            newResult.put(key2, right.getFsIndices().get(key2));

                        }
                        currentResult.putAll(newResult);
                    }

                } else if (element.equals("GF")) {
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
