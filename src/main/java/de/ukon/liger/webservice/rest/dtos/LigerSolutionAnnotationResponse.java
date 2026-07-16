package de.ukon.liger.webservice.rest.dtos;

import java.util.List;

public class LigerSolutionAnnotationResponse {

    public String sentence;
    public List<LigerSolutionAnnotation> solutions;

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
}
