package de.ukon.liger.webservice.rest.dtos;

import java.util.List;

public class LigerRuleAnnotation {

    public LigerWebGraph graph;

    public List<LigerRule> appliedRules;

    public String meaningConstructors;

    public LigerRuleAnnotation(LigerWebGraph graph, List<LigerRule> appliedRules)
    {
        this.graph = graph;
        this.appliedRules = appliedRules;
    }

    public LigerRuleAnnotation(LigerWebGraph graph, List<LigerRule> appliedRules, String meaningConstructors)
    {
        this.graph = graph;
        this.appliedRules = appliedRules;
        this.meaningConstructors = meaningConstructors;
    }
}
