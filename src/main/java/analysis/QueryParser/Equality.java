package analysis.QueryParser;

public class Equality extends QueryExpression {


    public Boolean equal;

    public Equality(Boolean equal, QueryParser qp)
    {
        this.equal = equal;
        setParser(qp);
    }

    @Override
    public void calculateSolutions() {
    }


}
