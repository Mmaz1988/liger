package de.ukon.liger.webservice.rest.dtos;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class LigerMultipleRequest {

   public HashMap<String,String> sentences;
   public String ruleString;

    public LigerMultipleRequest(HashMap<String,String> sentences, String ruleString) {
        this.sentences = sentences;
        this.ruleString = ruleString;
    }
}
