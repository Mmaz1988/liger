package analysis.QueryParser;

import syntax.GraphConstraint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class AttributeExpression extends QueryExpression {


    private QueryExpression left;
    private Attribute right;

    public AttributeExpression(QueryExpression left, Attribute right) {
        this.left = left;
        this.right = right;

        setParser(left.getParser());
        setQuery(right.getQuery());
        setNodeVar(left.getNodeVar());

        calculateSolutions();
    }

    @Override
    public void calculateSolutions()
    {
        HashMap<Set<SolutionKey>,HashMap<String, HashMap<String,HashMap<Integer,GraphConstraint>>>> out = left.getSolution();

        HashMap<Integer,GraphConstraint> fsIndices = new HashMap<>();

            Iterator<Set<SolutionKey>> it = out.keySet().iterator();


            while (it.hasNext())
            {
                Set<SolutionKey> key = it.next();
                Boolean containsAtribute = false;



                String nodeRef = left.getSolution().get(key).get(left.getNodeVar()).keySet().stream().findAny().get();
                HashMap<Integer,GraphConstraint> boundIndices = left.getSolution().get(key).get(left.getNodeVar()).get(nodeRef);


                    for (Integer key3 : boundIndices.keySet())
                    {
                        if(boundIndices.get(key3).getRelationLabel().equals(right.getQuery()))
                        {

                            fsIndices.put(key3,boundIndices.get(key3));
                            containsAtribute = true;
                        }
                    }

            if (!containsAtribute)
            {
                it.remove();
            }
        }
            setNodeVar(left.getNodeVar());
            setSolution(out);
            setFsIndices(fsIndices);
    }




}
