package de.ukon.liger.webservice.rest.dtos;

import java.util.HashMap;

public class LigerMultipleRequest {

   public HashMap<String,String> sentences;
   public String ruleString;
   public String logicType;

    public LigerMultipleRequest(HashMap<String,String> sentences, String ruleString, String logicType) {
        this.sentences = sentences;
        this.ruleString = ruleString;
        this.logicType = logicType;
    }
}
