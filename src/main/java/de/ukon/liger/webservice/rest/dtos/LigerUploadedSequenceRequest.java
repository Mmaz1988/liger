package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class LigerUploadedSequenceRequest {
    public String id;
    public String side;
    public List<Part> structures = new ArrayList<>();

    public static class Part {
        public String sentenceId;
        public String syntaxVariantId;
        public String solutionKey;
        public LinkedHashMap<String, Object> structure;
    }
}
