package de.ukon.liger.webservice.rest.dtos;

import java.util.HashMap;

public class LigerWebEdge extends LigerGraphComponent {
    //Edge
    public LigerWebEdge(String id, String source, String target, String label, String type)
    {
        HashMap<String,Object> data = new HashMap<>();
        data.put("id",id);
        data.put("source",source);
        data.put("target",target);
        data.put("label",label);
        data.put("edge_type",type);

        this.data = data;

    }

}
