package analysis.QueryParser;

import syntax.xle.Prolog2Java.GraphConstraint;
import utilities.HelperMethods;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class QueryExpression {

    private QueryParser parser;
    private String query;
    private HashMap<Integer, GraphConstraint> fsIndices;
    private String nodeVar;

    private HashMap<Set<String>,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> solution = new HashMap<>();

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

                if (HelperMethods.isInteger(fsIndices.get(key2).getFsValue())) {
                    if (Integer.parseInt((String) fsIndices.get(key2).getFsValue()) == (Integer.parseInt(key))) {
                        out.put(key2, fsIndices.get(key2));
                    }
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

    public HashMap<Set<String>,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> getSolution() {
        return solution;
    }

    public void setSolution(HashMap<Set<String>,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> solution) {
        this.solution = solution;
    }

    public abstract void calculateSolutions();


    public HashMap<Set<String>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>>
    mapUsedKeys(Set<String> usedKeys, HashMap<Integer,GraphConstraint> fsIndices, String nodeVar) {

        HashMap<Set<String>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out2 = new HashMap<>();


        for (String fs : usedKeys) {
            HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new HashMap<>();
            binding.put(nodeVar, new HashMap<>());
            HashMap<String, HashMap<Integer, GraphConstraint>> reference = new HashMap<>();
            reference.put(fs, new HashMap<>());

            for (Integer key : fsIndices.keySet()) {
                if (fsIndices.get(key).getFsNode().equals(fs)) {
                    reference.get(fs).put(key, fsIndices.get(key));
                }
            }
            binding.put(nodeVar, reference);
            out2.put(Collections.singleton(nodeVar+fs), binding);
        }

        return out2;
    }

}
