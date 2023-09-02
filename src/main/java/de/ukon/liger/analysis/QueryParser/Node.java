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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Node extends QueryExpression {

    public boolean constant;

    public Node(String query, String nodeVar, HashMap fsIndices, boolean constant, QueryParser parser)
    {

        this.constant = constant;
        setNodeVar(nodeVar);
        setQuery(query);
        setParser(parser);
        setFsIndices(fsIndices);
        calculateSolutions();


}

    public Node(String nodeVar, boolean constant, HashMap fsIndices)
    {
        this.constant = constant;
        setNodeVar(nodeVar);
        setFsIndices(fsIndices);
        calculateSolutions();


    }


@Override
public void calculateSolutions()
{

    HashMap<Set<SolutionKey>,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> out = new HashMap<>();


    if (constant)
    {
        HashMap<String,HashMap<Integer,GraphConstraint>> reference = new HashMap<>();

        reference.put(getNodeVar(),new HashMap<>());

        for (Integer key : getFsIndices().keySet())
        {
            if (getFsIndices().get(key).getFsNode().equals(getNodeVar()))
            {
                reference.get(getNodeVar()).put(key,getFsIndices().get(key));
            }
        }

        HashMap<String,HashMap<String,HashMap<Integer,GraphConstraint>>> binding = new HashMap<>();
        binding.put(getNodeVar(),reference);
        SolutionKey key = new SolutionKey(getNodeVar(),getNodeVar());
        out.put(Collections.singleton(key),binding);
        setSolution(out);
        return;
    }

    //Collect all available nodes in graph
    Set<String> usedKeys = new HashSet<>();
    for (Integer key : getFsIndices().keySet())
    {
        usedKeys.add(getFsIndices().get(key).getFsNode());
        if (HelperMethods.isInteger(getFsIndices().get(key).getFsValue()))
        {
            usedKeys.add(getFsIndices().get(key).getFsValue().toString());
        }
    }


        for (String fs : usedKeys)
        {
            HashMap<String,HashMap<Integer,GraphConstraint>> reference = new HashMap<>();
            reference.put(fs,new HashMap<>());
            HashMap<String,HashMap<String,HashMap<Integer,GraphConstraint>>> binding = new HashMap<>();
            binding.put(getNodeVar(),new HashMap<>());



            for (Integer key : getFsIndices().keySet())
        {
            if (getFsIndices().get(key).getFsNode().equals(fs))
            {
                reference.get(fs).put(key,getFsIndices().get(key));
            }
            }


            // String key = getNodeVar()+fs;
            SolutionKey key = new SolutionKey(getNodeVar(),fs);
            binding.put(getNodeVar(),reference);
            out.put(Collections.singleton(key),binding);
        }


        setSolution(out);

}


}