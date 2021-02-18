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

package syntax.xle;

import packing.ChoiceSpace;
import syntax.SyntacticStructure;
import syntax.GraphConstraint;

import java.util.List;

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
}
