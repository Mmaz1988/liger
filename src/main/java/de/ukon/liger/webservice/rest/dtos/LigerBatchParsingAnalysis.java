package de.ukon.liger.webservice.rest.dtos;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class LigerBatchParsingAnalysis {
    /** One {@link LigerSolutionAnnotationResponse} per sentence id -- exactly what
     *  {@code /apply_rules_xle} returns for that sentence, so a batch parse is N single
     *  parses and consumers need no special handling. Was a single
     *  {@code LigerRuleAnnotation} per sentence, which carried no structureJson, no
     *  structureVariants and every analysis's meaning constructors concatenated into one
     *  string. */
    public HashMap<String,LigerSolutionAnnotationResponse> annotations;
    public List<LigerGraphComponent> ruleApplicationGraph;

    public String report;

    /** Per sentence id: milliseconds to parse it with XLE and apply the rules, in the order
     *  the batch ran them. Lets a client name the slowest and fastest sentence. */
    public LinkedHashMap<String, Long> timingsMs = new LinkedHashMap<>();

    public LigerBatchParsingAnalysis(HashMap<String,LigerSolutionAnnotationResponse> annotations, List<LigerGraphComponent> ruleApplicationGraph, String report) {
        this.annotations = annotations;
        this.ruleApplicationGraph = ruleApplicationGraph;
        this.report = report;
    }
}
