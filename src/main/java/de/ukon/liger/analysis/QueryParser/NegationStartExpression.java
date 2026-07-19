package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;

import java.util.HashMap;

final class NegationStartExpression extends QueryExpression {

    private final QueryExpression left;
    private final String innerQuery;

    NegationStartExpression(String innerQuery, QueryParser parser) {
        this.left = null;
        this.innerQuery = innerQuery;
        setParser(parser);
        setFsIndices(parser.getFsIndices());
    }

    NegationStartExpression(QueryExpression left, String innerQuery, QueryParser parser) {
        this.left = left;
        this.innerQuery = innerQuery;
        setParser(parser);
        setFsIndices(left != null ? left.getFsIndices() : parser.getFsIndices());
        calculateSolutions();
    }

    String getInnerQuery() {
        return innerQuery;
    }

    @Override
    public void calculateSolutions() {
        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> current =
                left != null ? left.getSolution() : new HashMap<>();
        if (current.isEmpty()) {
            current.put(new Solution(), new HashMap<>());
        }
        getParser().beginNegationScope(innerQuery, current);
        setSolution(current);
        setConjoinedSolutions(left != null ? left.getConjoinedSolutions() : new java.util.ArrayList<>());
    }
}
