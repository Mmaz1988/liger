package de.ukon.liger.webservice.rest.dtos;

import java.util.HashMap;

public class LigerGraphComponent {

    public HashMap<String, Object> data;

    public LigerGraphComponent(){}

    public LigerGraphComponent(HashMap<String,Object> data)
    {
        this.data = data;
    }
}
