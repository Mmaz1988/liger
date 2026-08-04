package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

public class LigerRuleAnnotationResponse {

    public String sentence;

    public List<LigerRuleAnnotation> annotations;

    public LigerRuleAnnotationResponse(String sentence, List<LigerRuleAnnotation> annotations) {
        this.sentence = sentence;
        this.annotations = annotations == null ? new ArrayList<>() : annotations;
    }
}
