package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

public class LigerRuleAnnotationResponse {

    public String sentence;

    public List<LigerRuleAnnotation> annotations;

    /** Set when rules deleted every branch: there is no annotation, and this says why. */
    public String failureMessage;

    public LigerRuleAnnotationResponse(String sentence, List<LigerRuleAnnotation> annotations) {
        this.sentence = sentence;
        this.annotations = annotations == null ? new ArrayList<>() : annotations;
    }
}
