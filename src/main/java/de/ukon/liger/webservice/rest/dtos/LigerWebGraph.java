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
        List<LigerGraphComponent> edges = new ArrayList<>();
        Map<String, String> nodeTypes = new LinkedHashMap<>();
        Map<String, String> projectedNodeTypes = new LinkedHashMap<>();

        for (int i = 0; i < input.size(); i++)
        {
            GraphConstraint g = input.get(i);
            String fsNode = g.getFsNode();

            if (fsNode == null || fsNode.isBlank()) {
                continue;
            }

            nodes.putIfAbsent(fsNode, new HashMap<>());

            if ("NODE_TYPE".equals(g.getRelationLabel())) {
                String nodeType = normalizeNodeType(g.getFsValue());
                if (nodeType != null && !nodeType.isBlank()) {
                    nodeTypes.put(fsNode, nodeType);
                }
                continue;
            }

            if (g.getProj() != null) {
                nodes.get(fsNode).put("projection", g.getProj());
                String projectedNodeType = normalizeNodeType(g.getProj());
                if (projectedNodeType != null) {
                    projectedNodeTypes.merge(fsNode, projectedNodeType, LigerWebGraph::preferNodeType);
                }
            }

            if ("SYN-ID".equals(g.getRelationLabel())) {
                nodes.get(fsNode).put("SYN-ID", g.getFsValue().toString());

            } else if (isIntegerValue(g.getFsValue())
            ) {
                String target = g.getFsValue().toString();
                nodes.putIfAbsent(target, new HashMap<>());
                if (g.getProj() != null && !nodes.get(target).containsKey("projection")) {
                    nodes.get(target).put("projection", g.getProj());
                }
                edges.add(new LigerWebEdge("rid" + g.getFsNode() + g.getFsValue().toString(),
                        g.getFsNode(),g.getFsValue().toString(),
                        g.getRelationLabel(),"edge"));

            } else
            {
                nodes.get(fsNode).put(g.getRelationLabel(),g.getFsValue().toString());
            }

        }

        List<LigerGraphComponent> testNodes = new ArrayList<>();

        for (String key : nodes.keySet())
        {
            HashMap<String, String> avp = nodes.get(key);
            String resolvedNodeType = nodeTypes.get(key);
            if (resolvedNodeType == null) {
                resolvedNodeType = projectedNodeTypes.get(key);
            }
            if (resolvedNodeType == null) {
                resolvedNodeType = type;
            }

            if (!avp.keySet().isEmpty()) {
                testNodes.add(new LigerWebNode(key, resolvedNodeType, key, avp));
            } else {
                testNodes.add(new LigerWebNode(key, resolvedNodeType, key));
            }

        }

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

        return switch (nodeType.toLowerCase(Locale.ROOT)) {
            case "c" -> "cnode";
            case "g" -> "gnode";
            case "f" -> "input";
            default -> nodeType.toLowerCase(Locale.ROOT);
        };
    }

    private static String preferNodeType(String existing, String candidate) {
        if (existing == null || existing.isBlank()) {
            return candidate;
        }
        if (candidate == null || candidate.isBlank()) {
            return existing;
        }
        if ("gnode".equals(existing) || "gnode".equals(candidate)) {
            return "gnode";
        }
        if ("input".equals(existing) || "input".equals(candidate)) {
            return "input";
        }
        if ("cnode".equals(existing) || "cnode".equals(candidate)) {
            return "cnode";
        }
        return existing;
    }

}
