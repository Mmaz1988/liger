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
import de.ukon.liger.utilities.HelperMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class ValueExpression extends QueryExpression {

    private static final Logger log = LoggerFactory.getLogger(ValueExpression.class);
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
        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();
        out.putAll(left.getSolution());

        if (left.getNodeVar() == null) {
            HashMap<Integer, GraphConstraint> matchingIndices = new HashMap<>();
            String expected = normalizeFilterValue(right.getQuery());

            for (Integer key : left.getFsIndices().keySet()) {
                GraphConstraint constraint = left.getFsIndices().get(key);
                if (constraint == null || expected == null || expected.isBlank()) {
                    continue;
                }

                String relationLabel = normalizeGraphValue(constraint.getRelationLabel());
                String value = normalizeGraphValue(String.valueOf(constraint.getFsValue()));

                if (relationLabel.equals(normalizeEdgeFilter(left.getQuery())) || relationLabel.equals(left.getQuery())) {
                    if (expected.equals(value)) {
                        matchingIndices.put(key, constraint);
                    }
                }
            }

            if (!matchingIndices.isEmpty()) {
                HashMap<String, HashMap<Integer, GraphConstraint>> reference = new HashMap<>();
                reference.put("__match__", matchingIndices);

                HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new LinkedHashMap<>();
                binding.put("__match__", reference);

                HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> globalOut = new HashMap<>();
                globalOut.put(new Solution(Collections.singleton(new SolutionKey("__match__", "__match__"))), binding);
                setSolution(globalOut);
                setFsIndices(matchingIndices);
                setConjoinedSolutions(left.getConjoinedSolutions());
            }

            return;
        }

        HashMap<Integer,GraphConstraint> fsIndices = new HashMap<>();

        HashMap<Solution,HashMap<String,String>> newValueBindings = getParser().fsValueBindings;

            Iterator<Solution> it = out.keySet().iterator();



            while (it.hasNext()) {
                Solution key = it.next();
                if (!key.isTruthValue()) {
                    continue;
                }

            String nodeVar = left.getNodeVar();
            String nodeRef = out.get(key).get(nodeVar).keySet().stream().findAny().get();
            HashMap<Integer, GraphConstraint> boundIndices =  out.get(key).get(nodeVar).get(nodeRef);

            HashMap<Integer,GraphConstraint> matchingIndices = new HashMap<>();
            HashSet<Integer> nonBoundIndices = new HashSet<>();

            if (right.idRef)
            {
                String expectedId = ValueResolver.resolve(this, key, right);
                if (expectedId == null) {
                    it.remove();
                    continue;
                }
                for (Integer key2 : boundIndices.keySet()) {
                    if (boundIndices.get(key2).getRelationLabel().equals(left.getQuery())) {
                        String fsValue = (String) boundIndices.get(key2).getFsValue();
                        if ((fsValue.startsWith("'") && fsValue.endsWith("'")) ||
                                (fsValue.startsWith("\"") && fsValue.endsWith("\""))) {
                            fsValue = fsValue.substring(1, fsValue.length() - 1);
                        }

                        if (fsValue.equals(expectedId)) {
                            matchingIndices.put(key2, boundIndices.get(key2));
                        } else {
                            nonBoundIndices.add(key2);
                        }
                    }
                }
            }
            else if (right.var)
                {
                        if (!newValueBindings.containsKey(key))
                        {

                            newValueBindings.put(key,new HashMap<>());
                            //
                            Iterator<Solution> it2 = getParser().fsValueBindings.keySet().iterator();

                            while (it2.hasNext()) {
                                Solution key2 = it2.next();
                                if (!(key.equals(key2)) && key.getSolutionKeys().containsAll(key2.getSolutionKeys())) {
                                    newValueBindings.get(key).putAll(getParser().fsValueBindings.get(key2));
                                }
                            }
                    }

                    for (Integer key2 : boundIndices.keySet()) {
                        if (boundIndices.get(key2).getRelationLabel().equals(left.getQuery())) {
                            String varMatch;
                            if (!newValueBindings.get(key).containsKey(right.getQuery())) {
                                newValueBindings.get(key).put(right.getQuery(),
                                        (String) boundIndices.get(key2).getFsValue());

                                String fsValue = (String) boundIndices.get(key2).getFsValue();
                                if ((fsValue.startsWith("'") && fsValue.endsWith("'")) ||
                                        (fsValue.startsWith("\"") && fsValue.endsWith("\""))) {
                                    fsValue = fsValue.substring(1, fsValue.length() - 1);
                                }

                                varMatch = fsValue;

                            } else {
                                varMatch = newValueBindings.get(key).get(right.getQuery());
                                if ((varMatch.startsWith("'") && varMatch.endsWith("'")) ||
                                        (varMatch.startsWith("\"") && varMatch.endsWith("\""))) {
                                    varMatch = varMatch.substring(1, varMatch.length() - 1);
                                }
                            }

                            String fsValue = (String) boundIndices.get(key2).getFsValue();
                            if ((fsValue.startsWith("'") && fsValue.endsWith("'")) ||
                                    (fsValue.startsWith("\"") && fsValue.endsWith("\""))) {
                                fsValue = fsValue.substring(1, fsValue.length() - 1);
                            }

                            if (fsValue.equals(varMatch)) {
                                matchingIndices.put(key2, boundIndices.get(key2));
                            } else {
                                nonBoundIndices.add(key2);
                            }
                        }
                    }
                }
                else
                {
                    String varMatch = right.getQuery();

                    for (Integer key2 : boundIndices.keySet()) {
                        if (boundIndices.get(key2).getRelationLabel().equals(left.getQuery())) {
                            String fsValue = (String) boundIndices.get(key2).getFsValue();
                            if ((fsValue.startsWith("'") && fsValue.endsWith("'")) ||
                                    (fsValue.startsWith("\"") && fsValue.endsWith("\""))) {
                                fsValue = fsValue.substring(1, fsValue.length() - 1);
                            }

                            if (fsValue.equals(varMatch)) {
                                matchingIndices.put(key2, boundIndices.get(key2));
                            } else {
                                nonBoundIndices.add(key2);
                            }
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

    private String normalizeFilterValue(String raw) {
        if (raw == null) {
            return null;
        }

        String normalized = raw.trim();
        if (normalized.startsWith("value=")) {
            normalized = normalized.substring("value=".length());
        }
        if ((normalized.startsWith("'") && normalized.endsWith("'")) ||
                (normalized.startsWith("\"") && normalized.endsWith("\""))) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

    private String normalizeEdgeFilter(String raw) {
        if (raw == null) {
            return null;
        }
        if (raw.startsWith("edge=")) {
            return raw.substring("edge=".length());
        }
        return raw;
    }

    private String normalizeGraphValue(String raw) {
        if (raw == null) {
            return null;
        }

        String normalized = raw.trim();
        if ((normalized.startsWith("'") && normalized.endsWith("'")) ||
                (normalized.startsWith("\"") && normalized.endsWith("\""))) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }
}
