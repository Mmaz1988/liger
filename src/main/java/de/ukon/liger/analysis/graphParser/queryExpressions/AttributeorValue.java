package de.ukon.liger.analysis.graphParser.queryExpressions;

import de.ukon.liger.analysis.graphParser.LigerGraph.LigerGraph;
import de.ukon.liger.analysis.graphParser.queryExpressions.QueryExpression;

public class AttributeorValue extends QueryExpression {
    public AttributeorValue(String name, LigerGraph lg) {
        super(name, lg);
    }

    public AttributeorValue(String name) {
        super(name);
    }
}
