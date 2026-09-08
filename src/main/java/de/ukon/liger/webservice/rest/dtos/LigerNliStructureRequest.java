package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;

public class LigerNliStructureRequest {
    public String id;
    public LinkedHashMap<String, Object> premise;
    public LinkedHashMap<String, Object> conclusion;
}
