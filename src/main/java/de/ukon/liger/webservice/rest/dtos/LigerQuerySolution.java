package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;
import java.util.List;

public class LigerQuerySolution {
    public String signature;
    public LinkedHashMap<String, LinkedHashMap<String, List<String>>> bindings;

    public LigerQuerySolution(String signature, LinkedHashMap<String, LinkedHashMap<String, List<String>>> bindings) {
        this.signature = signature;
        this.bindings = bindings;
    }
}
