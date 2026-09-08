package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class LigerSequenceRequest {
    public List<String> sentences = new ArrayList<>();
    public List<String> sentenceIds = new ArrayList<>();
    public String ruleString = "";
    public String logicType;
    public boolean packAlternatives;
    public List<LinkedHashMap<String, Object>> parsedLastSentence = new ArrayList<>();
    public List<List<LinkedHashMap<String, Object>>> parsedSentences = new ArrayList<>();

    public LigerSequenceRequest() {
    }
}
