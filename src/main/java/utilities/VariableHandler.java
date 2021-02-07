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

package utilities;

//import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class VariableHandler {
    public enum variableType{
        SENTENCE_ID,
        FS_NODE,
        EVENT_IID,
        TIME_ID,
        TREL_ID,
        EV_NODE

/*
Possibly add more types, e.g. SemVarE, SemVarT etc.
*/

    }

    private static List<String> eiids;
    private static Integer eiid_size = 1000;
    private static List<String> tids;
    private static Integer tid_size = 1000;
    private static List<String> trelids;
    private static Integer trelids_size = 1000;

    private Integer ev_node;

    private HashMap<Integer,String> assignment = new HashMap<>();



    private HashMap<variableType,List<String>> usedVariables = usedVars();


    public VariableHandler()
    {
        if (ev_node == null)
        {
            ev_node = 1000;
        }
    }


    //Instantiates memory for used vars
    private static HashMap usedVars() {
        HashMap<variableType, List<String>> usedVars = new HashMap<>();

      usedVars.put(variableType.FS_NODE,
                new ArrayList<String>());

        usedVars.put(variableType.SENTENCE_ID,
                new ArrayList<>());

        return usedVars;

    }


    private static HashMap<variableType,List<String>> reservedVariables = reservedVars();


    private static HashMap reservedVars()
    {
        HashMap<variableType,List<String>> reservedVars = new HashMap<>();

        //Variables for linear logic
        reservedVars.put(variableType.FS_NODE,
                new ArrayList<String>(Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z")));




        return reservedVars;
    }


    public Integer returnEvNode()
    {
        Integer re = ev_node;

        ev_node++;

        return re;
    }

    public String returnNewVar(variableType varType, Integer value)
    {

        if (varType == variableType.FS_NODE) {

            List<String> variables = reservedVariables.get(varType);


            if (assignment.keySet().contains(value)) {
                return assignment.get(value);
            }

            for (String var : variables) {
                if (!usedVariables.get(varType).contains(var)) {
                    usedVariables.get(varType).add(var);
                    assignment.put(value, var);
                    return var;
                }
            }

            int i = 0;
            //threshold for trying out new indices; can be set higher?
            while (i < 3) {

                for (String var : variables) {

                    String varPrime = var + i;
                    if (!usedVariables.get(varType).
                            contains(varPrime)) {
                        getReservedVariables().get(varType).add(varPrime);
                        usedVariables.get(varType).add(varPrime);
                        assignment.put(value, varPrime);
                        return varPrime;
                    }
                }
                i++;
            }
        }
        else if (varType == variableType.SENTENCE_ID)
        {

            String out = null;

            int i = 0;
            int max = 10000;

            while(i < max) {
                if (usedVariables.get(variableType.SENTENCE_ID).contains("S" + i)) {
                    i++;
                } else
                {
                    usedVariables.get(variableType.SENTENCE_ID).add("S" + i);
                    return "S" + i;
                }
            }




            //threshold for trying out new indices; can be set higher?




        }


        else if (varType == variableType.EVENT_IID)
        {
            int i = 0;
            while (i < eiid_size) {

                if (eiids == null)
                {
                    eiids = new ArrayList<String>();
                }


                    String var = "e" + i;
                    if (!eiids.contains(var)) {
                       // assignment.put(value, var);
                        eiids.add(var);

                        return var;
                    }
                    i++;
                }

            }
            else if (varType == variableType.TIME_ID) {
            int i = 0;
            while (i < tid_size) {

                if (tids == null) {
                    tids = new ArrayList<String>();
                }

                String var = "t" + i;
                if (!tids.contains(var)) {
                    // assignment.put(value, var);
                    tids.add(var);

                    return var;
                }
                i++;
            }
        }
             else if (varType == variableType.TREL_ID) {
            int i = 0;
            while (i < trelids_size) {

                if (trelids == null) {
                    trelids = new ArrayList<String>();
                }


                String var = "l" + i;
                if (!trelids.contains(var)) {
                    // assignment.put(value, var);
                    trelids.add(var);

                    return var;
                }
                i++;
            }

        }

        return null;
    }


   /*
    public void addUsedVariable(variableType varType,String var)
    {
        usedVariables.get(varType).add(var);
    }
*/

    public void resetVars()
    {
        usedVariables = usedVars();
    }


    //setters and Getters

    public HashMap<variableType, List<String>> getUsedVariables() {
        return usedVariables;
    }

    public void setUsedVariables(HashMap<variableType, List<String>> usedVariables) {
        this.usedVariables = usedVariables;
    }

    public HashMap<variableType, List<String>> getReservedVariables() {
        return reservedVariables;
    }

    public void setReservedVariables(HashMap<variableType, List<String>> reservedVariables) {
        VariableHandler.reservedVariables = reservedVariables;
    }


    public HashMap<Integer, String> getAssignment() {
        return assignment;
    }

    public void setAssignment(HashMap<Integer, String> assignment) {
        this.assignment = assignment;
    }

    public static void resetVariableHandler()
    {
        eiids = null;
        tids = null;
    }



}
