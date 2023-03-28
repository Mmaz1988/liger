package de.ukon.liger.analysis.graphParser.queryExpressions;

import de.ukon.liger.analysis.graphParser.LigerGraph.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Attribute extends QueryExpression{

    public Map<String, List<LigerObject>> attributes;
    public Attribute(String name, LigerGraph lg) {
        super(name,lg);
        listAttributesAndRelations();



    }

    public void listAttributesAndRelations()
    {
        List<LigerObject> attributes = new ArrayList<>();
        for (LigerNode node : lg.nodes)
        {
            List<LigerAVP> nodeAVPs = (List<LigerAVP>) node.get("avps");
            attributes.addAll(nodeAVPs.stream()
                    .filter(x -> x.attribute.equals(this.name))
                    .collect(Collectors.toList()));
        }

        List<LigerObject> relations = new ArrayList<>(lg.edges.stream()
                .filter(x -> x.get("label").equals(this.name))
                .collect(Collectors.toList()));

        Map<String,List<LigerObject>> attrs = new HashMap<>();

        attrs.put("attributes",attributes);
        attrs.put("relations",relations);

        this.attributes = attrs;
    }


}
