package de.ukon.liger.webservice.rest.dtos;

import java.util.List;

public class LigerStructureQueryResponse {
    public String success;
    public int matchCount;
    public LigerWebGraph graph;
    public List<LigerQuerySolution> solutions;

    public LigerStructureQueryResponse(String success, int matchCount, LigerWebGraph graph, List<LigerQuerySolution> solutions) {
        this.success = success;
        this.matchCount = matchCount;
        this.graph = graph;
        this.solutions = solutions;
    }
}
