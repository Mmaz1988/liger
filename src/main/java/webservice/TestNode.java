package webservice;

import java.util.HashMap;
import java.util.List;

public class TestNode {
    public HashMap<String, Object> data;

    /*
                { // edge ab
                data: {id: '14', source: '1', target: '4', label: "projection",type: "proj"}
            }
     */

    public TestNode(String id, String type) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("type", type);

        HashMap<String, String> avp = new HashMap<>();
        avp.put("tense", "past");
        avp.put("aspect", "prog");

        data.put("avp", avp);

        this.data = data;

    }

    public TestNode(String id, String type, HashMap<String, String> avp)
    {
        HashMap<String,Object> data = new HashMap<>();
        data.put("id",id);
        data.put("type",type);
        data.put("avp",avp);

        this.data = data;
    }

    //Edge
    public TestNode(String id, String source, String target, String label, String type)
    {
        HashMap<String,Object> data = new HashMap<>();
        data.put("id",id);
        data.put("source",source);
        data.put("target",target);
        data.put("label",label);
        data.put("type",type);

        this.data = data;

    }
}
