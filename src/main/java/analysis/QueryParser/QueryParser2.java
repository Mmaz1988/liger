package analysis.QueryParser;

import syntax.GraphConstraint;

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


        if (c == '#')
        {
            pos++;

            StringBuilder sb = new StringBuilder();
            while(query.charAt(pos) != ' ')
            {
                sb.append(query.charAt(pos));
                pos++;
            }
            String varName = sb.toString();

            Node n = new Node(varName,fsIndices);




        }

        if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122))
        {

        }

        return null;
    }
}
