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

public class Value extends QueryExpression {

    public boolean var;
    public boolean strip;
    public boolean idRef;
    public String idVar;

    public Value(String query, HashMap<Integer, GraphConstraint> fsIndices, Boolean var, Boolean strip, QueryParser parser) {
        super(query, fsIndices,parser);
        this.var = var;
        this.strip = strip;
    }

    public void setIdRef(boolean idRef) {
        this.idRef = idRef;
    }

    public void setIdVar(String idVar) {
        this.idVar = idVar;
    }




    @Override
    public void calculateSolutions()
    {
        String lookup = normalizeValue(getQuery());

        HashMap<Integer, GraphConstraint> matching = new HashMap<>();
        for (Integer key : getFsIndices().keySet()) {
            String value = normalizeValue(String.valueOf(getFsIndices().get(key).getFsValue()));
            if (lookup != null && lookup.equals(value)) {
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

    private String normalizeValue(String raw) {
        if (raw == null) {
            return null;
        }

        String normalized = raw.trim();
        if (normalized.startsWith("value=")) {
            normalized = normalized.substring("value=".length());
        }
        if ((normalized.startsWith("'") && normalized.endsWith("'")) ||
                (normalized.startsWith("\"") && normalized.endsWith("\""))) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }
}
