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

package de.ukon.liger.syntax;


import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.utilities.HelperMethods;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinguisticStructure {

    @JsonAlias("id")
    public String local_id;
    public String text;
    public List<GraphConstraint> constraints;
    @JsonAlias("choiceSpace")
    public ChoiceSpace cp;

    @JsonAlias("annotations")
    public List<GraphConstraint> annotation = new ArrayList<>();




    public LinguisticStructure()
    {}

    public LinguisticStructure(String local_id, String sentence, List<GraphConstraint> fsFacts)
    {
        this.local_id = local_id;
        this.text = sentence;
        this.constraints = fsFacts;
        deduplicateEdges();
    }

    public LinguisticStructure(String local_id, String sentence, List<GraphConstraint> fsFacts, ChoiceSpace cp)
    {
        this.local_id = local_id;
        this.text = sentence;
        this.constraints = fsFacts;
        this.cp = cp;
        deduplicateEdges();
    }

    public LinguisticStructure(LinguisticStructure other) {
        if (other == null) {
            this.constraints = new ArrayList<>();
            this.annotation = new ArrayList<>();
            this.cp = new ChoiceSpace();
            return;
        }
        this.local_id = other.local_id;
        this.text = other.text;
        this.constraints = other.constraints == null ? new ArrayList<>() : other.constraints.stream()
                .map(GraphConstraint::copy)
                .collect(Collectors.toList());
        this.annotation = other.annotation == null ? new ArrayList<>() : other.annotation.stream()
                .map(GraphConstraint::copy)
                .collect(Collectors.toList());
        this.cp = other.cp == null ? new ChoiceSpace() : other.cp.copy();
        deduplicateEdges();
    }

    public LinguisticStructure copy() {
        return new LinguisticStructure(this);
    }

    public List<GraphConstraint> returnFullGraph(){
        deduplicateEdges();
        List<GraphConstraint> allConstraints = new ArrayList<>();
        allConstraints.addAll(this.constraints);
        allConstraints.addAll(this.annotation);
        return allConstraints;
    }

    public void deduplicateEdges() {
        deduplicateEdges(constraints, annotation);
    }

    public static void deduplicateEdges(List<GraphConstraint> constraints,
                                        List<GraphConstraint> annotation) {
        Set<List<Object>> seen = new HashSet<>();
        deduplicateEdges(constraints, seen);
        deduplicateEdges(annotation, seen);
    }

    private static void deduplicateEdges(List<GraphConstraint> constraints,
                                         Set<List<Object>> seen) {
        if (constraints == null) {
            return;
        }

        constraints.removeIf(constraint -> {
            if (constraint == null || !NodeIdPolicy.legacyCompatibleDefaults()
                    .isNodeReference(String.valueOf(constraint.getFsValue()))) {
                return false;
            }
            return !seen.add(Arrays.asList(
                    constraint.getFsNode(),
                    constraint.getFsValue(),
                    constraint.getRelationLabel()));
        });
    }


    public LinkedHashMap<String,Object> toJson()
    {
        LinkedHashMap<String,Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("id",this.local_id);
        jsonMap.put("text",this.text);

        List<LinkedHashMap> jsonConstraints = this.constraints.stream()
                .map(GraphConstraint::toJson).collect(Collectors.toList());
        jsonMap.put("constraints",jsonConstraints);

        List<LinkedHashMap> jsonAnnotations = this.annotation.stream()
                .map(GraphConstraint::toJson).collect(Collectors.toList());
        jsonMap.put("annotations",jsonAnnotations);

        jsonMap.put("choiceSpace",this.cp.toJson());

        return jsonMap;
    }

    public static LinguisticStructure parseFromJson(LinkedHashMap input)
    {
        LinguisticStructure ls = new LinguisticStructure();

        ls.local_id = (String) input.get("id");
        ls.text = (String) input.get("text");
        if (input.get("nodes") instanceof List<?> && input.get("edges") instanceof List<?>) {
            ls.constraints = parseCanonicalGraph(input);
        } else {
            ls.constraints = input.get("constraints") instanceof List<?> rawConstraints
                    ? (List<GraphConstraint>) rawConstraints.stream()
                    .map(x -> GraphConstraint.parseJson((LinkedHashMap) x)).collect(Collectors.toList())
                    : new ArrayList<>();
        }

        ls.annotation = input.get("annotations") instanceof List<?> rawAnnotations
                ? (List<GraphConstraint>) rawAnnotations.stream()
                .map(x -> GraphConstraint.parseJson((LinkedHashMap) x)).collect(Collectors.toList())
                : new ArrayList<>();


        if (!((LinkedHashMap) input.get("choiceSpace")).isEmpty()) {
            ls.cp = ChoiceSpace.parseJson((LinkedHashMap<String, Object>) input.get("choiceSpace"));
        }
        else {
            ls.cp = new ChoiceSpace();
        }

        ls.deduplicateEdges();

        return ls;
    }

    private static List<GraphConstraint> parseCanonicalGraph(LinkedHashMap input) {
        List<GraphConstraint> constraints = new ArrayList<>();
        for (Object rawNode : (List<?>) input.get("nodes")) {
            if (!(rawNode instanceof Map<?, ?> node) || node.get("id") == null) {
                continue;
            }
            String nodeId = String.valueOf(node.get("id"));
            addCanonicalAttribute(constraints, nodeId, "NODE_TYPE", node.get("node_type"));
            if (node.get("avp") instanceof Map<?, ?> avp) {
                for (Map.Entry<?, ?> attribute : avp.entrySet()) {
                    String name = String.valueOf(attribute.getKey());
                    if (!"NODE_TYPE".equals(name)) {
                        addCanonicalAttribute(constraints, nodeId, name, attribute.getValue());
                    }
                }
            }
        }
        for (Object rawEdge : (List<?>) input.get("edges")) {
            if (!(rawEdge instanceof Map<?, ?> edge)
                    || edge.get("source") == null || edge.get("target") == null || edge.get("label") == null) {
                continue;
            }
            String source = String.valueOf(edge.get("source"));
            String label = String.valueOf(edge.get("label"));
            String target = String.valueOf(edge.get("target"));
            constraints.add(new GraphConstraint(
                    Set.of(new ChoiceVar("1")),
                    source,
                    label,
                    target,
                    null,
                    false));
        }
        return constraints;
    }

    private static void addCanonicalAttribute(List<GraphConstraint> constraints,
                                              String nodeId, String name, Object value) {
        if (value == null) {
            return;
        }
        constraints.add(new GraphConstraint(
                Set.of(new ChoiceVar("1")), nodeId, name, String.valueOf(value), null, false));
    }
/*

    //TODO fix for cyclic structures
    public List<List<GraphConstraint>> getSubstructures(String name)
    {
        List<GraphConstraint> allConstraints = this.returnFullGraph();

        List<String> topNodes = new ArrayList<>();
        for (GraphConstraint g : allConstraints)
        {
            if (g.getRelationLabel().equals(name))
            {
                topNodes.add((String) g.getFsValue());
            }
        }

        List<List<GraphConstraint>> out = new ArrayList<>();

        for (String node : topNodes)
        {
            List<GraphConstraint> matrix = new ArrayList<>();
            Set<String> daugtherNodes = new HashSet<>();

            for (GraphConstraint g: allConstraints)
            {
                if (g.getFsNode().equals(node))
                {

                    if (HelperMethods.isInteger(g.getFsValue())) {
                        daugtherNodes.add((String) g.getFsValue());
                    }
                    matrix.add(g);
                }

                while (!daugtherNodes.isEmpty())
                {
                    Set<String> helperList = new HashSet<>();
                    for (GraphConstraint g1 : allConstraints)
                    {
                        if (daugtherNodes.contains(g1.getFsNode()))
                        {
                            matrix.add(g1);
                            if (HelperMethods.isInteger(g1.getFsValue()))
                            {
                                helperList.add((String) g1.getFsValue());
                            }
                        }
                    }
                    daugtherNodes = helperList;

                }

            }

            out.add(matrix);
        }

        return out;

    }

*/


}
