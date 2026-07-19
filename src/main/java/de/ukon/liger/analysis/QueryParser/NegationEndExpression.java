package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;

import java.util.HashMap;

final class NegationEndExpression extends QueryExpression {

    private final QueryExpression left;
    private final QueryParser parser;

    NegationEndExpression(QueryParser parser) {
        this.left = null;
        this.parser = parser;
        setParser(parser);
        setFsIndices(parser.getFsIndices());
    }

    NegationEndExpression(QueryExpression left, QueryParser parser) {
        this.left = left;
        this.parser = parser;
        setParser(parser);
        setFsIndices(left != null ? left.getFsIndices() : parser.getFsIndices());
        calculateSolutions();
    }

    @Override
    public void calculateSolutions() {
        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> innerResult =
                left != null ? left.getSolution() : new HashMap<>();
        setSolution(parser.endNegationScope(innerResult));
        setConjoinedSolutions(left != null ? left.getConjoinedSolutions() : new java.util.ArrayList<>());
    }
}
