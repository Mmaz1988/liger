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

package de.ukon.liger.webservice;

import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

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


    public TestGraph(LinguisticStructure s)
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

    public List<TestNode> extractGraph(LinguisticStructure s)
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
