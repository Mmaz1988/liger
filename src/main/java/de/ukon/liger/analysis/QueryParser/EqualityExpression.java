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

package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;

public class EqualityExpression extends QueryExpression {

    private Value left;
    private Equality middle;
    private Value right;

    public EqualityExpression(Value left, Equality middle, Value right)
    {
        this.left = left;
        this.middle = middle;
        this.right = right;
        setParser(middle.getParser());
        calculateSolutions();
    }


    @Override
    public void calculateSolutions() {

        HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();

        for (Set<SolutionKey> key : left.getSolution().keySet())
        {
            String leftString = "";
            String rightString = "";

            if (left.var)
            {
                for (Set<SolutionKey> key2 : getParser().fsValueBindings.keySet())
                {
                    if (key.containsAll(key2))
                    {
                        //TODO ambiguity?
                        leftString = getParser().fsValueBindings.get(key2).get(left.getQuery());
                        break;
                    }
                }

                /*
                if (getParser().fsValueBindings.get(key).containsKey(left.getQuery()))
                {
                    leftString = getParser().fsValueBindings.get(key).get(left.getQuery());
                }
                 */

                if (left.strip)
                {
                    leftString = HelperMethods.stripValue(leftString);
                }
            } else
            {
                leftString = left.getQuery();
            }

            if (right.var)
            {
                for (Set<SolutionKey> key2 : getParser().fsValueBindings.keySet())
                {
                    if (key.containsAll(key2))
                    {
                        //TODO ambiguity?
                        leftString = getParser().fsValueBindings.get(key2).get(right.getQuery());
                        break;
                    }
                }
                /*
                if (getParser().fsValueBindings.get(key).containsKey(right.getQuery()))
                {
                    rightString = getParser().fsValueBindings.get(key).get(right.getQuery());
                }
                 */

                if (right.strip)
                {
                    rightString = HelperMethods.stripValue(rightString);
                }

            } else
            {
                rightString = right.getQuery();
            }

            if (!rightString.equals("") && !leftString.equals(""))
            {
               Matcher m1 = HelperMethods.valueStringPattern.matcher(rightString);
               Matcher m2 = HelperMethods.valueStringPattern.matcher(leftString);

               if (m1.find())
               {
                   rightString = m1.group(1);
               }

               if (m2.find())
               {
                   leftString = m2.group(1);
               }


                if (middle.equal)
                {
                 if (leftString.equals(rightString))
                 {
                     out.put(key,left.getSolution().get(key));
                 }
                }
                else
                {
                    if (!leftString.equals(rightString))
                    {
                        out.put(key,left.getSolution().get(key));
                    }
                }
            }

        }


        setFsIndices(left.getFsIndices());
        setConjoinedSolutions(left.getConjoinedSolutions());
        setSolution(out);
  //      getParser().fsNodeBindings = out;
    }
}
