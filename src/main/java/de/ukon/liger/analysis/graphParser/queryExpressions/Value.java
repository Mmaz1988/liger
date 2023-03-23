package de.ukon.liger.analysis.graphParser.queryExpressions;

import de.ukon.liger.analysis.graphParser.LigerGraph.LigerGraph;

import javax.management.Query;

public class Value extends QueryExpression {

    public String modifier;
    public Value(String name, LigerGraph lg) {
        super(name,lg);
    }
    public Value(String name, LigerGraph lg, String modifier) {
        super(name,lg);
        this.modifier = modifier;
    }
}
