package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;

/** Semantic analysis contract shared with the GSWB sequence workflow. */
public class SemanticAnalysis {
    public String syntacticOrigin;
    public String semId;
    public String semString;
    public LinkedHashMap<String, Object> structure;
    public LigerWebGraph graph;
    public String semType;
    public String svg;
    public String prologRender;

    public SemanticAnalysis() {
    }
}
