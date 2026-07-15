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
            String leftString = resolveValue(key, left);
            String rightString = resolveValue(key, right);

            if (leftString == null || rightString == null)
            {
                continue;
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


        setFsIndices(left.getFsIndices());
        setConjoinedSolutions(left.getConjoinedSolutions());
        setSolution(out);
  //      getParser().fsNodeBindings = out;
    }

    private String resolveValue(Set<SolutionKey> solutionKey, Value value)
    {
        String resolved;

        if (value.var)
        {
            resolved = lookupBinding(solutionKey, value.getQuery());
        }
        else
        {
            resolved = value.getQuery();
        }

        if (resolved == null)
        {
            return null;
        }

        if (value.strip)
        {
            resolved = HelperMethods.stripValue(resolved);
        }

        Matcher matcher = HelperMethods.valueStringPattern.matcher(resolved);
        if (matcher.matches())
        {
            resolved = matcher.group(1);
        }

        return resolved;
    }

    private String lookupBinding(Set<SolutionKey> solutionKey, String valueVar)
    {
        HashMap<String, String> exactMatch = getParser().fsValueBindings.get(solutionKey);
        if (exactMatch != null && exactMatch.containsKey(valueVar))
        {
            return exactMatch.get(valueVar);
        }

        String bestMatch = null;
        int bestSize = -1;

        for (Set<SolutionKey> candidate : getParser().fsValueBindings.keySet())
        {
            if (solutionKey.containsAll(candidate) && candidate.size() > bestSize)
            {
                HashMap<String, String> bindings = getParser().fsValueBindings.get(candidate);
                if (bindings != null && bindings.containsKey(valueVar))
                {
                    bestMatch = bindings.get(valueVar);
                    bestSize = candidate.size();
                }
            }
        }

        return bestMatch;
    }
}
