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

package de.ukon.liger.utilities;

import de.ukon.liger.syntax.GraphConstraint;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelperMethods {


    public static Pattern fsNodePattern = Pattern.compile("#([a-z])");
    public static Pattern valueVarPattern = Pattern.compile("(%[a-z])");
    //semform('say',3,[var(11),var(2)],[]))
    public static Pattern predPattern = Pattern.compile("semform\\('(.+)',.+\\)");
    public static Pattern hyphenPattern = Pattern.compile("'(.+)'");
    public static Pattern valueStringPattern = Pattern.compile("'(.+)'");
    public static Pattern stripPattern = Pattern.compile("(strip\\((.+)\\))");
    public static Pattern uncertaintyPattern = Pattern.compile("([\\^|!])\\((.*)\\)");


        public static boolean isInteger (Object s)
        {
            boolean isValidInteger = false;
            try {
                Integer.parseInt((String) s);

                // s is a valid integer

                isValidInteger = true;
            } finally {
                return isValidInteger;
                // s is not an integer
            }
        }

    public static String stripValue(String value)
    {
        Matcher m = predPattern.matcher(value);

        if (m.matches())
        {
            return m.group(1);
        } else
        {
            Matcher m2 = hyphenPattern.matcher(value);
            if (m2.matches()) {
                return m2.group(1);
            }
        }

        return value;
        //semform('say',3,[var(11),var(2)],[]))
    }


    public static String stripValeue2(String value) {

        StringBuilder newString = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c == 's') {
               try {
                   int j = i;
                   if (value.charAt(j + 1) == 't' && value.charAt(j + 2) == 'r' && value.charAt(j + 3) == 'i' && value.charAt(j + 4) == 'p'
                           && value.charAt(j + 5) == '(') {
                       StringBuilder sb = new StringBuilder();
                       int bracketCounter = 1;
                       i = i + 6;
                       while (bracketCounter > 0) {
                           char ch = value.charAt(i);
                           if (ch == '(') {
                               sb.append(ch);
                               i++;
                               bracketCounter++;
                           } else if (ch == ')' && bracketCounter > 1) {
                               sb.append(ch);
                               i++;
                               bracketCounter = bracketCounter - 1;
                           } else if (ch == ')' && bracketCounter == 1) {
                               bracketCounter = bracketCounter - 1;
                               i++;
                           } else {
                               sb.append(ch);
                               i++;
                           }
                       }
                       newString.append(HelperMethods.stripValue(sb.toString()));
                   }
               } catch(Exception e)
               {
                   System.out.println("Hit end of string");
               }
            }
            if (i < value.length()) {
                newString.append(value.charAt(i));
            }else
            {
                break;
            }
        }
        return newString.toString();
    }

    /**
     * IDs are strings consisting of a type identifier (e.g. w for word) and an integer. For example
     * @param id
     * @return
     */
    public static int getIntegerFromID(String id)
    {
        Pattern p = Pattern.compile("([a-z]|[A-Z])*(\\d+)");
        Matcher pm = p.matcher(id);
        if (pm.matches())
        {
            return Integer.parseInt(pm.group(2));
        }
        return 0;
    }


    public static boolean isValue(String query, HashMap<Integer, GraphConstraint> fsIndices)
    {
        for (Integer key : fsIndices.keySet())
        {
            if (fsIndices.get(key).getFsValue().equals(query))
            {
                return true;
            }
        }

        Matcher m = HelperMethods.valueVarPattern.matcher(query);
        Matcher sm = HelperMethods.stripPattern.matcher(query);
        Matcher vm = HelperMethods.valueStringPattern.matcher(query);

        if (m.matches())
        {
            return true;
        }

        if (sm.matches())
        {
            return true;
        }

        if (vm.matches())
        {
            return true;
        }
        return false;
    }


    public static boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }



}


