package de.ukon.liger.webservice.rest.dtos;

import java.util.List;

public class LigerRuleAnnotation {

    public LigerWebGraph graph;

    public List<String> appliedRules;

    public LigerRuleAnnotation(LigerWebGraph graph, List<String> appliedRules)
    {
        this.graph = graph;
        this.appliedRules = appliedRules;
    }
}
