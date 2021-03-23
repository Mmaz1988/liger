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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ChoiceSpace {

    public static Pattern choicePattern = Pattern.compile("\\[(.+)\\],(.+)");
    public static Pattern orPattern = Pattern.compile("or\\((.+)\\)");

    public Set<ChoiceVar> rootChoice = new HashSet<ChoiceVar>(Collections.singleton(new ChoiceVar("1")));
    public List<ChoiceNode> choiceNodes;
    public Set<Set<ChoiceVar>> choices = new HashSet<>();


    public ChoiceSpace(List<String> choices)
    {
       choiceNodes = parseChoiceSpace(choices);
    }
    public  ChoiceSpace() {}


    public static Set<ChoiceVar> parseChoice(String choice)
    {

        Matcher orMatcher = orPattern.matcher(choice);

        Set<ChoiceVar> context;

        if (orMatcher.find())
        {
            context = Arrays.asList(orMatcher.group(1).split(",")).stream().map(n -> new ChoiceVar(n)).collect(Collectors.toSet());
        }
        else
        {
            context = Collections.singleton(new ChoiceVar(choice));
        }

        return context;
    }

    public List<ChoiceNode> parseChoiceSpace(List<String> choices)
    {
        List<ChoiceNode> choiceNodes = new ArrayList<>();
        for (String choice : choices)
        {
            Matcher choiceMatcher  = choicePattern.matcher(choice);

            List<String> daughterNodes = null;
            Set<ChoiceVar> daughter = null;

            if (choiceMatcher.find())
            {
                daughterNodes = Arrays.asList(choiceMatcher.group(1).split(","));
            }

            daughter = daughterNodes.stream().map(n -> new ChoiceVar(n)).collect(Collectors.toSet());

            Matcher orMatcher = orPattern.matcher(choiceMatcher.group(2));

            Set<ChoiceVar> mother;

            if (orMatcher.find())
            {
                mother = Arrays.asList(orMatcher.group(1).split(",")).stream().map(n -> new ChoiceVar(n)).collect(Collectors.toSet());
            }
            else
            {
                mother = Collections.singleton(new ChoiceVar(choiceMatcher.group(2).trim()));
            }

            ChoiceNode choiceNode = new ChoiceNode(mother,daughter);
            choiceNodes.add(choiceNode);
        }
    return choiceNodes;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (ChoiceNode cn : choiceNodes)
        {
            sb.append(cn.toString());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
