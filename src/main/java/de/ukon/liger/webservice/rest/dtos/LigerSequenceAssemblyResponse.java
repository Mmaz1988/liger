package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;
import java.util.List;

public class LigerSequenceAssemblyResponse {
    public String id;
    public String side;
    public LigerWebGraph graph;
    public LinkedHashMap<String, Object> structureJson;
    public LinkedHashMap<String, String> rebasedIds;
    public List<Provenance> provenance;

    public record Provenance(int sourceIndex, String sentenceId, String syntaxVariantId,
                             String solutionKey, String side, String rootId,
                             LinkedHashMap<String, String> rebasedIds) {}
}
