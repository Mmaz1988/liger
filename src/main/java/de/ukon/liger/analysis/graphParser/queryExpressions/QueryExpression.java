package de.ukon.liger.analysis.graphParser.queryExpressions;

import de.ukon.liger.analysis.graphParser.LigerGraph.LigerGraph;

public class QueryExpression {

    public String name;
    public LigerGraph lg;

    public QueryExpression(String name, LigerGraph lg)
    {
    this.name = name;
    this.lg = lg;
    }

    public QueryExpression(String name)
    {
        this.name = name;
    }



}
