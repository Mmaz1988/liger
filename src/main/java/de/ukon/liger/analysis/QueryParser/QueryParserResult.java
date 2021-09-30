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

package analysis.QueryParser;

import syntax.GraphConstraint;

import java.util.HashMap;
import java.util.Set;

public class QueryParserResult {
    public HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> result;
    public HashMap<Set<SolutionKey>, HashMap<String,String>> valueBindings;

    public Boolean isSuccess;

    public QueryParserResult(HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> result,
                             HashMap<Set<SolutionKey>, HashMap<String,String>> valueBindings)
    {

        this.result = result;
        this.isSuccess = !result.keySet().isEmpty();
        this.valueBindings = valueBindings;

    }





}
