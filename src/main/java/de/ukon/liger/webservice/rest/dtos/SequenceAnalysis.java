package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Canonical sequence container with syntax-semantic provenance. */
public class SequenceAnalysis {
    public String id;
    public String text;
    public List<SentenceAnalysis> sentences = new ArrayList<>();
    public List<SyntacticAnalysis> syntax = new ArrayList<>();
    public List<SemanticAnalysis> semantics = new ArrayList<>();
    public LinkedHashMap<String, List<String>> synSemMapping = new LinkedHashMap<>();

    public SequenceAnalysis() {
    }

    public SequenceAnalysis(String id, String text) {
        this.id = id;
        this.text = text;
    }
}
