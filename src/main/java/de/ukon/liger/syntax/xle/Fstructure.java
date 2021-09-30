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
import de.ukon.liger.syntax.SyntacticStructure;
import de.ukon.liger.syntax.GraphConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Fstructure extends SyntacticStructure {

    public boolean packed;


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
    }

    public String writeToProlog(Boolean annotated){
        StringBuilder prologStringBuilder = new StringBuilder();

        prologStringBuilder.append("% -*- coding: utf-8 -*-\n");
        prologStringBuilder.append(System.lineSeparator());
        prologStringBuilder.append("fstructure("+this.sentence +",\n");
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



}
