package webservice;

import syntax.SyntacticStructure;
import syntax.GraphConstraint;
import utilities.HelperMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class TestGraph {

    public List<TestNode> graphElements;
    public String semantics;

    public TestGraph(List<TestNode> graphElements) {
        this.graphElements = graphElements;
    }

    public TestGraph(List<TestNode> graphElements, String semantics) {
        this.graphElements = graphElements;
        this.semantics = semantics;
    }


    public TestGraph(SyntacticStructure s)
    {
        this.graphElements = extractGraph(s);
    }

    public TestGraph(List<GraphConstraint> syntax, List<GraphConstraint> annotation)
    {
        HashMap<String,List<TestNode>> synMap = extractGraph2(syntax,"input");
        HashMap<String,List<TestNode>> annMap = extractGraph2(annotation, "annotation");

        List<TestNode> nodes = new ArrayList<>();
        nodes.addAll(synMap.get("nodes"));
        nodes.addAll(annMap.get("nodes"));
        List<TestNode> edges = new ArrayList<>();
        nodes.addAll(synMap.get("edges"));
        nodes.addAll(annMap.get("edges"));

        List<TestNode> data = new ArrayList<>();
        data.addAll(nodes);
        data.addAll(edges);
        this.graphElements = data;
    }

    public TestGraph(List<GraphConstraint> syntax, List<GraphConstraint> annotation, String semantics)
    {
        HashMap<String,List<TestNode>> synMap = extractGraph2(syntax,"input");
        HashMap<String,List<TestNode>> annMap = extractGraph2(annotation, "annotation");

        List<TestNode> nodes = new ArrayList<>();
        nodes.addAll(synMap.get("nodes"));
        nodes.addAll(annMap.get("nodes"));
        List<TestNode> edges = new ArrayList<>();
        nodes.addAll(synMap.get("edges"));
        nodes.addAll(annMap.get("edges"));

        List<TestNode> data = new ArrayList<>();
        data.addAll(nodes);
        data.addAll(edges);
        this.graphElements = data;
        this.semantics = semantics;
    }

    public List<TestNode> extractGraph(SyntacticStructure s)
    {
        LinkedHashMap<Integer, HashMap<String,String>> nodes = new LinkedHashMap<>();
        List<TestNode> edges = new ArrayList<>();

        for (int i = 0; i < s.constraints.size(); i++)
        {
            int rel = 0;
            GraphConstraint g = s.constraints.get(i);
            Integer fsNode = Integer.parseInt(g.getFsNode());

            if (!nodes.containsKey(fsNode))
            {
                nodes.put(fsNode,new HashMap<>());
            }

            if (HelperMethods.isInteger(g.getFsValue())
            ) {
                if (!nodes.containsKey(fsNode))
                {
                    nodes.put(fsNode,new HashMap<>());
                }
                //TestNode(String id, String source, String target, String label, String type)
                edges.add(new TestNode("rid" + g.getFsNode() + rel + g.getFsValue().toString(),
                        g.getFsNode(),g.getFsValue().toString(),
                        g.getRelationLabel(),"edge"));

            } else
            {
                nodes.get(fsNode).put(g.getRelationLabel(),g.getFsValue().toString());
            }

        }

        List<TestNode> testNodes = new ArrayList<>();

        for (Integer key : nodes.keySet())
        {
            if (!nodes.get(key).keySet().isEmpty()) {
                testNodes.add(new TestNode(key.toString(), "input", nodes.get(key)));
            } else
            {
                testNodes.add(new TestNode(key.toString(),"input"));
            }
        }

        List<TestNode> output = new ArrayList<>();
        output.addAll(testNodes);
        output.addAll(edges);
        return output;
    }


    public HashMap<String,List<TestNode>> extractGraph2(List<GraphConstraint> input,String type)
    {
        LinkedHashMap<Integer, HashMap<String,String>> nodes = new LinkedHashMap<>();
        List<TestNode> edges = new ArrayList<>();

        for (int i = 0; i < input.size(); i++)
        {
            int rel = 0;
            GraphConstraint g = input.get(i);
            Integer fsNode = Integer.parseInt(g.getFsNode());

            if (!nodes.containsKey(fsNode))
            {
                nodes.put(fsNode,new HashMap<>());
            }

            if (HelperMethods.isInteger(g.getFsValue())
            ) {
                if (!nodes.containsKey(Integer.parseInt(g.getFsValue().toString())))
                {
                    nodes.put(Integer.parseInt(g.getFsValue().toString()),new HashMap<>());
                }
                //TestNode(String id, String source, String target, String label, String type)
                edges.add(new TestNode("rid" + g.getFsNode() + rel + g.getFsValue().toString(),
                        g.getFsNode(),g.getFsValue().toString(),
                        g.getRelationLabel(),"edge"));

            } else
            {
                nodes.get(fsNode).put(g.getRelationLabel(),g.getFsValue().toString());
            }

        }

        List<TestNode> testNodes = new ArrayList<>();

        for (Integer key : nodes.keySet())
        {
            if (!nodes.get(key).keySet().isEmpty()) {
                testNodes.add(new TestNode(key.toString(), type, nodes.get(key)));
            } else
            {
                testNodes.add(new TestNode(key.toString(),type));
            }
        }

        HashMap<String,List<TestNode>> output = new HashMap<>();
        output.put("nodes",testNodes);
        output.put("edges",edges);


        return output;
    }

}
