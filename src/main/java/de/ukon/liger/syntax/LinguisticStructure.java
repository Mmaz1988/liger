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
import de.ukon.liger.utilities.HelperMethods;

import java.util.*;
import java.util.stream.Collectors;

public class LinguisticStructure {

    public String local_id;
    public String text;
    public List<GraphConstraint> constraints;
    public ChoiceSpace cp;


    //TODO move annotation to general LigerAnnotation structure
    public List<GraphConstraint> annotation = new ArrayList<>();


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



    public LinguisticStructure(String local_id, String sentence, List<GraphConstraint> fsFacts)
    {
        this.local_id = local_id;
        this.text = sentence;
        this.constraints = fsFacts;
    }

    public LinguisticStructure(String local_id, String sentence, List<GraphConstraint> fsFacts, ChoiceSpace cp)
    {
        this.local_id = local_id;
        this.text = sentence;
        this.constraints = fsFacts;
        this.cp = cp;
    }

    public List<List<GraphConstraint>> getSubstructures(String name)
    {
        List<String> topNodes = new ArrayList<>();
        for (GraphConstraint g : annotation)
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

            for (GraphConstraint g: annotation)
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
                    for (GraphConstraint g1 : annotation)
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


}
