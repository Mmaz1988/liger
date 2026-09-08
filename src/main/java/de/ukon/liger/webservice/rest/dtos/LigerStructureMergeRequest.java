package de.ukon.liger.webservice.rest.dtos;

import de.ukon.liger.syntax.LinguisticStructure;

import java.util.LinkedHashMap;

public class LigerStructureMergeRequest {

    public LinkedHashMap<String, Object> syntax;
    public LigerWebGraph syntaxGraph;
    public LinkedHashMap<String, Object> drs;

    /** Whether to render the merged structure to a LigerWebGraph as well.
     *
     *  Absent means yes, so every existing caller is unaffected. Callers that only feed
     *  the merged structure into the next request -- the client's discourse/reasoning
     *  pipeline does exactly that -- send false: the rendering is roughly a third of this
     *  response, and it was being downloaded and discarded on every rule branch. */
    public Boolean includeGraph;

    public LigerStructureMergeRequest() {
    }

    public LigerStructureMergeRequest(LinkedHashMap<String, Object> syntax, LinkedHashMap<String, Object> drs) {
        this.syntax = syntax;
        this.drs = drs;
    }

    public LigerStructureMergeRequest(LigerWebGraph syntaxGraph, LinkedHashMap<String, Object> drs) {
        this.syntaxGraph = syntaxGraph;
        this.drs = drs;
    }
}
