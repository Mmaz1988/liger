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

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;

import java.util.*;
import java.util.stream.Collectors;

public class LigerWebGraph {

    public List<LigerGraphComponent> graphElements;
    public String semantics;

    public LigerWebGraph() {
    }

    public LigerWebGraph(List<LigerGraphComponent> graphElements) {
        this.graphElements = graphElements;
    }
    public LigerWebGraph(List<LigerGraphComponent> graphElements, String semantics) {
        this.graphElements = graphElements;
        this.semantics = semantics;
    }

    public LigerWebGraph(LinguisticStructure s) {
        this(safeConstraints(s == null ? null : s.constraints), safeConstraints(s == null ? null : s.annotation));
    }

    public LigerWebGraph(List<GraphConstraint> syntax, List<GraphConstraint> annotation)
    {
        Map<String,List<LigerGraphComponent>> synMap = extractGraph2(syntax,"input");
        Map<String,List<LigerGraphComponent>> annMap = extractGraph2(annotation, "annotation");

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
        Map<String,List<LigerGraphComponent>> synMap = extractGraph2(syntax,"input");
        Map<String,List<LigerGraphComponent>> annMap = extractGraph2(annotation, "annotation");

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

    public Map<String,List<LigerGraphComponent>> extractGraph2(List<GraphConstraint> input, String type)
    {
        if (input == null) {
            Map<String, List<LigerGraphComponent>> output = new LinkedHashMap<>();
            output.put("nodes", new ArrayList<>());
            output.put("edges", new ArrayList<>());
            return output;
        }

        LinkedHashMap<String, HashMap<String,String>> nodes = new LinkedHashMap<>();
        Map<String, String> nodeTypes = new HashMap<>();
        List<LigerGraphComponent> edges = new ArrayList<>();

        for (int i = 0; i < input.size(); i++)
        {
            GraphConstraint g = input.get(i);
            String fsNode = g.getFsNode();

            if (!nodes.containsKey(fsNode))
            {
                nodes.put(fsNode,new HashMap<>());

                if (g.getProj() != null)
                {
                    nodes.get(fsNode).put("projection",g.getProj());
                }
            } else {
                if (g.getProj() != null && !nodes.get(fsNode).containsKey("projection"))
                {
                    nodes.get(fsNode).put("projection",g.getProj());
                } else if (nodes.get(fsNode).containsKey("projection")) {
                    String proj = nodes.get(fsNode).get("projection");
                    if ("g".equals(proj) || "g".equals(g.getProj()))
                    {
                        nodes.get(fsNode).put("projection","g");
                    }
                }
            }

            if (isIntegerValue(g.getFsValue())
            ) {
                if (!nodes.containsKey(g.getFsValue().toString()))
                {
                    nodes.put(g.getFsValue().toString(),new HashMap<>());
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
                edges.add(new LigerWebEdge("rid" + g.getFsNode() + g.getFsValue().toString(),
                        g.getFsNode(),g.getFsValue().toString(),
                        g.getRelationLabel(),"edge"));

            } else
            {
                nodes.get(fsNode).put(g.getRelationLabel(),g.getFsValue().toString());
            }

            if ("NODE_TYPE".equals(g.getRelationLabel())) {
                String nodeType = normalizeNodeType(g.getFsValue());
                if (nodeType != null && !nodeType.isBlank()) {
                    nodeTypes.putIfAbsent(g.getFsNode(), nodeType);
                }
            }
        }

        List<LigerGraphComponent> testNodes = new ArrayList<>();

        int counter = 0;

        for (String key : nodes.keySet())
        {
            LigerWebNode lwn = null;

            if (!nodes.get(key).keySet().isEmpty()) {
                lwn = new LigerWebNode(key, type, key, nodes.get(key));
            } else
            {
                lwn = new LigerWebNode(key,type,key);
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

            if (nodeTypes.containsKey(key)) {
                lwn.data.put("node_type", nodeTypes.get(key));
            }



            testNodes.add(lwn);

        }

        System.out.println("Modified " + counter + " nodes");

        Map<String,List<LigerGraphComponent>> output = new LinkedHashMap<>();
        output.put("nodes",testNodes);
        output.put("edges",edges);


        return output;
    }

    private static List<GraphConstraint> safeConstraints(List<GraphConstraint> constraints) {
        return constraints == null ? Collections.emptyList() : constraints;
    }

    private static boolean isIntegerValue(Object value) {
        if (value == null) {
            return false;
        }

        try {
            Integer.parseInt(String.valueOf(value));
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static String normalizeNodeType(Object value) {
        if (value == null) {
            return null;
        }

        String nodeType = String.valueOf(value).trim();
        if (nodeType.isBlank()) {
            return null;
        }

        return switch (nodeType.toLowerCase()) {
            case "c" -> "cnode";
            case "g" -> "gnode";
            case "f" -> "input";
            
            default -> nodeType.toLowerCase();
        };
    }

}
