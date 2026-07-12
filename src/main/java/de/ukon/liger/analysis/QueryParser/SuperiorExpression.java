package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;

import java.util.HashMap;
import java.util.Set;

public class SuperiorExpression extends QueryExpression {

    private final QueryExpression left;
    private final Superior right;

    public SuperiorExpression(QueryExpression left, Superior right) {
        this.left = left;
        this.right = right;
        setParser(left.getParser());
        setFsIndices(left.getFsIndices());
        calculateSolutions();
    }

    @Override
    public void calculateSolutions() {
        if (getParser() != null) {
            getParser().addSuperiorConstraint(right);
        }

        setConjoinedSolutions(left.getConjoinedSolutions());
        setSolution(left.getSolution());
    }
}
