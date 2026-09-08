package de.ukon.liger.analysis.QueryParser;

import java.util.List;

public class QueryTemplate {
    private final String name;
    private final List<String> parameters;
    private final List<List<String>> alternatives;

    public QueryTemplate(String name, List<String> parameters, List<List<String>> alternatives) {
        this.name = name;
        this.parameters = parameters;
        this.alternatives = alternatives;
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public List<List<String>> getAlternatives() {
        return alternatives;
    }
}
