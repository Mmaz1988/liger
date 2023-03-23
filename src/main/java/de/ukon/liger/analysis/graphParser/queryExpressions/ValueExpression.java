package de.ukon.liger.analysis.graphParser.queryExpressions;

import de.ukon.liger.analysis.graphParser.LigerGraph.LigerGraph;

public class ValueExpression extends QueryExpression{
    public QueryExpression left;
    public QueryExpression right;

    public ValueExpression(String name, LigerGraph lg, QueryExpression left, QueryExpression right)
    {
        super(name,lg);
        this.left = left;
        this.right = right;


    }
}
