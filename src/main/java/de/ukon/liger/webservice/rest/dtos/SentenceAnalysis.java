package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Canonical sentence container. Semantic fields are populated by GSWB. */
public class SentenceAnalysis {
    public String id;
    public String text;
    public List<SyntacticAnalysis> syntax = new ArrayList<>();
    public List<SemanticAnalysis> semantics = new ArrayList<>();
    public LinkedHashMap<String, List<String>> synSemMapping = new LinkedHashMap<>();

    public SentenceAnalysis() {
    }

    public SentenceAnalysis(String id, String text) {
        this.id = id;
        this.text = text;
    }
}
