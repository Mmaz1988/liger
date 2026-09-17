package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

public class LigerDrsProvenanceResponse {
    public String text;
    public List<LigerSourceToken> tokens = new ArrayList<>();
    public List<LigerProvenanceCondition> conditions = new ArrayList<>();
    public List<LigerProvenanceReferent> referents = new ArrayList<>();
    public List<String> warnings = new ArrayList<>();

    public LigerDrsProvenanceResponse() {}
}
