package analysis.QueryParser;


import syntax.GraphConstraint;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Node extends QueryExpression {

    public Node(String query, String nodeVar, HashMap fsIndices, QueryParser parser)
    {

        setNodeVar(nodeVar);
        setQuery(query);
        setParser(parser);
        setFsIndices(fsIndices);
        calculateSolutions();


}

    public Node(String nodeVar, HashMap fsIndices)
    {

        setNodeVar(nodeVar);
        setFsIndices(fsIndices);
        calculateSolutions();


    }


@Override
public void calculateSolutions()
{

    HashMap<Set<String>,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> out = new HashMap<>();

    Set<String> usedKeys = new HashSet<>();
    for (Integer key : getFsIndices().keySet())
    {
        usedKeys.add(getFsIndices().get(key).getFsNode());
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

            String key = getNodeVar()+fs;
            binding.put(getNodeVar(),reference);
            out.put(Collections.singleton(key),binding);
        }

        setSolution(out);

}


}