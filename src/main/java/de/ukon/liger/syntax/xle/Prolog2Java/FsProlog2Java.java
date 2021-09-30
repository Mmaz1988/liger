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

package de.ukon.liger.syntax.xle.Prolog2Java;

import de.ukon.liger.main.DbaMain;
import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.xle.FstructureElements.*;
import de.ukon.liger.utilities.HelperMethods;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// This Object contains a linked hash map f-structure (following Brubeck Unhammer)

public class FsProlog2Java {


    public static Pattern ambiguities = Pattern.compile("cf\\((.+?),\\w+\\(");
    public static Pattern keys = Pattern.compile("var\\((\\d+)\\)");
    public static Pattern preds = Pattern.compile("attr\\(var\\((\\d+)\\),('PRED')\\),(semform\\(.*\\[.*\\],\\[.*\\]\\))");
    public static Pattern adjuncts = Pattern.compile("attr\\(var\\((\\d+)\\),'ADJUNCT'\\),var\\((\\d+)\\)");
    public static Pattern inSet = Pattern.compile("in_set\\((.*),var\\((\\d+)\\)\\)");
    public static Pattern subsume = Pattern.compile("subsume\\((.*),var\\((\\d+)\\)\\)");
    public static Pattern nonTerminals = Pattern.compile("attr\\(var\\((\\d+)\\),('.*')\\),var\\((\\d+)\\)");
    public static Pattern terminals = Pattern.compile("attr\\(var\\((\\d+)\\),('.*')\\),('.*')");
    public static Pattern cstructure = Pattern.compile("(semform_data|surfaceform)\\((.+?),(.+?),(.+?),(.+?)\\)");
    private final static Logger LOGGER = Logger.getLogger(DbaMain.class.getName());

    public ReadFsProlog In;
    //public LinkedHashMap<Integer, List<Object>> FsHash;


    public FsProlog2Java(ReadFsProlog input)
    {
        this.In = input;
    }

    public static  LinkedHashMap<Set<ChoiceVar>,LinkedHashMap<Integer, List<AttributeValuePair>>> fs2Hash(ReadFsProlog plFs) {
        // This method creates a hashmap from prolog input

        List<String> constraints = plFs.prolog;
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
            Matcher terminalsMatcher = terminals.matcher(constraint);
            Matcher cstructureMatcher = cstructure.matcher(constraint);
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
                LOGGER.warning(constraint + " threw an error");
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
                Adjunct avp = new Adjunct(adjunctMatcher.group(2));
                values.add(avp);
                continue;
            }

            //Processes non-terminal nodes
            if (nonTerminalMatcher.find()) {
                Integer key = Integer.parseInt(nonTerminalMatcher.group(1));
                List<AttributeValuePair> values = fsHash.get(context).get(key);
                NonTerminalAVP avp = new NonTerminalAVP(nonTerminalMatcher.group(2), nonTerminalMatcher.group(3));
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

                AdjunctSet avp = new AdjunctSet(var);
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

                SubsumeRel avp = new SubsumeRel(var);
                values.add(avp);
                continue;
            }


            // Processes terminal nodes in the f-structure
            if (terminalsMatcher.find()) {
                Integer key = Integer.parseInt(terminalsMatcher.group(1));
                List<AttributeValuePair> values = fsHash.get(context).get(key);
                TerminalAVP avp = new TerminalAVP(terminalsMatcher.group(2), terminalsMatcher.group(3));
                values.add(avp);
                continue;
            }

            if (cstructureMatcher.find())
            {
                if (!fsHash.get(context).containsKey(-1))
                {
                    fsHash.get(context).put(-1,new ArrayList<AttributeValuePair>());
                }
                else {
                    List<AttributeValuePair> values = fsHash.get(context).get(-1);
                    CsCorrespondence csc = new CsCorrespondence(cstructureMatcher.group(1),
                            Arrays.asList(cstructureMatcher.group(2),
                                            cstructureMatcher.group(3),
                                            cstructureMatcher.group(4),
                                            cstructureMatcher.group(5)));
                    values.add(csc);
                }
            }


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

                    out.add(new GraphConstraint(ambKey,key,attribute,value));
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
