package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

public class LigerSequenceRequest {
    public List<String> sentences = new ArrayList<>();
    public List<String> sentenceIds = new ArrayList<>();
    public String ruleString = "";
    public String logicType;
    public boolean packAlternatives;

    public LigerSequenceRequest() {
    }
}
