package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;
import java.util.List;

public class LigerSourceSpanRequest {
    public LinkedHashMap<String, Object> structure;
    public List<List<Integer>> sourceIndexGroups;

    public LigerSourceSpanRequest() {}
}
