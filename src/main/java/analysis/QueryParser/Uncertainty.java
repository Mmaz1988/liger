package analysis.QueryParser;

import syntax.GraphConstraint;

import java.util.HashMap;

public class Uncertainty extends QueryExpression {

    public Boolean insideOut;

    public Uncertainty(String query, Boolean insideOut, HashMap<Integer,GraphConstraint> fsIndices, QueryParser qp)
    {
      setQuery(query);
      this.insideOut = insideOut;
      setFsIndices(fsIndices);
      setParser(qp);
    }


    @Override
    public void calculateSolutions()
    {

    }



}
