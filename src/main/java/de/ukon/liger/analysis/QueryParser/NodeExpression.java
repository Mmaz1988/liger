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

import de.ukon.liger.syntax.GraphConstraint;

import java.util.*;

public class NodeExpression extends QueryExpression {

    private QueryExpression left;
    private Node right;

    public NodeExpression(QueryExpression left, Node right) {
        setNodeVar(right.getNodeVar());
        setFsIndices(new HashMap<>());
        this.left = left;
        this.right = right;
        setParser(left.getParser());

        calculateSolutions();
    }


    @Override
    public void calculateSolutions() {

        if (left instanceof ConjointExpression) {

            Boolean alreadyBound = false;
            for (Set<SolutionKey> key : left.getSolution().keySet()) {
                if (left.getSolution().get(key).containsKey(right.getNodeVar())) {



                //    setSolution(left.getSolution());
                    //TODO (maybe) set solution of right
                    alreadyBound = true;
                    break;
                }
            }

            if (!alreadyBound)
            {
                HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();


                    HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out2 =
                            right.getSolution();

                    for (Set<SolutionKey> key : left.getSolution().keySet()){

                    for (Set<SolutionKey> key2 : out2.keySet())
                    {
                        Set<SolutionKey> newKey = new HashSet<>();
                        newKey.addAll(key);
                        newKey.addAll(key2);

                        out.put(newKey,new HashMap<>());
                        out.get(newKey).putAll(left.getSolution().get(key));
                        out.get(newKey).putAll(out2.get(key2));
                    }
                }

                    setFsIndices(right.getFsIndices());
                    setSolution(out);

            }
            else
            {
                setSolution(left.getSolution());
                /*
                for (Set<SolutionKey> key : left.getSolution().keySet())
                {
                    for (String key2 : left.getSolution().get(key).get(right.getNodeVar()).keySet())
                    {
                        getFsIndices().putAll(left.getSolution().get(key).get(right.getNodeVar()).get(key2));
                    }
                }

                 */
            }



        } else if (left instanceof UncertaintyExpression)
        {
            setFsIndices(left.getFsIndices());
            setSolution(left.getSolution());
        }
        else {

            HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> leftSolution = left.getSolution();
            HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> rightSolution = right.getSolution();

            HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> solution = new HashMap<>();



            Iterator<Set<SolutionKey>> it = rightSolution.keySet().iterator();
            while (it.hasNext()) {
                Set<SolutionKey> key = it.next();

                String nodeVar = right.getNodeVar();
                String nodeRef = rightSolution.get(key).get(nodeVar).keySet().stream().findAny().get();
                //          HashMap<Integer, GraphConstraint> boundIndices = rightSolution.get(key).get(nodeVar).get(nodeRef);


                if (left.getNodeVar() != null) {

                    for (Set<SolutionKey> key2 : left.getSolution().keySet()) {
                        String nodeRef2 = leftSolution.get(key2).get(left.getNodeVar()).keySet().stream().findAny().get();
                        for (Integer key3 : leftSolution.get(key2).get(left.getNodeVar()).get(nodeRef2).keySet()) {
                            if (leftSolution.get(key2).get(left.getNodeVar()).get(nodeRef2).get(key3).getFsValue().equals(nodeRef) &&
                            leftSolution.get(key2).get(left.getNodeVar()).get(nodeRef2).get(key3).getRelationLabel().equals(left.getQuery())) {
                                if (checkSolutionCompatibility(key, key2)) {
                                    Set<SolutionKey> newKey = new HashSet<>();
                                    newKey.addAll(key);
                                    newKey.addAll(key2);

                                    HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new HashMap<>();

                                    binding.putAll(rightSolution.get(key));
                                    binding.putAll(leftSolution.get(key2));

                                    solution.put(newKey, binding);

                                }

                            }
                        }
                    }
                } else {
                    for (Integer key2 : left.getFsIndices().keySet()) {
                        if (left.getFsIndices().get(key2).getFsValue().equals(nodeRef)) {
                            HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new HashMap<>();

                            binding.putAll(rightSolution.get(key));
                            solution.put(key, binding);

                        }
                    }
                }
            }

            setSolution(solution);

                //This collects the constraints that are currently bound by the righthand side variable.
                //There are some issues with respect to be the right hand side being empty. The second for-loop
                //ensures that empty structures are also included.
/*
                for (Integer key3 : boundIndices.keySet()) {
                    if (left.getFsIndices().containsKey(key3)) {

                        if (HelperMethods.isInteger(left.getFsIndices().get(key3).getFsValue())) {
                            usedKeys.add((String) left.getFsIndices().get(key3).getFsValue());
                        }
                    }
                }

                for (Integer key3 : boundIndices.keySet())
                {
                    for (Integer key4 : left.getFsIndices().keySet())
                    {
                        if (boundIndices.get(key3).getFsNode().equals(left.getFsIndices().get(key4).getFsValue()))
                        {
                            usedKeys.add((String) left.getFsIndices().get(key4).getFsValue());
                        }
                    }
                }
                */

                        /*

        //Solution only for current Node variable
            HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out2 =
                    mapUsedKeys(usedKeys, right.getFsIndices(), right.getNodeVar());


            //Collect current values to fsIndices

            for (Set<SolutionKey> key : out2.keySet())
            {
                for (String key2 : out2.get(key).keySet())
                    for (String key3 : out2.get(key).get(key2).keySet())
                        getFsIndices().putAll(out2.get(key).get(key2).get(key3));
            }

            right.setSolution(out2);

            //  System.out.println("Test");


            //Concatenation with previous variable bindings

            if (left.getNodeVar() != null) {
                HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out3 = new HashMap<>();


                for (Set<SolutionKey> key : leftSolution.keySet()) {


                    String nodeVar = left.getNodeVar();
                    String nodeRef = leftSolution.get(key).get(nodeVar).keySet().stream().findAny().get();
                    HashMap<Integer, GraphConstraint> boundIndices = leftSolution.get(key).get(nodeVar).get(nodeRef);

                    for (Set<SolutionKey> key2 : out2.keySet()) {

                        if (checkSolutionCompatibility(key,key2))
                        {


                        String nodeVar2 = getNodeVar();
                        String nodeRef2 = out2.get(key2).get(nodeVar2).keySet().stream().findAny().get();
                        //      HashMap<Integer,GraphConstraint> boundIndices2 = out2.get(key2).get(nodeVar2).get(nodeRef2);

                        HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new HashMap<>();

                        for (String key4 : leftSolution.get(key).keySet()) {
                            if (!key4.equals(nodeVar)) {
                                binding.put(key4, leftSolution.get(key).get(key4));
                            }

                        }

                        for (Integer key3 : boundIndices.keySet()) {
                            if (boundIndices.get(key3).getFsValue().equals(nodeRef2)) {
                                binding.put(nodeVar, leftSolution.get(key).get(nodeVar));
                                binding.put(nodeVar2, out2.get(key2).get(nodeVar2));

                                Set<SolutionKey> newKey = new HashSet<>();
                                newKey.addAll(key);
                                newKey.addAll(key2);

                                out3.put(newKey, binding);
                            }
                        }
                    }
                }
                }


                setSolution(out3);

            } else
            {
                setSolution(out2);
            }
      //      getParser().fsNodeBindings = out3;

         */


        }
    }


    public QueryExpression getLeft() {
        return left;
    }

    public void setLeft(QueryExpression left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public Boolean checkSolutionCompatibility(Set<SolutionKey> sol1,
                                              Set<SolutionKey> sol2)
    {

                Iterator<SolutionKey> sol1Itr = sol1.iterator();
                HashMap<String,Set<String>> sol1map = new HashMap();
                while (sol1Itr.hasNext())
                {
                    SolutionKey sol1var = sol1Itr.next();
                    if (!sol1map.containsKey(sol1var.variable))
                    {
                        sol1map.put(sol1var.variable,new HashSet<>());
                    }
                    sol1map.get(sol1var.variable).add(sol1var.reference);
                }

                Iterator<SolutionKey> sol2Itr = sol2.iterator();
                HashMap<String,Set<String>> sol2map = new HashMap();
                while (sol2Itr.hasNext())
                {
                    SolutionKey sol2var = sol2Itr.next();
                    if (!sol2map.containsKey(sol2var.variable))
                    {
                        sol2map.put(sol2var.variable,new HashSet<>());
                    }
                    sol2map.get(sol2var.variable).add(sol2var.reference);
                }

                List<String> commonKeys;
                List<String> difference;
                if (sol1map.keySet().size() > sol2map.keySet().size())
                {
                    commonKeys = new ArrayList<String>(sol1map.keySet());
                    difference = new ArrayList<String>(sol2map.keySet());
                } else{
                    commonKeys = new ArrayList<String>(sol2map.keySet());
                    difference = new ArrayList<String>(sol1map.keySet());
                }

                commonKeys.retainAll(difference);

                for (String commonKey : commonKeys){
                    if (!sol1map.get(commonKey).equals(sol2map.get(commonKey)))
                    {
                        return false;
                    }
        }
        return true;
    }
}
