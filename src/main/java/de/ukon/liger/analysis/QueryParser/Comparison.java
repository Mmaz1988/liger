package de.ukon.liger.analysis.QueryParser;

public class Comparison extends QueryExpression {

    public enum Operator {
        LT,
        GT,
        LE,
        GE
    }

    public final Operator operator;

    public Comparison(Operator operator, QueryParser parser) {
        this.operator = operator;
        setParser(parser);
    }

    @Override
    public void calculateSolutions() {
    }
}
