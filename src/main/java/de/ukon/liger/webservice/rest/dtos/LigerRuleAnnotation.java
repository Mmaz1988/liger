package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;

import de.ukon.liger.syntax.GraphConstraint;

public class LigerRuleAnnotation {

    public String sentence;

    public LigerWebGraph graph;

    public LinkedHashMap<String, Object> structureJson;

    public LinkedHashSet<LigerRule> appliedRules;

    public LinkedHashSet<String> highlightedNodeIds;

    public LinkedHashMap<Integer, LinkedHashSet<String>> highlightedNodeIdsByRule;

    public LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> addedAnnotationsByRule;

    public List<LinkedHashMap<String, Object>> structureVariants;

    public List<LigerWebGraph> structureVariantGraphs;

    public String meaningConstructors;

    public int numberOfMCsets;

    public List<String> axioms;

    public LigerRuleAnnotation(LigerWebGraph graph, LinkedHashSet<LigerRule> appliedRules)
    {
        this.graph = graph;
        this.appliedRules = appliedRules;
        this.highlightedNodeIds = new LinkedHashSet<>();
        this.highlightedNodeIdsByRule = new LinkedHashMap<>();
        this.addedAnnotationsByRule = new LinkedHashMap<>();
        this.structureVariants = new java.util.ArrayList<>();
        this.structureVariantGraphs = new java.util.ArrayList<>();
    }

    public LigerRuleAnnotation(LigerWebGraph graph, LinkedHashSet<LigerRule> appliedRules, LinkedHashMap<String, Object> structureJson)
    {
        this.graph = graph;
        this.appliedRules = appliedRules;
        this.structureJson = structureJson;
        this.highlightedNodeIds = new LinkedHashSet<>();
        this.highlightedNodeIdsByRule = new LinkedHashMap<>();
        this.addedAnnotationsByRule = new LinkedHashMap<>();
        this.structureVariants = new java.util.ArrayList<>();
        this.structureVariantGraphs = new java.util.ArrayList<>();
    }

    public LigerRuleAnnotation(LigerWebGraph graph, LinkedHashSet<LigerRule> appliedRules, String meaningConstructors, List<String> axioms)
    {
        this.graph = graph;
        this.appliedRules = appliedRules;
        this.meaningConstructors = meaningConstructors;
        this.axioms = axioms;
        this.highlightedNodeIds = new LinkedHashSet<>();
        this.highlightedNodeIdsByRule = new LinkedHashMap<>();
        this.addedAnnotationsByRule = new LinkedHashMap<>();
        this.structureVariants = new java.util.ArrayList<>();
        this.structureVariantGraphs = new java.util.ArrayList<>();
    }

    public LigerRuleAnnotation(String sentence, LigerWebGraph graph, LinkedHashSet<LigerRule> appliedRules, String meaningConstructors, int numberOfMCsets, List<String> axioms)
    {
        this.sentence = sentence;
        this.graph = graph;
        this.appliedRules = appliedRules;
        this.meaningConstructors = meaningConstructors;
        this.numberOfMCsets = numberOfMCsets;
        this.axioms = axioms;
        this.highlightedNodeIds = new LinkedHashSet<>();
        this.highlightedNodeIdsByRule = new LinkedHashMap<>();
        this.addedAnnotationsByRule = new LinkedHashMap<>();
        this.structureVariants = new java.util.ArrayList<>();
        this.structureVariantGraphs = new java.util.ArrayList<>();
    }

    public LigerRuleAnnotation(String sentence, LigerWebGraph graph, LinkedHashSet<LigerRule> appliedRules, String meaningConstructors, int numberOfMCsets, List<String> axioms, LinkedHashMap<String, Object> structureJson)
    {
        this.sentence = sentence;
        this.graph = graph;
        this.appliedRules = appliedRules;
        this.meaningConstructors = meaningConstructors;
        this.numberOfMCsets = numberOfMCsets;
        this.axioms = axioms;
        this.structureJson = structureJson;
        this.highlightedNodeIds = new LinkedHashSet<>();
        this.highlightedNodeIdsByRule = new LinkedHashMap<>();
        this.addedAnnotationsByRule = new LinkedHashMap<>();
        this.structureVariants = new java.util.ArrayList<>();
        this.structureVariantGraphs = new java.util.ArrayList<>();
    }
}
