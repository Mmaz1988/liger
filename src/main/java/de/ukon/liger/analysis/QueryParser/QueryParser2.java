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

import java.util.HashMap;
import java.util.LinkedList;

public class QueryParser2 {

    private int pos = 0;

    // LinkedList<QueryExpression> parsedExpressions = new LinkedList<>();

    public QueryExpression parseQuery(String query, LinkedList<QueryExpression> parsedExpressions, HashMap<Integer, GraphConstraint> fsIndices)
    {
        while (query.charAt(pos) == ' ')
        {
            pos++;
        }

        char c = query.charAt(pos);


        if (c == '#' || c == '*')
        {
            pos++;

            StringBuilder sb = new StringBuilder();
            while(query.charAt(pos) != ' ')
            {
                sb.append(query.charAt(pos));
                pos++;
            }
            String varName = sb.toString();

            boolean constant = false;
            if (c == '*') {
                constant = true;
            }

            Node newNode = new Node(varName, constant, fsIndices);




        }

        if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122))
        {

        }

        return null;
    }
}
