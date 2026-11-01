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

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Node extends QueryExpression {

    public boolean constant;
    /** Whether the candidates are computed: only when read, since a variable already bound by
     *  the matches before it (or by a negation's seed) never reads them. */
    private boolean computed;

    public Node(String query, String nodeVar, HashMap fsIndices, boolean constant, QueryParser parser)
    {

        this.constant = constant;
        setNodeVar(nodeVar);
        setQuery(query);
        setParser(parser);
        setFsIndices(fsIndices);


}

    public Node(String nodeVar, boolean constant, HashMap fsIndices)
    {
        this.constant = constant;
        setNodeVar(nodeVar);
        setFsIndices(fsIndices);
        calculateSolutions();


    }


@Override
public HashMap<Solution,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> getSolution()
{
    if (!computed) {
        computed = true;
        calculateSolutions();
    }
    return super.getSolution();
}

@Override
public void setSolution(HashMap<Solution,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> solution)
{
    computed = true;
    super.setSolution(solution);
}

@Override
public void calculateSolutions()
{
    GraphIndex index = getParser() != null && getParser().getFsIndices() == getFsIndices()
            ? getParser().graphIndex() : null;
    if (index != null) {
        calculateSolutions(index);
        return;
    }

    HashMap<Solution,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> out = new LinkedHashMap<>();


    if (constant)
    {
        HashMap<String,HashMap<Integer,GraphConstraint>> reference = new LinkedHashMap<>();

        reference.put(getNodeVar(),new LinkedHashMap<>());

        for (Integer key : getFsIndices().keySet())
        {
            if (HelperMethods.nodeIdsEqual(getFsIndices().get(key).getFsNode(), getNodeVar()))
            {
                reference.get(getNodeVar()).put(key,getFsIndices().get(key));
            }
        }

        HashMap<String,HashMap<String,HashMap<Integer,GraphConstraint>>> binding = new LinkedHashMap<>();
        binding.put(getNodeVar(),reference);
        SolutionKey key = new SolutionKey(getNodeVar(),getNodeVar());
        out.put(new Solution(Collections.singleton(key)),binding);
        setSolution(out);
        return;
    }

    //Collect all available nodes in graph
    Set<String> usedKeys = new LinkedHashSet<>();
    for (Integer key : getFsIndices().keySet())
    {
        usedKeys.add(getFsIndices().get(key).getFsNode());
    }
    for (Integer key : getFsIndices().keySet())
    {
        Object value = getFsIndices().get(key).getFsValue();
        if (HelperMethods.isNodeReference(value))
        {
            usedKeys.add(String.valueOf(value));
        }
    }


    // Each node's facts, in fact order: one pass instead of one per node
    HashMap<String,HashMap<Integer,GraphConstraint>> factsByNode = new HashMap<>();
    for (Integer key : getFsIndices().keySet())
    {
        GraphConstraint fact = getFsIndices().get(key);
        factsByNode.computeIfAbsent(fact.getFsNode(), node -> new LinkedHashMap<>()).put(key, fact);
    }


        for (String fs : usedKeys)
        {
            HashMap<String,HashMap<Integer,GraphConstraint>> reference = new LinkedHashMap<>();
            reference.put(fs,new LinkedHashMap<>(factsByNode.getOrDefault(fs, new LinkedHashMap<>())));
            HashMap<String,HashMap<String,HashMap<Integer,GraphConstraint>>> binding = new LinkedHashMap<>();
            binding.put(getNodeVar(),new LinkedHashMap<>());


            // String key = getNodeVar()+fs;
            SolutionKey key = new SolutionKey(getNodeVar(),fs);
            binding.put(getNodeVar(),reference);
            out.put(new Solution(Collections.singleton(key)),binding);
        }


        setSolution(out);

}



/** The same candidates from the parser's index: every node in Node's order, each with a copy of
 *  its facts (later steps narrow a candidate's facts in place). */
private void calculateSolutions(GraphIndex index)
{
    HashMap<Solution,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> out = new LinkedHashMap<>();
    if (constant)
    {
        HashMap<String,HashMap<Integer,GraphConstraint>> reference = new LinkedHashMap<>();
        reference.put(getNodeVar(), new LinkedHashMap<>(index.ofNode(getNodeVar())));
        HashMap<String,HashMap<String,HashMap<Integer,GraphConstraint>>> binding = new LinkedHashMap<>();
        binding.put(getNodeVar(),reference);
        out.put(new Solution(Collections.singleton(new SolutionKey(getNodeVar(),getNodeVar()))),binding);
        super.setSolution(out);
        return;
    }
    for (String fs : index.nodes())
    {
        HashMap<String,HashMap<Integer,GraphConstraint>> reference = new LinkedHashMap<>();
        reference.put(fs,new LinkedHashMap<>(index.ofNode(fs)));
        HashMap<String,HashMap<String,HashMap<Integer,GraphConstraint>>> binding = new LinkedHashMap<>();
        binding.put(getNodeVar(),reference);
        out.put(new Solution(Collections.singleton(new SolutionKey(getNodeVar(),fs))),binding);
    }
    super.setSolution(out);
}

}
