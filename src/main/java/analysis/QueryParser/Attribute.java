package analysis.QueryParser;

import syntax.xle.Prolog2Java.GraphConstraint;

import java.util.HashMap;

public class Attribute extends QueryExpression {

    public Attribute(String query, HashMap<Integer, GraphConstraint> fsIndices, QueryParser parser) {
        super(query, fsIndices,parser);

        HashMap<Integer,GraphConstraint> fs = new HashMap<>();

        for (int key : fsIndices.keySet())
        {
            if (fsIndices.get(key).getRelationLabel().equals(query))
            {
                fs.put(key,fsIndices.get(key));
            }
        }
        setFsIndices(fs);
    }



    @Override
    public void calculateSolutions()
    {

    }


}

