package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;

public class LigerMergeResponse {

    public LigerWebGraph graph;
    public LinkedHashMap<String, Object> structureJson;
    public List<LinkedHashMap<String, Object>> structureVariants = new ArrayList<>();
    public List<LigerWebGraph> structureVariantGraphs = new ArrayList<>();

    public LigerMergeResponse() {
    }

    public LigerMergeResponse(LigerWebGraph graph) {
        this.graph = graph;
    }

    public LigerMergeResponse(LigerWebGraph graph, LinkedHashMap<String, Object> structureJson) {
        this.graph = graph;
        this.structureJson = structureJson;
    }
}
