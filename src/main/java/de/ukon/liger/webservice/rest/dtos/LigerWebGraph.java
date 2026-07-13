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



    public LigerWebGraph(List<GraphConstraint> syntax, List<GraphConstraint> annotation)
    {
        HashMap<String,List<LigerGraphComponent>> graph = extractGraph2(syntax, annotation);
        List<LigerGraphComponent> data = new ArrayList<>();
        data.addAll(graph.get("nodes"));
        data.addAll(graph.get("edges"));
        this.graphElements = data;
    }

    public LigerWebGraph(List<GraphConstraint> syntax, List<GraphConstraint> annotation, String semantics)
    {
        HashMap<String,List<LigerGraphComponent>> graph = extractGraph2(syntax, annotation);
        List<LigerGraphComponent> data = new ArrayList<>();
        data.addAll(graph.get("nodes"));
        data.addAll(graph.get("edges"));
        this.graphElements = data;
        this.semantics = semantics;
    }

    public HashMap<String,List<LigerGraphComponent>> extractGraph2(List<GraphConstraint> syntax, List<GraphConstraint> annotation)
    {
        LinkedHashMap<String, HashMap<String,String>> nodes = new LinkedHashMap<>();
        List<LigerGraphComponent> edges = new ArrayList<>();

        addConstraints(nodes, edges, syntax, "input");
        addConstraints(nodes, edges, annotation, "annotation");

        List<LigerGraphComponent> testNodes = new ArrayList<>();

        int counter = 0;

        for (String key : nodes.keySet())
        {
            HashMap<String,String> nodeData = nodes.get(key);
            String sourceType = nodeData.getOrDefault("sourceType", "input");
            HashMap<String,String> avp = new HashMap<>(nodeData);
            avp.remove("sourceType");

            LigerWebNode lwn = null;

            if (!avp.keySet().isEmpty()) {
                lwn = new LigerWebNode(key, sourceType, key, avp);
            } else
            {
                lwn = new LigerWebNode(key,sourceType,key);
            }


            if (lwn.data.containsKey("avp")) {
            if (!((HashMap<String, String>) lwn.data.get("avp")).keySet().isEmpty()) {
                if (((HashMap<String, String>) lwn.data.get("avp")).containsKey("projection")) {
                    if (((HashMap<String, String>) lwn.data.get("avp")).get("projection").equals("c")) {
                        lwn.data.put("node_type", "cnode");
                        ((HashMap<String, String>) lwn.data.get("avp")).remove("projection");
                        counter++;
                    } else
                    if (((HashMap<String, String>) lwn.data.get("avp")).get("projection").equals("g")) {
                        lwn.data.put("node_type", "gnode");
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

    private void addConstraints(LinkedHashMap<String, HashMap<String,String>> nodes,
                                List<LigerGraphComponent> edges,
                                List<GraphConstraint> input,
                                String sourceType)
    {
        for (int i = 0; i < input.size(); i++)
        {
            int rel = 0;
            GraphConstraint g = input.get(i);
            String fsNode = g.getFsNode();

            if (!nodes.containsKey(fsNode))
            {
                nodes.put(fsNode,new HashMap<>());
                nodes.get(fsNode).put("sourceType", sourceType);
            } else if (!nodes.get(fsNode).containsKey("sourceType")) {
                nodes.get(fsNode).put("sourceType", sourceType);
            }

            if (g.getProj() != null)
            {
                nodes.get(fsNode).put("projection",g.getProj());
            }

            if ("SYN-ID".equals(g.getRelationLabel())) {
                nodes.get(fsNode).put("SYN-ID", g.getFsValue().toString());
            } else if (HelperMethods.isInteger(g.getFsValue())) {
                if (!nodes.containsKey(g.getFsValue().toString()))
                {
                    nodes.put(g.getFsValue().toString(),new HashMap<>());
                    nodes.get(g.getFsValue().toString()).put("sourceType", sourceType);
                    if (g.getProj() != null)
                    {
                        nodes.get(g.getFsValue().toString()).put("projection",g.getProj());
                    }
                } else {
                    if (g.getProj() != null &&  !nodes.get(g.getFsValue().toString()).containsKey("projection"))
                    {
                        nodes.get(g.getFsValue().toString()).put("projection",g.getProj());
                    } else if (nodes.get(g.getFsValue().toString()).containsKey("projection")) {
                        String proj = nodes.get(g.getFsValue().toString()).get("projection");
                        if ("g".equals(proj) || "g".equals(g.getProj()))
                        {
                            nodes.get(g.getFsValue().toString()).put("projection","g");
                        }
                    }
                }

                edges.add(new LigerWebEdge("rid" + g.getFsNode() + rel + g.getFsValue().toString(),
                        g.getFsNode(),g.getFsValue().toString(),
                        g.getRelationLabel(),"edge"));

            } else
            {
                nodes.get(fsNode).put(g.getRelationLabel(),g.getFsValue().toString());
            }
        }
    }

}
