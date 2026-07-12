package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Superior extends QueryExpression {

    private static final Pattern SUPERIOR_PATTERN = Pattern.compile("^superior\\(([^,]+),\\s*([^,]+),\\s*([^\\)]+)\\)$");

    private final String hierarchyName;
    private final String superiorVar;
    private final String inferiorVar;

    public Superior(String query, HashMap<Integer, GraphConstraint> fsIndices, QueryParser parser) {
        super(query, fsIndices, parser);
        ParsedSuperior parsedSuperior = parseQuery(query);
        this.hierarchyName = parsedSuperior.hierarchyName();
        this.superiorVar = parsedSuperior.superiorVar();
        this.inferiorVar = parsedSuperior.inferiorVar();
        calculateSolutions();
    }

    @Override
    public void calculateSolutions() {
        setSolution(new HashMap<>());
    }

    public String getHierarchyName() {
        return hierarchyName;
    }

    public String getSuperiorVar() {
        return superiorVar;
    }

    public String getInferiorVar() {
        return inferiorVar;
    }

    private ParsedSuperior parseQuery(String query) {
        Matcher matcher = SUPERIOR_PATTERN.matcher(query.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid superior query: " + query);
        }

        return new ParsedSuperior(matcher.group(1).trim(), matcher.group(2).trim(), matcher.group(3).trim());
    }

    private record ParsedSuperior(String hierarchyName, String superiorVar, String inferiorVar) {}
}
