package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;

import java.util.HashMap;
import java.util.Set;

final class SeedExpression extends QueryExpression {

    SeedExpression(HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> solution,
                   QueryParser parser) {
        setParser(parser);
        setFsIndices(parser.getFsIndices());
        setSolution(solution);
    }

    @Override
    public void calculateSolutions() {
        // Seed expression only provides the initial solution snapshot.
    }
}
