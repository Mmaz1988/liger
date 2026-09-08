package de.ukon.liger.webservice.rest.dtos;

import java.util.List;

public class LigerSolutionAnnotationResponse {

    public String sentence;
    public List<LigerSolutionAnnotation> solutions;
    public boolean success = true;
    public Integer failedSentenceIndex;
    public String failureMessage;

    public java.util.LinkedHashMap<String, Object> structureJson;

    public LigerSolutionAnnotationResponse(String sentence, List<LigerSolutionAnnotation> solutions) {
        this.sentence = sentence;
        this.solutions = solutions;
    }

    public LigerSolutionAnnotationResponse(String sentence, List<LigerSolutionAnnotation> solutions, java.util.LinkedHashMap<String, Object> structureJson) {
        this.sentence = sentence;
        this.solutions = solutions;
        this.structureJson = structureJson;
    }

    public static LigerSolutionAnnotationResponse failure(String sentence,
                                                          int failedSentenceIndex,
                                                          String failureMessage) {
        LigerSolutionAnnotationResponse response =
                new LigerSolutionAnnotationResponse(sentence, new java.util.ArrayList<>());
        response.success = false;
        response.failedSentenceIndex = failedSentenceIndex;
        response.failureMessage = failureMessage;
        return response;
    }
}
