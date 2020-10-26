package analysis.QueryParser;

public class ConjointExpression extends QueryExpression {



    private QueryExpression previous;

    public ConjointExpression(QueryExpression previous)
    {
        this.previous = previous;
        setParser(previous.getParser());
        setFsIndices(previous.getFsIndices());
        calculateSolutions();
    }






    @Override
    public void calculateSolutions()
    {
    setSolution(previous.getSolution());
    }


}
