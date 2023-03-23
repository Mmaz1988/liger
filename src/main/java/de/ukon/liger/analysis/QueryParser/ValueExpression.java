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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ValueExpression extends QueryExpression {

    private QueryExpression left;
    private Value right;


    public ValueExpression(QueryExpression left, Value right)
    {

        this.left = left;
        this.right = right;
        setParser(right.getParser());

   calculateSolutions();


    }


    @Override
    public void calculateSolutions()
    {
        HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();

        out.putAll(left.getSolution());

        HashMap<Integer,GraphConstraint> fsIndices = new HashMap<>();

        HashMap<Set<SolutionKey>,HashMap<String,String>> newValueBindings = getParser().fsValueBindings;

            Iterator<Set<SolutionKey>> it = out.keySet().iterator();



            while (it.hasNext()) {
                Set<SolutionKey> key = it.next();

                String nodeVar = left.getNodeVar();
                String nodeRef = out.get(key).get(nodeVar).keySet().stream().findAny().get();
                HashMap<Integer, GraphConstraint> boundIndices =  out.get(key).get(nodeVar).get(nodeRef);

                HashMap<Integer,GraphConstraint> matchingIndices = new HashMap<>();

                if (right.var)
                {
                        if (!newValueBindings.containsKey(key))
                        {

                            newValueBindings.put(key,new HashMap<>());
                            //
                            Iterator<Set<SolutionKey>> it2 = getParser().fsValueBindings.keySet().iterator();

                            while (it2.hasNext()) {
                                Set<SolutionKey> key2 = it2.next();
                                if (!(key.equals(key2)) && key.containsAll(key2)) {
                                    newValueBindings.get(key).putAll(getParser().fsValueBindings.get(key2));
                                }
                            }
                        }
                        }

                HashSet<Integer> nonBoundIndices = new HashSet<>();

                for (Integer key2 : boundIndices.keySet()) {
                if (boundIndices.get(key2).getRelationLabel().equals(left.getQuery())) {

                    String varMatch;
                    if (right.var) {
                        if (!newValueBindings.get(key).containsKey(right.getQuery())) {
                            newValueBindings.get(key).put(right.getQuery(),
                                    (String) boundIndices.get(key2).getFsValue());

                            varMatch = (String) boundIndices.get(key2).getFsValue();

                        } else {
                            varMatch = newValueBindings.get(key).get(right.getQuery());
                        }
                    }
                        else
                        {
                            varMatch = right.getQuery();
                        }

                        if (boundIndices.get(key2).getFsValue().equals(varMatch)) {
                            matchingIndices.put(key2, boundIndices.get(key2));
                        } else if (!right.var)
                        {
                            //Delete non-matching values
                            nonBoundIndices.add(key2);
                            //boundIndices.remove(key2);
                        }


                }
                }

                boundIndices.keySet().removeAll(nonBoundIndices);

                if (matchingIndices.keySet().isEmpty())
                {
                    it.remove();
                }

                    fsIndices.putAll(matchingIndices);

            }



            if (!fsIndices.keySet().isEmpty()) {
                setSolution(out);
                setFsIndices(fsIndices);
                setConjoinedSolutions(left.getConjoinedSolutions());
      //          getParser().fsNodeBindings = out;
            }
        }



}


