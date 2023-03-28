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

package de.ukon.liger.packing;

import java.util.*;
import java.util.stream.Collectors;

public class ChoiceNode {
    public Set<Object> choiceNode;
    public Set<ChoiceVar> daughterNodes;


    public ChoiceNode(Set<Object> choiceNode, Set<ChoiceVar> daughterNodes)
    {
        this.choiceNode = choiceNode;
        this.daughterNodes = daughterNodes;
    }


    public LinkedHashMap<String,Object> toJson()
    {
        LinkedHashMap<String,Object> jsonMap = new LinkedHashMap<>();

        jsonMap.put("daughterNodes", this.daughterNodes.stream().map(ChoiceVar::toJson).collect(Collectors.toList()));

        jsonMap.put("mother",motherNodeToJson(this.choiceNode));

        return jsonMap;
    }
    public List<Object> motherNodeToJson(Set<Object> motherSet)
    {

        List<Object> motherNodes = new ArrayList<>();

        for (Object choiceSet : motherSet)
        {
            //Add a linked Hashmap describing a choicevar
            if (choiceSet instanceof ChoiceVar){
                motherNodes.add(((ChoiceVar) choiceSet).toJson());
            } else {
                List<Object> nested = motherNodeToJson((Set<Object>) choiceSet);
                motherNodes.add(nested);
            }
        }
        return motherNodes;
    }

    public static ChoiceNode parseJson(LinkedHashMap input){

        Set<Object> mother = new HashSet<>();

        mother.addAll(parseChoiceSetfromJson((List<Object>) input.get("mother")));

        Set<ChoiceVar> daughter = (Set<ChoiceVar>) ((List) input.get("daughterNodes")).stream().map(x -> ChoiceVar.parseJson((LinkedHashMap) x)).collect(Collectors.toSet());
        return new ChoiceNode(mother,daughter);
    }

    public static List<Object> parseChoiceSetfromJson(List<Object> inputList){

    List<Object> choiceList = new ArrayList<>();

    for (Object input : inputList)
    {
     if (input instanceof LinkedHashMap)
     {
         choiceList.add(ChoiceVar.parseJson((LinkedHashMap<String, String>) input));
     } else
     {
         List<Object> nested = parseChoiceSetfromJson((List<Object>) input);
         choiceList.add(nested);
     }
    }
    return choiceList;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChoiceNode choiceVar = (ChoiceNode) o;
        return Objects.equals(this.choiceNode, choiceVar.choiceNode) &&
                Objects.equals(this.daughterNodes, choiceVar.daughterNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(choiceNode, daughterNodes);
    }
}
