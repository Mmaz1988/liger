package de.ukon.liger.webservice.rest.dtos;

import java.util.HashMap;

public class LigerMultipleRequest {

   public HashMap<String,String> sentences;
   public String ruleString;
    /** XLE when absent; see {@link LigerParserSelection}. */
    public LigerParserSelection parser;

    public LigerMultipleRequest(HashMap<String,String> sentences, String ruleString) {
        this.sentences = sentences;
        this.ruleString = ruleString;
    }
}
