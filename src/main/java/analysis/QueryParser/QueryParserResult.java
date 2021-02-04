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
