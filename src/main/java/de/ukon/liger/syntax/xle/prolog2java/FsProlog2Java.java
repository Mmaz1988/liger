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

package de.ukon.liger.syntax.xle.prolog2java;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.xle.avp_elements.*;
import de.ukon.liger.utilities.HelperMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// This Object contains a linked hash map f-structure (following Brubeck Unhammer)

public class FsProlog2Java {


    public static Pattern ambiguities = Pattern.compile("cf\\((.+?),\\w+\\(");
    public static Pattern keys = Pattern.compile("var\\((\\d+)\\)");
    public static Pattern preds = Pattern.compile("attr\\(var\\((\\d+)\\),'(PRED)'\\),(semform\\(.*\\[.*\\],\\[.*\\]\\))");
    public static Pattern adjuncts = Pattern.compile("attr\\(var\\((\\d+)\\),'ADJUNCT'\\),var\\((\\d+)\\)");
    public static Pattern inSet = Pattern.compile("in_set\\((.*),var\\((\\d+)\\)\\)");
    public static Pattern subsume = Pattern.compile("subsume\\((.*),var\\((\\d+)\\)\\)");
    public static Pattern nonTerminals = Pattern.compile("attr\\(var\\((\\d+)\\),'(.*)'\\),var\\((\\d+)\\)");
    //TODO check wether a projection can be a simple attribute
    public static Pattern projections = Pattern.compile("proj\\(var\\((\\d+)\\),'(.*)'\\),var\\((\\d+)\\)");
    public static Pattern terminals = Pattern.compile("attr\\(var\\((\\d+)\\),'(.*)'\\),('.*')");
  //  public static Pattern projections = Pattern.compile("proj\\(var\\((\\d+)\\),('.*')\\),('.*')");

    //C-structure matcher
    //matches trees, projections (phi), and c-projections
    /*
    cf(1,subtree(1764,'ROOT',1763,293)),
	cf(1,phi(1764,var(0))),
	cf(1,cproj(1764,var(23))),
     */
    public static Pattern semForm = Pattern.compile("cf\\((.+?),semform_data\\((.+?),(.+?),(.+?),(.+?)\\)");
    public static Pattern surfaceForm = Pattern.compile("cf\\((.+?),surfaceform\\((.+?),(.+?),(.+?),(.+?)\\)");

    public static Pattern subtreePattern = Pattern.compile("cf\\((.+?),subtree\\((.+?),(.+?),(.+?),(.+?)\\)\\)");
    public static Pattern terminalPattern = Pattern.compile("cf\\((.+?),terminal\\((.+?),(.+?),\\[(.+?)\\]\\)\\)");
    public static Pattern phiPattern = Pattern.compile("cf\\((.+?),phi\\((.+?),var\\((\\d+)\\)\\)\\)");
    public static Pattern cprojPattern = Pattern.compile("cf\\((.+?),cproj\\((.+?),var\\((\\d+)\\)\\)\\)");
    public static Pattern fspan = Pattern.compile("cf\\((.+?),fspan\\(var\\((.+)\\),(.+),(.+)\\)\\)");

    private final static Logger LOGGER = LoggerFactory.getLogger(FsProlog2Java.class);

    public ReadFsProlog In;
    //public LinkedHashMap<Integer, List<Object>> FsHash;


    public FsProlog2Java(ReadFsProlog input)
    {
        this.In = input;
    }

    public static  LinkedHashMap<Set<ChoiceVar>,LinkedHashMap<Integer, List<AttributeValuePair>>> fs2Hash(ReadFsProlog plFs) {
        // This method creates a hashmap from prolog input

        List<String> constraints = plFs.fstr;
        LinkedHashMap<Set<ChoiceVar>, LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash =
                new LinkedHashMap<>();

        //Patterns for different kinds of f-structure constraints


        for (String constraint : constraints) {
            Matcher ambMatcher = ambiguities.matcher(constraint);
            Matcher keyMatcher = keys.matcher(constraint);
            Matcher predsMatcher = preds.matcher(constraint);
            Matcher adjunctMatcher = adjuncts.matcher(constraint);
            Matcher setMatcher = inSet.matcher(constraint);
            Matcher nonTerminalMatcher = nonTerminals.matcher(constraint);
            Matcher projectionMatcher = projections.matcher(constraint);
            Matcher terminalsMatcher = terminals.matcher(constraint);
            Matcher subsumeMatcher = subsume.matcher(constraint);

            Set<ChoiceVar> context = null;

            if (ambMatcher.find()) {
                context = ChoiceSpace.parseChoice(ambMatcher.group(1));
                plFs.cp.choices.add(context);

                if (!fsHash.containsKey(context)) {
                    LinkedHashMap<Integer, List<AttributeValuePair>> fsConstraints = new LinkedHashMap<>();
                    fsHash.put(context, fsConstraints);
                }
            }
            else {
                LOGGER.warn(constraint + " threw an error");
            }

            // Collects keys for hashMap
            while (keyMatcher.find()) {
                Integer var = Integer.parseInt(keyMatcher.group(1));
                if (!fsHash.get(context).containsKey(var)) {
                    Integer key = var;
                    List<AttributeValuePair> values = new ArrayList<AttributeValuePair>();
                    fsHash.get(context).put(key, values);
                }
            }

            //Processes preds
            if (predsMatcher.find()) {
                Integer key = Integer.parseInt(predsMatcher.group(1));
                List<AttributeValuePair> values = fsHash.get(context).get(key);
                PredAVP avp = new PredAVP(predsMatcher.group(2), predsMatcher.group(3));
                values.add(avp);
                continue;
            }

            //Processes adjuncts
            if (adjunctMatcher.find()) {

                List<AttributeValuePair> values =
                        fsHash.get(context).get(Integer.parseInt(adjunctMatcher.group(1)));
                Adjunct avp = new Adjunct(adjunctMatcher.group(2),"f");
                values.add(avp);
                continue;
            }

            //Processes non-terminal nodes
            if (nonTerminalMatcher.find()) {
                Integer key = Integer.parseInt(nonTerminalMatcher.group(1));
                List<AttributeValuePair> values = fsHash.get(context).get(key);
                NonTerminalAVP avp = new NonTerminalAVP(nonTerminalMatcher.group(2), nonTerminalMatcher.group(3),"f");
                values.add(avp);
                continue;
            }

            if (projectionMatcher.find()) {
                Integer key = Integer.parseInt(projectionMatcher.group(1));
                List<AttributeValuePair> values = fsHash.get(context).get(key);
                Projection avp = new Projection(projectionMatcher.group(2), projectionMatcher.group(3));
                values.add(avp);
                continue;
            }


            if (setMatcher.find()) {
                Integer key = Integer.parseInt(setMatcher.group(2));
                List<AttributeValuePair> values = fsHash.get(context).get(key);

                Matcher varMatcher = keys.matcher(setMatcher.group(1));

                String var;

                if (varMatcher.find())
                {
                    var = varMatcher.group(1);
                }
                else
                {
                    var = setMatcher.group(1);
                }

                AdjunctSet avp = new AdjunctSet(var,"f");
                values.add(avp);
                continue;
            }

            if (subsumeMatcher.find()) {
                Integer key = Integer.parseInt(subsumeMatcher.group(2));
                List<AttributeValuePair> values = fsHash.get(context).get(key);

                Matcher varMatcher = keys.matcher(subsumeMatcher.group(1));

                String var;

                if (varMatcher.find())
                {
                    var = varMatcher.group(1);
                }
                else
                {
                    var = setMatcher.group(1);
                }

                SubsumeRel avp = new SubsumeRel(var,"f");
                values.add(avp);
                continue;
            }


            // Processes terminal nodes in the f-structure
            if (terminalsMatcher.find()) {
                Integer key = Integer.parseInt(terminalsMatcher.group(1));
                List<AttributeValuePair> values = fsHash.get(context).get(key);
                TerminalAVP avp = new TerminalAVP(terminalsMatcher.group(2), terminalsMatcher.group(3),"f");
                values.add(avp);
                continue;
            }

        }

        /*
         public static Pattern subtreePattern = Pattern.compile("cf\\((.+?),subtree\\((.+?),(.+?),(.+?),(.+?)\\)\\)");
    public static Pattern terminalPattern = Pattern.compile("cf\\((.+?),terminal\\((.+?),(.+?),(.+?)\\)\\)");
    public static Pattern phiPattern = Pattern.compile("cf\\((.+?),phi\\((.+?),(var\\(\\d+\\))\\)\\)");
    public static Pattern cprojPattern = Pattern.compile("cf\\((.+?),cproj\\((.+?),(var\\(\\d+\\))\\)\\)");
    public static Pattern fspan
         */



        for (String cstrConstraint : plFs.cstr)
        {
        Matcher subTreeMatcher = subtreePattern.matcher(cstrConstraint);
        Matcher terminalMatcher = terminalPattern.matcher(cstrConstraint);
        Matcher phiMatcher = phiPattern.matcher(cstrConstraint);
        Matcher cprojMatcher = cprojPattern.matcher(cstrConstraint);
        Matcher fspanMatcher = fspan.matcher(cstrConstraint);
        Matcher semFormMatcher = semForm.matcher(cstrConstraint);
        Matcher surfaceFormMatcher = surfaceForm.matcher(cstrConstraint);


        if (subTreeMatcher.find()) {
            String context = subTreeMatcher.group(1);
            String mother = subTreeMatcher.group(2);
            String category = subTreeMatcher.group(3);
            String left = subTreeMatcher.group(4);
            String right = subTreeMatcher.group(5);

            Set<ChoiceVar> choiceVar = null;

            choiceVar = ChoiceSpace.parseChoice(context);
            plFs.cp.choices.add(choiceVar);

            if (!fsHash.containsKey(choiceVar)) {
                LinkedHashMap<Integer, List<AttributeValuePair>> fsConstraints = new LinkedHashMap<>();
                fsHash.put(choiceVar, fsConstraints);
                fsConstraints.put(Integer.parseInt(mother), new ArrayList<>());
                fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("left", left,"c"));
                fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("right", right,"c"));
                fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("CAT", category,"c"));


            } else {
                if (!fsHash.get(choiceVar).containsKey(Integer.parseInt(mother))) {
                    List<AttributeValuePair> avps = new ArrayList<>();
                    fsHash.get(choiceVar).put(Integer.parseInt(mother), avps);
                }

                fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("left", left,"c"));
                fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("right", right,"c"));
                fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("CAT", category,"c"));

            }
        }

        if (terminalMatcher.find())
        {
            String context = terminalMatcher.group(1);
            String mother = terminalMatcher.group(2);
            String category = terminalMatcher.group(3);
            String terminal = terminalMatcher.group(4);

            Set<ChoiceVar> choiceVar = null;

            choiceVar = ChoiceSpace.parseChoice(context);
            plFs.cp.choices.add(choiceVar);

            if (!fsHash.containsKey(choiceVar)) {
                LinkedHashMap<Integer, List<AttributeValuePair>> fsConstraints = new LinkedHashMap<>();
                fsHash.put(choiceVar, fsConstraints);
                fsConstraints.put(Integer.parseInt(mother), new ArrayList<>());
                fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("CAT", category,"c"));
                fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("terminal",terminal,"c"));

            } else {
                if (!fsHash.get(choiceVar).containsKey(Integer.parseInt(mother))) {
                    List<AttributeValuePair> avps = new ArrayList<>();
                    fsHash.get(choiceVar).put(Integer.parseInt(mother), avps);
                }

                fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("terminal", terminal,"c"));
                fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("CAT", category,"c"));

            }

        }

            if (phiMatcher.find())
            {
                String context = phiMatcher.group(1);
                String mother = phiMatcher.group(2);
                String daughter = phiMatcher.group(3);

                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                if (!fsHash.containsKey(choiceVar)) {
                    LinkedHashMap<Integer, List<AttributeValuePair>> fsConstraints = new LinkedHashMap<>();
                    fsHash.put(choiceVar, fsConstraints);
                    fsConstraints.put(Integer.parseInt(mother), new ArrayList<>());
                    fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("phi", daughter,"c"));


                } else {
                    if (!fsHash.get(choiceVar).containsKey(Integer.parseInt(mother))) {
                        List<AttributeValuePair> avps = new ArrayList<>();
                        fsHash.get(choiceVar).put(Integer.parseInt(mother), avps);
                    }

                    fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("phi", daughter,"c"));


                }

            }

            /*
            	cf(1,semform_data(1,18,1,5)),
	cf(1,semform_data(3,102,6,10)),
	cf(1,semform_data(9,178,16,20)),
	cf(1,semform_data(12,192,21,24)),
	cf(1,semform_data(13,222,25,29)),
	cf(1,fspan(var(0),1,30)),
	cf(1,fspan(var(11),1,5)),
	cf(1,fspan(var(2),11,29)),
	cf(1,fspan(var(4),16,20)),
	cf(1,fspan(var(10),25,29)),
	cf(1,surfaceform(1,'John',1,5)),
	cf(1,surfaceform(48,'said',6,10)),
	cf(1,surfaceform(112,'that',11,15)),
	cf(1,surfaceform(161,'Mary',16,20)),
	cf(1,surfaceform(189,'was',21,24)),
	cf(1,surfaceform(218,'sick',25,29)),
	cf(1,surfaceform(236,'.',29,30))
             */

            //"cf\\((.+?),fspan\\((.+),(.+),(.+)\\)\\)"

            if (fspanMatcher.find()) {
                String context = fspanMatcher.group(1);
                String mother = fspanMatcher.group(2);
                String start = fspanMatcher.group(3);
                String end = fspanMatcher.group(4);


                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                if (!fsHash.containsKey(choiceVar)) {
                    LinkedHashMap<Integer, List<AttributeValuePair>> fsConstraints = new LinkedHashMap<>();
                    fsHash.put(choiceVar, fsConstraints);
                    fsConstraints.put(Integer.parseInt(mother), new ArrayList<>());
                    fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("start", "int(" + start + ")","f"));
                    fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("end","int(" + end + ")","f"));


                } else {
                    if (!fsHash.get(choiceVar).containsKey(Integer.parseInt(mother))) {
                        List<AttributeValuePair> avps = new ArrayList<>();
                        fsHash.get(choiceVar).put(Integer.parseInt(mother), avps);
                    }

                    fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("start", "int(" + start + ")","f"));
                    fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("end","int(" + end + ")","f"));
                }
            }

            /*

            	cf(1,semform_data(12,192,21,24)),
	            cf(1,surfaceform(48,'said',6,10)),
                public static Pattern semForm = Pattern.compile("cf\\((.+?),semform_data\\((.+?),(.+?),(.+?),(.+?)\\)");
    public static Pattern surfaceForm = Pattern.compile("cf\\((.+?),surfaceform\\((.+?),(.+?),(.+?),(.+?)\\)");
             */


            if (cprojMatcher.find()) {
                //  Pattern.compile("cf\\((.+?),cproj\\((.+?),(var\\(\\d+\\))\\)\\)");
                String context = cprojMatcher.group(1);
                String mother = cprojMatcher.group(2);
                String daughter = cprojMatcher.group(3);

                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                if (!fsHash.containsKey(choiceVar)) {
                    LinkedHashMap<Integer, List<AttributeValuePair>> fsConstraints = new LinkedHashMap<>();
                    fsHash.put(choiceVar, fsConstraints);
                    fsConstraints.put(Integer.parseInt(mother), new ArrayList<>());
                    fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("cproj", daughter, "c"));

                } else {
                    if (!fsHash.get(choiceVar).containsKey(Integer.parseInt(mother))) {
                        fsHash.get(choiceVar).put(Integer.parseInt(mother), new ArrayList<>());
                    }

                    fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("cproj", daughter, "c"));
                }
            }
            if (surfaceFormMatcher.find()) {
                String context = surfaceFormMatcher.group(1);
                String mother = surfaceFormMatcher.group(2);
                String surfaceString = surfaceFormMatcher.group(3);
                String start = surfaceFormMatcher.group(4);
                String end = surfaceFormMatcher.group(5);



                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                if (!fsHash.containsKey(choiceVar)) {
                    LinkedHashMap<Integer, List<AttributeValuePair>> fsConstraints = new LinkedHashMap<>();
                    fsHash.put(choiceVar, fsConstraints);
                    fsConstraints.put(Integer.parseInt(mother), new ArrayList<>());
                    fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("token",surfaceString,"c"));
                    fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("start", "int(" +start + ")","f"));
                    fsConstraints.get(Integer.parseInt(mother)).add(new NonTerminalAVP("end", "int(" + end +")","f"));


                } else {
                    if (!fsHash.get(choiceVar).containsKey(Integer.parseInt(mother))) {
                        List<AttributeValuePair> avps = new ArrayList<>();
                        fsHash.get(choiceVar).put(Integer.parseInt(mother), avps);
                    }
                    fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("token",surfaceString,"c"));
                    fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("start", "int(" + start + ")","f"));
                    fsHash.get(choiceVar).get(Integer.parseInt(mother)).add(new NonTerminalAVP("end","int(" + end + ")","f"));
                }
            }


            //TODO semform data.. Do we need it?



        }

        //TODO add to logger?
        // Debug print fsHash
/*
        for (Set<ChoiceVar> key1 : fsHash.keySet()) {
            for (Integer key : fsHash.get(key1).keySet()) {
                String keyO = key.toString();
                String value = fsHash.get(key1).get(key).toString();
                System.out.println(key1 + " " + keyO + " " + value);
            }
        }
*/
        return fsHash;
    }


    public static List<GraphConstraint> fs2List(ReadFsProlog plFs) {
        // This method creates a hashmap from prolog input

        List<String> constraints = plFs.fstr;
        List<GraphConstraint> graphConstraints = new ArrayList<>();

        //Patterns for different kinds of f-structure constraints

        for (int i = 0; i < constraints.size(); i++) {
            String constraint = constraints.get(i);
            Matcher ambMatcher = ambiguities.matcher(constraint);
            Matcher keyMatcher = keys.matcher(constraint);
            Matcher predsMatcher = preds.matcher(constraint);
            Matcher adjunctMatcher = adjuncts.matcher(constraint);
            Matcher setMatcher = inSet.matcher(constraint);
            Matcher nonTerminalMatcher = nonTerminals.matcher(constraint);
            Matcher projectionMatcher = projections.matcher(constraint);
            Matcher terminalsMatcher = terminals.matcher(constraint);
            Matcher subsumeMatcher = subsume.matcher(constraint);

            Set<ChoiceVar> context = null;

            if (ambMatcher.find()) {
                context = ChoiceSpace.parseChoice(ambMatcher.group(1));
                plFs.cp.choices.add(context);
            }
            else {
                LOGGER.warn(constraint + " threw an error when parsing choices");
            }

            boolean root = false;

            if ( i == 0){
                root = true;
            }


            //Processes preds
            if (predsMatcher.find()) {

                graphConstraints.add(new GraphConstraint(context, predsMatcher.group(1), predsMatcher.group(2), predsMatcher.group(3), "f",root));
                continue;
            }

            //Processes adjuncts
            if (adjunctMatcher.find()) {

            graphConstraints.add(new GraphConstraint(context, adjunctMatcher.group(1), "ADJUNCT", adjunctMatcher.group(2), "f",root));
                continue;
            }

            //Processes non-terminal nodes
            if (nonTerminalMatcher.find()) {
                graphConstraints.add(new GraphConstraint(context, nonTerminalMatcher.group(1), nonTerminalMatcher.group(2), nonTerminalMatcher.group(3), "f",root));
                continue;
            }

            if (projectionMatcher.find()) {
                graphConstraints.add(new GraphConstraint(context, projectionMatcher.group(1), projectionMatcher.group(2), projectionMatcher.group(3), "f",root));
                continue;
            }


            if (setMatcher.find()) {
                String key = setMatcher.group(2);
                Matcher varMatcher = keys.matcher(setMatcher.group(1));
                String var;

                if (varMatcher.find())
                {
                    var = varMatcher.group(1);
                }
                else
                {
                    var = setMatcher.group(1);
                }

                graphConstraints.add(new GraphConstraint(context, key, "in_set", var, "f", root));
                continue;
            }

            if (subsumeMatcher.find()) {
                String key = subsumeMatcher.group(2);
                Matcher varMatcher = keys.matcher(subsumeMatcher.group(1));

                String var;

                if (varMatcher.find())
                {
                    var = varMatcher.group(1);
                }
                else
                {
                    var = setMatcher.group(1);
                }

                graphConstraints.add(new GraphConstraint(context, key, "subsume", var, "f",root));
                continue;
            }


            // Processes terminal nodes in the f-structure
            if (terminalsMatcher.find()) {

                graphConstraints.add(new GraphConstraint(context, terminalsMatcher.group(1), terminalsMatcher.group(2), terminalsMatcher.group(3), "f",root));
                continue;
            }

        }

        /*
         public static Pattern subtreePattern = Pattern.compile("cf\\((.+?),subtree\\((.+?),(.+?),(.+?),(.+?)\\)\\)");
    public static Pattern terminalPattern = Pattern.compile("cf\\((.+?),terminal\\((.+?),(.+?),(.+?)\\)\\)");
    public static Pattern phiPattern = Pattern.compile("cf\\((.+?),phi\\((.+?),(var\\(\\d+\\))\\)\\)");
    public static Pattern cprojPattern = Pattern.compile("cf\\((.+?),cproj\\((.+?),(var\\(\\d+\\))\\)\\)");
    public static Pattern fspan
         */



        for (int i = 0; i < plFs.cstr.size(); i++)
        {
            boolean root = false;
            if (i == 0)
            {
                root = true;
            }

            String cstrConstraint = plFs.cstr.get(i);

            Matcher subTreeMatcher = subtreePattern.matcher(cstrConstraint);
            Matcher terminalMatcher = terminalPattern.matcher(cstrConstraint);
            Matcher phiMatcher = phiPattern.matcher(cstrConstraint);
            Matcher cprojMatcher = cprojPattern.matcher(cstrConstraint);
            Matcher fspanMatcher = fspan.matcher(cstrConstraint);
            Matcher semFormMatcher = semForm.matcher(cstrConstraint);
            Matcher surfaceFormMatcher = surfaceForm.matcher(cstrConstraint);


            if (subTreeMatcher.find()) {
                String context = subTreeMatcher.group(1);
                String mother = subTreeMatcher.group(2);
                String category = subTreeMatcher.group(3);
                String left = subTreeMatcher.group(4);
                String right = subTreeMatcher.group(5);

                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                if (left.equals("-"))
                {
                    graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "left", "0" + right, "c",root));
                }else {
                    graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "left", "0" + left, "c",root));
                    graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "right", "0" + right, "c", root));
                }
                graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "CAT", category, "c",root));

            }

            if (terminalMatcher.find())
            {
                String context = terminalMatcher.group(1);
                String mother = terminalMatcher.group(2);
                String category = terminalMatcher.group(3);
                String terminal = terminalMatcher.group(4);

                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "CAT", category, "c",root));
               graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "terminal", "0" + terminal, "c",root));

            }

            if (phiMatcher.find()) {
                String context = phiMatcher.group(1);
                String mother = phiMatcher.group(2);
                String daughter = phiMatcher.group(3);

                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "phi", daughter, "c",root));
            }

            /*
            	cf(1,semform_data(1,18,1,5)),
	cf(1,semform_data(3,102,6,10)),
	cf(1,semform_data(9,178,16,20)),
	cf(1,semform_data(12,192,21,24)),
	cf(1,semform_data(13,222,25,29)),
	cf(1,fspan(var(0),1,30)),
	cf(1,fspan(var(11),1,5)),
	cf(1,fspan(var(2),11,29)),
	cf(1,fspan(var(4),16,20)),
	cf(1,fspan(var(10),25,29)),
	cf(1,surfaceform(1,'John',1,5)),
	cf(1,surfaceform(48,'said',6,10)),
	cf(1,surfaceform(112,'that',11,15)),
	cf(1,surfaceform(161,'Mary',16,20)),
	cf(1,surfaceform(189,'was',21,24)),
	cf(1,surfaceform(218,'sick',25,29)),
	cf(1,surfaceform(236,'.',29,30))
             */

            //"cf\\((.+?),fspan\\((.+),(.+),(.+)\\)\\)"

            if (fspanMatcher.find()) {
                String context = fspanMatcher.group(1);
                String mother = fspanMatcher.group(2);
                String start = fspanMatcher.group(3);
                String end = fspanMatcher.group(4);


                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                graphConstraints.add(new GraphConstraint(choiceVar, mother, "start", "int(" + start + ")", "f",root));
                graphConstraints.add(new GraphConstraint(choiceVar, mother, "end", "int(" + end + ")", "f",root));

            }

            /*

            	cf(1,semform_data(12,192,21,24)),
	            cf(1,surfaceform(48,'said',6,10)),
                public static Pattern semForm = Pattern.compile("cf\\((.+?),semform_data\\((.+?),(.+?),(.+?),(.+?)\\)");
    public static Pattern surfaceForm = Pattern.compile("cf\\((.+?),surfaceform\\((.+?),(.+?),(.+?),(.+?)\\)");
             */


            if (cprojMatcher.find()) {
                //  Pattern.compile("cf\\((.+?),cproj\\((.+?),(var\\(\\d+\\))\\)\\)");
                String context = cprojMatcher.group(1);
                String mother = cprojMatcher.group(2);
                String daughter = cprojMatcher.group(3);

                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "cproj", daughter, "c",root));
            }

            if (surfaceFormMatcher.find()) {
                String context = surfaceFormMatcher.group(1);
                String mother = surfaceFormMatcher.group(2);
                String surfaceString = surfaceFormMatcher.group(3);
                String start = surfaceFormMatcher.group(4);
                String end = surfaceFormMatcher.group(5);



                Set<ChoiceVar> choiceVar = null;

                choiceVar = ChoiceSpace.parseChoice(context);
                plFs.cp.choices.add(choiceVar);

                graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "token", surfaceString, "c",root));
                graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "start", "int(" + start + ")", "c",root));
                graphConstraints.add(new GraphConstraint(choiceVar, "0" + mother, "end", "int(" + end + ")", "c",root));

            }


            //TODO semform data.. Do we need it?



        }

        //TODO add to logger?
        // Debug print fsHash
/*
        for (Set<ChoiceVar> key1 : fsHash.keySet()) {
            for (Integer key : fsHash.get(key1).keySet()) {
                String keyO = key.toString();
                String value = fsHash.get(key1).get(key).toString();
                System.out.println(key1 + " " + keyO + " " + value);
            }
        }
*/

        if (graphConstraints.size() < plFs.fstr.size() + plFs.cstr.size())
        {
            int missing = (plFs.fstr.size() + plFs.cstr.size()) - graphConstraints.size();
            LOGGER.warn("Potentially not all constraints have been parsed correctly." +
                    "Missing " + missing + " constraints.");
        }
        return graphConstraints;
    }

    public static List<GraphConstraint> fsHash2List(
            LinkedHashMap<Set<ChoiceVar>,
                    LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash)
    {

        Pattern string = Pattern.compile("'(.*)'");


        List<GraphConstraint> out = new ArrayList<GraphConstraint>();

        for(Set<ChoiceVar> ambKey : fsHash.keySet())

        {

            for (Integer key : fsHash.get(ambKey).keySet())
            {
                for (AttributeValuePair avp : fsHash.get(ambKey).get(key))
                {

                    Boolean projection = false;
                    if (avp instanceof Projection)
                    {
                        projection = true;
                    }


                    String attribute = avp.attribute;
                    String value = avp.value;

                    Matcher am = string.matcher(avp.attribute);
                    Matcher vm = string.matcher(avp.value);

                    Set<Integer> pathNodes = new HashSet<>();
                    pathNodes.add(key);

                    //pathNodes = returnPathNodes(fsHash,key,ambKey,pathNodes);


                    if (am.matches())
                    {
                        attribute = am.group(1);
                    }

                    if (vm.matches())
                    {
                        if (HelperMethods.isInteger(value)) {
                            value = vm.group(1);
                        }
                    }

                    GraphConstraint gc = new GraphConstraint(ambKey,key,attribute,value,projection);

                    if (avp.projection != null)
                    {
                        gc.setProj(avp.projection);
                    }

                    out.add(gc);
                }


            }
        }

        return out;
    }



    // deletes entries in the fsHash that point at nothing
    public static LinkedHashMap<Integer,List<AttributeValuePair>>
        cleanFsHash(LinkedHashMap<Integer,List<AttributeValuePair>> fsHash) {

        for (Map.Entry<Integer,List<AttributeValuePair>> entry : fsHash.entrySet())
        {
            List<AttributeValuePair> values = entry.getValue();

            for (int i = 0; i < values.size(); i++)
            {
                if (values.get(i) instanceof NonTerminalAVP)
                {
                    if (! fsHash.containsKey(Integer.parseInt(values.get(i).value)) )
                    {
                        values.remove(values.get(i));

                    }
                    if (values.isEmpty())
                    {
                        fsHash.remove(entry.getKey());
                    }
                }
            }
        }
        return fsHash;
    }


    public static LinkedHashMap<Integer,List<AttributeValuePair>>
    deleteEmptyEntries(LinkedHashMap<Integer,List<AttributeValuePair>> fsHash)
    {
        fsHash.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        return fsHash;
    }
}
