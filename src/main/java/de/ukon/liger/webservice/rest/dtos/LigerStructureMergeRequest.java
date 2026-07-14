package de.ukon.liger.webservice.rest.dtos;

import de.ukon.liger.syntax.LinguisticStructure;

import java.util.LinkedHashMap;

public class LigerStructureMergeRequest {

    public LinkedHashMap<String, Object> syntax;
    public LigerWebGraph syntaxGraph;
    public LinkedHashMap<String, Object> drs;

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
