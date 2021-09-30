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

import edu.stanford.nlp.ling.CoreAnnotations;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ChoiceSpace {

    public static Pattern choicePattern = Pattern.compile("\\[(.+)\\],(.+)");
    public static Pattern orPattern = Pattern.compile("or\\((.+)\\)");
    public static String inputString;

    public Set<ChoiceVar> rootChoice = new HashSet<ChoiceVar>(Collections.singleton(new ChoiceVar("1")));
    public List<ChoiceNode> choiceNodes;
    public Set<Set<ChoiceVar>> choices = new HashSet<>();


    public ChoiceSpace(List<String> choices)
    {
       choiceNodes = parseChoiceSpace(choices);
    }
    public  ChoiceSpace() {}
    public List<String> allVariables = new ArrayList<>();
    private static String[] choiceArray = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};


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
            //System.out.println("Input "+choice);
            Matcher choiceMatcher  = choicePattern.matcher(choice);

            List<String> daughterNodes = null;
            Set<ChoiceVar> daughter = null;

            if (choiceMatcher.find())
            {
                daughterNodes = Arrays.asList(choiceMatcher.group(1).split(","));
            }

            daughter = daughterNodes.stream().map(n -> new ChoiceVar(n)).collect(Collectors.toSet());
            //System.out.println("Test1" + daughter);
            ChoiceVar variable = daughter.iterator().next();
            String variableFinal = variable.toString().replaceAll("[0-9]", "");
            //System.out.println(variableFinal);
            allVariables.add(variableFinal);

            Matcher orMatcher = orPattern.matcher(choiceMatcher.group(2));
            Set<Object> inputSet;
            Set<Object> mother = new HashSet<>();
//Create mother node
            if (orMatcher.find())
            {
                List<String> input = Collections.singletonList(choiceMatcher.group(2));
                inputString = input.get(0).strip();
                inputString = inputString.substring(3,inputString.length());
                //System.out.println(inputString);
                mother = buildOrMother(mother);
                //System.out.println("Mother: "+mother);
                //System.out.println(mother.iterator().next().getClass());
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

    private Set<Object> buildOrMother(Set<Object> mother){
        Set<Object> temp = new HashSet<>();
        boolean parse = true;

        while (parse){
            if (inputString.charAt(0) == 'o'){
                inputString=inputString.substring(3,inputString.length());
                temp.clear();
                temp= buildOrMother(temp);
                mother.add(Stream.of(temp).map(n -> new HashSet(n) {}).collect(Collectors.toSet()).iterator().next());
            }
            int comma = inputString.indexOf(",");
            int par = inputString.indexOf(")");
            if ((comma<par)&&(comma!=-1)){
                String variable = inputString.substring(0, comma);
                if(variable.length()>1){
                    mother.add(Arrays.asList(variable).stream().map(n -> new ChoiceVar(n)).collect(Collectors.toSet()).iterator().next());}
                inputString = inputString.substring(comma + 1, inputString.length());

            }
            else{
                String variable = inputString.substring(0, par);
                if(variable.length()>1){
                    mother.add(Stream.of(variable).map(n -> new ChoiceVar(n)).collect(Collectors.toSet()).iterator().next());}
                inputString = inputString.substring(par + 1, inputString.length());
                parse = false;

            }
        }
        return mother;
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

    public Set<ChoiceVar> returnNewChoiceVars(Integer numberOfChoices)
    {
        String choice;
        String previousVar = lastVar();
        // Tested by entering random variable below to see if it finds the next one :)
        //String previousVar = "BDFKIZ";
        //System.out.println(previousVar);
        int index = previousVar.length()-1;
        //System.out.println(index);
        boolean goOn = true;
        StringBuilder sb = new StringBuilder();

        while (index >= 0 && goOn) {
            String currentChar = String.valueOf(previousVar.charAt(index));
            if (!currentChar.equals("Z")){
                for (int i = 0; i < choiceArray.length; i++) {
                    if (currentChar.equals(choiceArray[i])) {
                        sb.append(choiceArray[i + 1]);
                        goOn = false;
                    }
                }
            }
            else{
                sb.append("A");
                index--;

            }

        }
        sb.reverse();

        if (goOn){
            choice = "A"+sb.toString();
        }
        else{
            choice = previousVar.substring(0, index)+sb.toString();
        }
        HashSet<ChoiceVar> result = new HashSet<>();
        for (int i = 0; i < numberOfChoices; i++)
        {
            int j = i+1;
            result.add(new ChoiceVar(choice + j));
        }
        allVariables.add(choice);
        return result;
    }

    public String lastVar(){
        int index = allVariables.size()-1;
        return allVariables.get(index);
    }
}
