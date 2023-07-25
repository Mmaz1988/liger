package de.ukon.liger.webservice.rest.dtos;

import java.util.HashMap;
import java.util.List;

public class LigerBatchParsingAnalysis {
    public HashMap<String,LigerRuleAnnotation> annotations;
    public List<LigerGraphComponent> ruleApplicationGraph;

    public String report;

    public LigerBatchParsingAnalysis(HashMap<String,LigerRuleAnnotation> annotations, List<LigerGraphComponent> ruleApplicationGraph, String report) {
        this.annotations = annotations;
        this.ruleApplicationGraph = ruleApplicationGraph;
        this.report = report;
    }
}
