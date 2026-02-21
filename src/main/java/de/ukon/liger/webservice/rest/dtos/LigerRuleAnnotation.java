package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashSet;
import java.util.List;

public class LigerRuleAnnotation {

    public String sentence;

    public LigerWebGraph graph;

    public LinkedHashSet<LigerRule> appliedRules;

    public String meaningConstructors;

    public int numberOfMCsets;

    public List<String> axioms;

    public LigerRuleAnnotation(LigerWebGraph graph, LinkedHashSet<LigerRule> appliedRules)
    {
        this.graph = graph;
        this.appliedRules = appliedRules;
    }

    public LigerRuleAnnotation(LigerWebGraph graph, LinkedHashSet<LigerRule> appliedRules, String meaningConstructors, List<String> axioms)
    {
        this.graph = graph;
        this.appliedRules = appliedRules;
        this.meaningConstructors = meaningConstructors;
        this.axioms = axioms;
    }

    public LigerRuleAnnotation(String sentence, LigerWebGraph graph, LinkedHashSet<LigerRule> appliedRules, String meaningConstructors, int numberOfMCsets, List<String> axioms)
    {
        this.sentence = sentence;
        this.graph = graph;
        this.appliedRules = appliedRules;
        this.meaningConstructors = meaningConstructors;
        this.numberOfMCsets = numberOfMCsets;
        this.axioms = axioms;
    }
}

