package analysis.QueryParser;

import syntax.GraphConstraint;

import java.util.HashMap;

public class Value extends QueryExpression {

    public boolean var;
    public boolean strip;

    public Value(String query, HashMap<Integer, GraphConstraint> fsIndices, Boolean var, Boolean strip, QueryParser parser) {
        super(query, fsIndices,parser);
        this.var = var;
        this.strip = strip;
    }




    @Override
    public void calculateSolutions()
    {

    }
}
