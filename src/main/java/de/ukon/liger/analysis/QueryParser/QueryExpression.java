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
import de.ukon.liger.packing.ChoiceContext;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.utilities.HelperMethods;

import java.util.*;

public abstract class QueryExpression {

    private QueryParser parser;
    private String query;
    private HashMap<Integer, GraphConstraint> fsIndices;
    private String nodeVar;

    private HashMap<Solution,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> solution = new HashMap<>();



    private List<HashMap<Solution,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>>> conjoinedSolutions = new ArrayList<>();

    public QueryExpression()
    {

    }
    public QueryExpression(String query, HashMap<Integer, GraphConstraint> fsIndices, QueryParser parser) {
        setQuery(query);
        setFsIndices(fsIndices);
        setParser(parser);
    }


    public HashMap<Integer,GraphConstraint> calculateInsideOutIndices(HashMap<Integer,GraphConstraint> in, HashMap<Integer,GraphConstraint> fsIndices)
    {
        Set<String> fsNodes = new HashSet<>();
        HashMap<Integer,GraphConstraint> out = new HashMap<>();

        for (Integer key : in.keySet())
        {
            fsNodes.add(fsIndices.get(key).getFsNode());
        }

        for (String key : fsNodes)
        {
            for (Integer key2 : fsIndices.keySet())
            {
                if (key.equals(String.valueOf(fsIndices.get(key2).getFsValue()))) {
                    out.put(key2, fsIndices.get(key2));
                }
            }
        }


        return out;
    }


    public QueryParser getParser() {
        return parser;
    }

    public void setParser(QueryParser parser) {
        this.parser = parser;
    }

    public HashMap<Integer, GraphConstraint> getFsIndices() {
        return fsIndices;
    }

    public void setFsIndices(HashMap<Integer, GraphConstraint> fsIndices) {
        this.fsIndices = fsIndices;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getNodeVar() {
        return nodeVar;
    }

    public void setNodeVar(String nodeVar) {
        this.nodeVar = nodeVar;
    }


    //Test

    public HashMap<Solution,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> getSolution() {
        return solution;
    }

    public void setSolution(HashMap<Solution,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> solution) {
        this.solution = solution;
    }

    public abstract void calculateSolutions();


    public HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>>
    mapUsedKeys(Set<String> usedKeys, HashMap<Integer,GraphConstraint> fsIndices, String nodeVar) {

        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out2 = new HashMap<>();


        for (String fs : usedKeys) {
            HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new HashMap<>();
            binding.put(nodeVar, new HashMap<>());
            HashMap<String, HashMap<Integer, GraphConstraint>> reference = new HashMap<>();
            reference.put(fs, new HashMap<>());

            /*
            for (Integer key : fsIndices.keySet()) {
                if (fsIndices.get(key).getFsNode().equals(fs)) {
                    reference.get(fs).put(key, fsIndices.get(key));
                }
            }
             */
            binding.put(nodeVar, reference);
            Solution solution = new Solution(Collections.singleton(new SolutionKey(nodeVar, fs)));
            Set<Set<ChoiceVar>> contexts = new LinkedHashSet<>();
            for (GraphConstraint constraint : fsIndices.values()) {
                if (fs.equals(constraint.getFsNode()) && !ChoiceContext.isRoot(constraint.getReading())) {
                    contexts.add(constraint.getReading());
                }
            }
            if (contexts.isEmpty()) {
                contexts.add(Collections.singleton(new ChoiceVar("1")));
            }
            solution.setChoiceContexts(contexts);
            out2.put(solution, binding);
        }

        return out2;
    }

    public HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>>
    mergeSolutions(HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> solution)
    {
        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> merged = new HashMap<>();

        return null;
    }

    public List<HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>>> getConjoinedSolutions() {
        return conjoinedSolutions;
    }

    public void setConjoinedSolutions(List<HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>>> conjoinedSolutions) {
        this.conjoinedSolutions = conjoinedSolutions;
    }

}
