package de.ukon.liger.analysis.graphParser.queryExpressions;

import de.ukon.liger.analysis.graphParser.LigerGraph.LigerGraph;

public class AttributeExpression extends QueryExpression {


    public QueryExpression left;
    public QueryExpression right;

    public AttributeExpression(String name, LigerGraph lg, QueryExpression left, QueryExpression right)
    {
        super(name,lg);
        this.left = left;
        this.right = right;


    }


    public void calculateSolution(){

        if (left instanceof Node || left instanceof NodeExpression)
        {

        }

    }

}
