package de.ukon.liger.webservice.rest.dtos;

import java.util.List;

public class LigerMultipleRequest {

   public List<String> sentences;
   public String ruleString;

    public LigerMultipleRequest(List<String> sentences, String ruleString) {
        this.sentences = sentences;
        this.ruleString = ruleString;
    }
}
