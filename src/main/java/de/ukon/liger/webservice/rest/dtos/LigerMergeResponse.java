package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;

public class LigerMergeResponse {

    public LigerWebGraph graph;
    public LinkedHashMap<String, Object> structureJson;

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
