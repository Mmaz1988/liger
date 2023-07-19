package de.ukon.liger.webservice.rest.dtos;

import java.util.HashMap;
import java.util.List;

public class LigerBatchParsingAnalysis {
    public HashMap<Integer,LigerRuleAnnotation> annotations;
    public List<LigerGraphComponent> ruleApplicationGraph;

    public LigerBatchParsingAnalysis(HashMap<Integer,LigerRuleAnnotation> annotations, List<LigerGraphComponent> ruleApplicationGraph) {
        this.annotations = annotations;
        this.ruleApplicationGraph = ruleApplicationGraph;
    }
}
