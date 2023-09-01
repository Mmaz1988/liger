/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

package de.ukon.liger.webservice.rest.dtos;

import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

import java.util.*;
import java.util.stream.Collectors;

public class LigerWebGraph {

    public List<LigerGraphComponent> graphElements;
    public String semantics;
    public LigerWebGraph(List<LigerGraphComponent> graphElements) {
        this.graphElements = graphElements;
    }
    public LigerWebGraph(List<LigerGraphComponent> graphElements, String semantics) {
        this.graphElements = graphElements;
        this.semantics = semantics;
    }


    public LigerWebGraph(LinguisticStructure s)
    {
        this.graphElements = extractGraph(s);
    }

    public LigerWebGraph(List<GraphConstraint> syntax, List<GraphConstraint> annotation)
    {
        HashMap<String,List<LigerGraphComponent>> synMap = extractGraph2(syntax,"input");
        HashMap<String,List<LigerGraphComponent>> annMap = extractGraph2(annotation, "annotation");

        List<LigerGraphComponent> nodes = new ArrayList<>();
        nodes.addAll(synMap.get("nodes"));

        HashSet<Object> ids = nodes.stream().map(p ->  p.data.get("id")).collect(Collectors.toCollection(HashSet::new));

        nodes.addAll(annMap.get("nodes").stream().filter(p -> !ids.contains(p.data.get("id"))).collect(Collectors.toList()));
        List<LigerGraphComponent> edges = new ArrayList<>();
        edges.addAll(synMap.get("edges"));
        edges.addAll(annMap.get("edges"));

        List<LigerGraphComponent> data = new ArrayList<>();
        data.addAll(nodes);
        data.addAll(edges);
        this.graphElements = data;
    }

    public LigerWebGraph(List<GraphConstraint> syntax, List<GraphConstraint> annotation, String semantics)
    {
        HashMap<String,List<LigerGraphComponent>> synMap = extractGraph2(syntax,"input");
        HashMap<String,List<LigerGraphComponent>> annMap = extractGraph2(annotation, "annotation");

        List<LigerGraphComponent> nodes = new ArrayList<>();
        nodes.addAll(synMap.get("nodes"));

        HashSet<Object> ids = nodes.stream().map(p ->  p.data.get("id")).collect(Collectors.toCollection(HashSet::new));

        nodes.addAll(annMap.get("nodes").stream().filter(p -> !ids.contains(p.data.get("id"))).collect(Collectors.toList()));
        List<LigerGraphComponent> edges = new ArrayList<>();
        edges.addAll(synMap.get("edges"));
        edges.addAll(annMap.get("edges"));

        List<LigerGraphComponent> data = new ArrayList<>();
        data.addAll(nodes);
        data.addAll(edges);
        this.graphElements = data;
        this.semantics = semantics;
    }

    public List<LigerGraphComponent> extractGraph(LinguisticStructure s)
    {
        LinkedHashMap<Integer, HashMap<String,String>> nodes = new LinkedHashMap<>();
        List<LigerWebEdge> edges = new ArrayList<>();

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
                edges.add(new LigerWebEdge("rid" + g.getFsNode() + rel + g.getFsValue().toString(),
                        g.getFsNode(),g.getFsValue().toString(),
                        g.getRelationLabel(),"edge"));

            } else
            {
                nodes.get(fsNode).put(g.getRelationLabel(),g.getFsValue().toString());
            }

        }

        List<LigerWebNode> testNodes = new ArrayList<>();

        for (Integer key : nodes.keySet())
        {
            if (!nodes.get(key).keySet().isEmpty()) {
                testNodes.add(new LigerWebNode(key.toString(), "input", nodes.get(key)));
            } else
            {
                testNodes.add(new LigerWebNode(key.toString(),"input"));
            }
        }

        List<LigerGraphComponent> output = new ArrayList<>();
        output.addAll(testNodes);
        output.addAll(edges);
        return output;
    }


    public HashMap<String,List<LigerGraphComponent>> extractGraph2(List<GraphConstraint> input, String type)
    {
        LinkedHashMap<String, HashMap<String,String>> nodes = new LinkedHashMap<>();
        List<LigerGraphComponent> edges = new ArrayList<>();

        List<GraphConstraint> cstr = input.stream().filter(x -> x.getProj() != null && x.getProj().equals("c")).collect(Collectors.toList());

        for (int i = 0; i < input.size(); i++)
        {
            int rel = 0;
            GraphConstraint g = input.get(i);
            String fsNode = g.getFsNode();

            if (!nodes.containsKey(fsNode))
            {
                nodes.put(fsNode,new HashMap<>());

                if (g.getProj() != null && g.getProj().equals("c"))
                {
                    nodes.get(fsNode).put("projection",g.getProj());
                }
            } else {
                if (g.getProj() != null && g.getProj().equals("c") && !nodes.get(fsNode).containsKey("projection"))
                {
                    nodes.get(fsNode).put("projection",g.getProj());
                }
            }

            if (HelperMethods.isInteger(g.getFsValue())
            ) {
                if (!nodes.containsKey(g.getFsValue().toString()))
                {
                    nodes.put(g.getFsValue().toString(),new HashMap<>());
                }
                //TestNode(String id, String source, String target, String label, String type)
                edges.add(new LigerWebEdge("rid" + g.getFsNode() + rel + g.getFsValue().toString(),
                        g.getFsNode(),g.getFsValue().toString(),
                        g.getRelationLabel(),"edge"));

            } else
            {
                nodes.get(fsNode).put(g.getRelationLabel(),g.getFsValue().toString());
            }

        }

        List<LigerGraphComponent> testNodes = new ArrayList<>();

        int counter = 0;

        for (String key : nodes.keySet())
        {
            LigerWebNode lwn = null;

            if (!nodes.get(key).keySet().isEmpty()) {
                lwn = new LigerWebNode(key, type, nodes.get(key));
            } else
            {
                lwn = new LigerWebNode(key,type);
            }


            if (lwn.data.containsKey("avp")) {
            if (!((HashMap<String, String>) lwn.data.get("avp")).keySet().isEmpty()) {
                if (((HashMap<String, String>) lwn.data.get("avp")).containsKey("projection")) {
                    if (((HashMap<String, String>) lwn.data.get("avp")).get("projection").equals("c")) {
                        lwn.data.put("node_type", "cnode");
                        ((HashMap<String, String>) lwn.data.get("avp")).remove("projection");
                        counter++;
                    }
                }
            }
        }



            testNodes.add(lwn);

        }

        System.out.println("Modified " + counter + " nodes");

        HashMap<String,List<LigerGraphComponent>> output = new HashMap<>();
        output.put("nodes",testNodes);
        output.put("edges",edges);


        return output;
    }

}
