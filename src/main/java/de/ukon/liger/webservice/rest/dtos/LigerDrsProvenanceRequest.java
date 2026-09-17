package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;

public class LigerDrsProvenanceRequest {
    /** The LiGER structure (structureJson) the DRS was composed from. */
    public LinkedHashMap<String, Object> syntax;
    /** The GSWB semantic graph ({@code nodes}/{@code edges}) of one semantic solution. */
    public LinkedHashMap<String, Object> drs;

    public LigerDrsProvenanceRequest() {}
}
