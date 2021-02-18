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

package packing;

import java.util.Set;
import java.util.stream.Collectors;

public class ChoiceNode {
    public Set<ChoiceVar> choiceNode;
    public Set<ChoiceVar> daughterNodes;


    public ChoiceNode(Set<ChoiceVar> choiceNode, Set<ChoiceVar> daughterNodes)
    {
        this.choiceNode = choiceNode;
        this.daughterNodes = daughterNodes;
    }

    @Override
    public String toString()
    {
        String mother;
        if (choiceNode.size() == 1)
        {
            mother = choiceNode.stream().findAny().get().toString();
        } else
        {
            mother = "or(" + choiceNode.stream().map(n -> n.toString()).collect(Collectors.joining(",")) + ")";
        }

        String daughter = "[" + daughterNodes.stream().map(n -> n.toString()).collect(Collectors.joining(",")) + "]";

        return "choice(" + daughter + "," + mother + ")";

    }
}
