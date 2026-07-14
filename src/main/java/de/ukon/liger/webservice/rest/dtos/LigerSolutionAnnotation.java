package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashSet;
import java.util.List;

public class LigerSolutionAnnotation {

    public String solutionKey;
    public LigerWebGraph graph;
    public LinkedHashSet<LigerRule> appliedRules;
    public String meaningConstructors;
    public int numberOfMCsets;
    public List<String> axioms;

    public LigerSolutionAnnotation(String solutionKey,
                                   LigerWebGraph graph,
                                   LinkedHashSet<LigerRule> appliedRules,
                                   String meaningConstructors,
                                   int numberOfMCsets,
                                   List<String> axioms) {
        this.solutionKey = solutionKey;
        this.graph = graph;
        this.appliedRules = appliedRules;
        this.meaningConstructors = meaningConstructors;
        this.numberOfMCsets = numberOfMCsets;
        this.axioms = axioms;
    }
}
