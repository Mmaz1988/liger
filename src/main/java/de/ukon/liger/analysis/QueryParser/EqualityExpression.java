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

        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();

        for (Solution key : left.getSolution().keySet())
        {
            if (!key.isTruthValue()) {
                continue;
            }
            String leftString = resolveValue(key, left);
            String rightString = resolveValue(key, right);

            if (leftString == null || rightString == null)
            {
                continue;
            }

            if (left.idRef || right.idRef) {
                boolean equals;
                try {
                    equals = ValueResolver.compareIds(leftString, rightString) == 0;
                } catch (IllegalArgumentException e) {
                    continue;
                }
                if ((middle.equal && equals) || (!middle.equal && !equals)) {
                    out.put(key, left.getSolution().get(key));
                }
            } else if (middle.equal) {
                if (leftString.equals(rightString)) {
                    out.put(key,left.getSolution().get(key));
                }
            } else {
                if (!leftString.equals(rightString)) {
                    out.put(key,left.getSolution().get(key));
                }
            }

        }


        setFsIndices(left.getFsIndices());
        setConjoinedSolutions(left.getConjoinedSolutions());
        setSolution(out);
  //      getParser().fsNodeBindings = out;
    }

    private String resolveValue(Solution solutionKey, Value value)
    {
        String resolved = ValueResolver.resolve(this, solutionKey, value);
        if (resolved == null)
        {
            return null;
        }

        if ((resolved.startsWith("'") && resolved.endsWith("'")) ||
                (resolved.startsWith("\"") && resolved.endsWith("\""))) {
            resolved = resolved.substring(1, resolved.length() - 1);
        }

        if (value.strip)
        {
            resolved = HelperMethods.stripValue(resolved);
        }

        return resolved;
    }
}
