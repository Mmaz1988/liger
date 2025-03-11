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

package de.ukon.liger.syntax.xle;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.GraphConstraint;

import java.util.*;
import java.util.stream.Collectors;

public class Fstructure extends LinguisticStructure {

    public boolean packed;

    public List<GraphConstraint> fstructureFacts;
    public List<GraphConstraint> cStructureFacts;


    public String prologString;

    //TODO
    //public Integer global_id

    public Fstructure(String local_id, String sentence, List<GraphConstraint> fsFacts, ChoiceSpace cp)
    {
        super(local_id,sentence,fsFacts,cp);
        if (cp.choiceNodes.size() > 0) {
            this.packed = true;
        }else{
            this.packed = false;
        }

        this.fstructureFacts = fsFacts.stream().filter(x -> x.getProj().equals("f")).collect(Collectors.toList());
        this.cStructureFacts = fsFacts.stream().filter(x -> x.getProj().equals("c")).collect(Collectors.toList());
    }

    public String writeToProlog(Boolean annotated){
        StringBuilder prologStringBuilder = new StringBuilder();

        prologStringBuilder.append("% -*- coding: utf-8 -*-\n");
        prologStringBuilder.append(System.lineSeparator());
        prologStringBuilder.append("fstructure("+this.text +",\n");
        prologStringBuilder.append("% Properties:\n");
        //Properties list can be empty
        prologStringBuilder.append("[],\n");
        //TODO Implement toString for Choices and Equivalences
        prologStringBuilder.append("% Choices:\n");
        prologStringBuilder.append("[],\n");
        prologStringBuilder.append("% Equivalences:\n");
        prologStringBuilder.append("[],\n");
        prologStringBuilder.append("% Constraints:\n[\n");


        /*
        for (GraphConstraint g : this.constraints) {
            if (!g.getFsNode().equals("-1")) {
                prologStringBuilder.append(g.toPrologString() + ",\n");
            }
        }
*/

        List<GraphConstraint> factList = constraints;

        if (annotated)
        {
            factList = new ArrayList<>();
            factList.addAll(constraints);
            factList.addAll(annotation);
        }



      prologStringBuilder.append(factList.stream().filter(x -> !x.getFsNode().equals("-1")).map(x -> x.toPrologString()).collect(Collectors.joining(",\n")));

        prologStringBuilder.append("\n],\n");
        prologStringBuilder.append("% C-structure:\n");
        prologStringBuilder.append("[]).\n");


        return prologStringBuilder.toString();
    }

    // String root = this.cStructureFacts.stream().filter(GraphConstraint::isRoot).map(GraphConstraint::getFsNode).findFirst().get();
    public LinkedHashMap<String, Object> builtCstructureTree(String root) {
        LinkedHashMap<String, Object> cStructureTree = new LinkedHashMap<>();

        List<GraphConstraint> currentConstraints = this.cStructureFacts.stream().filter(x -> x.getFsNode().equals(root)).collect(Collectors.toList());

        Set<GraphConstraint> leftSet = currentConstraints.stream().filter(c1 -> c1.getFsNode().equals(root) && c1.getRelationLabel().equals("left")).collect(Collectors.toSet());
        Set<GraphConstraint> rightSet = currentConstraints.stream().filter(c1 -> c1.getFsNode().equals(root) && c1.getRelationLabel().equals("right")).collect(Collectors.toSet());

        String left = null;
        if (!leftSet.isEmpty()) {
            left = leftSet.stream().map(GraphConstraint::getFsValue).findFirst().get().toString();
        }

        String right = null;
        if (!rightSet.isEmpty())
        {
            right = rightSet.stream().map(GraphConstraint::getFsValue).findFirst().get().toString();
        }

        if (left == null && right == null)
        {
            cStructureTree.put(root,null);
        } else {

            Object[] daughters = new Object[2];

            if (left != null) {
                LinkedHashMap<String, Object> leftTree = builtCstructureTree(left);
                if (leftTree != null) {
                    daughters[0] = leftTree;
                }
            }
            if (right != null) {
                LinkedHashMap<String, Object> rightTree = builtCstructureTree(right);
                if (rightTree != null) {
                    daughters[1] = rightTree;
                }
            }
            cStructureTree.put(root,daughters);
        }

        return cStructureTree;
    }



}
