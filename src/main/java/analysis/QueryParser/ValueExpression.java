package analysis.QueryParser;

import syntax.xle.Prolog2Java.GraphConstraint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ValueExpression extends QueryExpression {

    private QueryExpression left;
    private Value right;


    public ValueExpression(QueryExpression left, Value right)
    {

        this.left = left;
        this.right = right;
        setParser(right.getParser());

   calculateSolutions();


    }


    @Override
    public void calculateSolutions()
    {
        HashMap<Set<String>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();

        out.putAll(left.getSolution());

        HashMap<Integer,GraphConstraint> fsIndices = new HashMap<>();

        HashMap<Set<String>,HashMap<String,String>> newValueBindings = getParser().fsValueBindings;

            Iterator<Set<String>> it = out.keySet().iterator();



            while (it.hasNext()) {
                Set<String> key = it.next();

                String nodeVar = left.getNodeVar();
                String nodeRef = out.get(key).get(nodeVar).keySet().stream().findAny().get();
                HashMap<Integer, GraphConstraint> boundIndices =  out.get(key).get(nodeVar).get(nodeRef);

                HashMap<Integer,GraphConstraint> matchingIndices = new HashMap<>();

                if (right.var)
                {
                        if (!newValueBindings.containsKey(key))
                        {

                            newValueBindings.put(key,new HashMap<>());
                            //
                            Iterator<Set<String>> it2 = getParser().fsValueBindings.keySet().iterator();

                            while (it2.hasNext()) {
                                Set<String> key2 = it2.next();
                                if (!(key.equals(key2)) && key.containsAll(key2)) {
                                    newValueBindings.get(key).putAll(getParser().fsValueBindings.get(key2));
                                }
                            }
                        }
                        }

                for (Integer key2 : boundIndices.keySet()) {
                if (boundIndices.get(key2).getRelationLabel().equals(left.getQuery())) {

                    String varMatch;
                    if (right.var) {
                        if (!newValueBindings.get(key).containsKey(right.getQuery())) {
                            newValueBindings.get(key).put(right.getQuery(),
                                    (String) boundIndices.get(key2).getFsValue());

                            varMatch = (String) boundIndices.get(key2).getFsValue();

                        } else {
                            varMatch = newValueBindings.get(key).get(right.getQuery());
                        }
                    }
                        else
                        {
                            varMatch = right.getQuery();
                        }

                        if (boundIndices.get(key2).getFsValue().equals(varMatch)) {
                            matchingIndices.put(key2, boundIndices.get(key2));
                        }

                }
                }
                if (matchingIndices.keySet().isEmpty())
                {
                    it.remove();
                }

                    fsIndices.putAll(matchingIndices);

            }



            if (!fsIndices.keySet().isEmpty()) {
                setSolution(out);
                setFsIndices(fsIndices);
      //          getParser().fsNodeBindings = out;
            }
        }



}


