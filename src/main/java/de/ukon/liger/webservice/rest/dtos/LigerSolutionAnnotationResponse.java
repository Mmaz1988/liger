package de.ukon.liger.webservice.rest.dtos;

import java.util.List;

public class LigerSolutionAnnotationResponse {

    public String sentence;
    public List<LigerSolutionAnnotation> solutions;

    public LigerSolutionAnnotationResponse(String sentence, List<LigerSolutionAnnotation> solutions) {
        this.sentence = sentence;
        this.solutions = solutions;
    }
}
