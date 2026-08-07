package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;

/** A named LiGER syntax structure and its graph representation. */
public class SyntacticAnalysis {
    public String synId;
    public LinkedHashMap<String, Object> structure;
    public LigerWebGraph graph;
    public String meaningConstructors;
    public int numberOfMCsets;

    public SyntacticAnalysis() {
    }

    public SyntacticAnalysis(String synId, LinkedHashMap<String, Object> structure,
                             LigerWebGraph graph, String meaningConstructors,
                             int numberOfMCsets) {
        this.synId = synId;
        this.structure = structure;
        this.graph = graph;
        this.meaningConstructors = meaningConstructors;
        this.numberOfMCsets = numberOfMCsets;
    }
}
