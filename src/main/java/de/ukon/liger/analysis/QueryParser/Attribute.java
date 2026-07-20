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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Collections;

public class Attribute extends QueryExpression {

    public Attribute(String query, HashMap<Integer, GraphConstraint> fsIndices, QueryParser parser) {
        super(query, fsIndices,parser);

        HashMap<Integer,GraphConstraint> fs = new HashMap<>();
        String lookup = query != null && query.startsWith("edge=") ? query.substring("edge=".length()) : query;

        for (int key : fsIndices.keySet())
        {
            if (fsIndices.get(key).getRelationLabel().equals(lookup))
            {
                fs.put(key,fsIndices.get(key));
            }
        }
        setFsIndices(fs);
        calculateSolutions();
    }



    @Override
    public void calculateSolutions()
    {
        String lookup = getQuery() != null && getQuery().startsWith("edge=")
                ? getQuery().substring("edge=".length())
                : getQuery();

        HashMap<Integer, GraphConstraint> matching = new HashMap<>();
        for (Integer key : getFsIndices().keySet()) {
            if (lookup != null && lookup.equals(getFsIndices().get(key).getRelationLabel())) {
                matching.put(key, getFsIndices().get(key));
            }
        }

        if (!matching.isEmpty()) {
            setFsIndices(matching);

            HashMap<String, HashMap<Integer, GraphConstraint>> reference = new HashMap<>();
            reference.put("__match__", matching);
            HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new LinkedHashMap<>();
            binding.put("__match__", reference);

            HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();
            out.put(new Solution(Collections.singleton(new SolutionKey("__match__", "__match__"))), binding);
            setSolution(out);
        }
    }


}
