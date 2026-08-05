package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;

public class LigerNliStructureResponse {
    public String id;
    public LigerWebGraph graph;
    public LinkedHashMap<String, Object> structureJson;
    public String premiseRoot;
    public String conclusionRoot;
    public String boundaryRelation = "NLI_BOUNDARY";
    public boolean directional = true;
}
