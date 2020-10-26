package analysis.QueryParser;

import syntax.xle.Prolog2Java.GraphConstraint;
import utilities.HelperMethods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NodeExpression extends QueryExpression {

    private QueryExpression left;
    private Node right;

    public NodeExpression(QueryExpression left, Node right) {
        setNodeVar(right.getNodeVar());
        setFsIndices(new HashMap<>());
        this.left = left;
        this.right = right;
        setParser(left.getParser());

        calculateSolutions();
    }


    @Override
    public void calculateSolutions() {

        if (left instanceof ConjointExpression) {

            Boolean alreadyBound = false;
            for (Set<String> key : left.getSolution().keySet()) {
                if (left.getSolution().get(key).containsKey(right.getNodeVar())) {
                    setSolution(left.getSolution());
                    //TODO (maybe) set solution of right
                    alreadyBound = true;
                    break;
                }
            }

            if (!alreadyBound)
            {
                HashMap<Set<String>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out = new HashMap<>();


                    HashMap<Set<String>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out2 =
                            right.getSolution();

                    for (Set<String> key : left.getSolution().keySet()){

                    for (Set<String> key2 : out2.keySet())
                    {
                        Set<String> newKey = new HashSet<>();
                        newKey.addAll(key);
                        newKey.addAll(key2);

                        out.put(newKey,new HashMap<>());
                        out.get(newKey).putAll(left.getSolution().get(key));
                        out.get(newKey).putAll(out2.get(key2));
                    }
                }

                    setFsIndices(right.getFsIndices());
                    setSolution(out);

            }
            else
            {
                setSolution(left.getSolution());
                for (Set<String> key : left.getSolution().keySet())
                {
                    for (String key2 : left.getSolution().get(key).get(right.getNodeVar()).keySet())
                    {
                        getFsIndices().putAll(left.getSolution().get(key).get(right.getNodeVar()).get(key2));
                    }
                }
            }



        } else if (left instanceof UncertaintyExpression)
        {
            setFsIndices(left.getFsIndices());
            setSolution(left.getSolution());
        }
        else {

            HashMap<Set<String>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> leftSolution = left.getSolution();
            HashMap<Set<String>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> rightSolution = right.getSolution();



            Set<String> usedKeys = new HashSet<>();

            Iterator<Set<String>> it = rightSolution.keySet().iterator();
            while (it.hasNext()) {
                Set<String> key = it.next();

                String nodeVar = rightSolution.get(key).keySet().stream().findAny().get();
                String nodeRef = rightSolution.get(key).get(nodeVar).keySet().stream().findAny().get();
                HashMap<Integer, GraphConstraint> boundIndices = rightSolution.get(key).get(nodeVar).get(nodeRef);



                //This collects the constraints that are currently bound by the righthand side variable.
                //There are some issues with respect to be the right hand side being empty. The second for-loop
                //ensures that empty structures are also included.

                for (Integer key3 : boundIndices.keySet()) {
                    if (left.getFsIndices().containsKey(key3)) {

                        if (HelperMethods.isInteger(left.getFsIndices().get(key3).getFsValue())) {
                            usedKeys.add((String) left.getFsIndices().get(key3).getFsValue());
                        }
                    }
                }

                for (Integer key3 : boundIndices.keySet())
                {
                    for (Integer key4 : left.getFsIndices().keySet())
                    {
                        if (boundIndices.get(key3).getFsNode().equals(left.getFsIndices().get(key4).getFsValue()))
                        {
                            usedKeys.add((String) left.getFsIndices().get(key4).getFsValue());
                        }
                    }
                }
                }

        //Solution only for current Node variable
            HashMap<Set<String>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out2 =
                    mapUsedKeys(usedKeys, right.getFsIndices(), right.getNodeVar());


            //Collect current values to fsIndices

            for (Set<String> key : out2.keySet())
            {
                for (String key2 : out2.get(key).keySet())
                    for (String key3 : out2.get(key).get(key2).keySet())
                        getFsIndices().putAll(out2.get(key).get(key2).get(key3));
            }

            right.setSolution(out2);

            //  System.out.println("Test");


            //Concatenation with previous variable bindings

            HashMap<Set<String>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> out3 = new HashMap<>();

            for (Set<String> key : leftSolution.keySet()) {


                String nodeVar = left.getNodeVar();
                String nodeRef = leftSolution.get(key).get(nodeVar).keySet().stream().findAny().get();
                HashMap<Integer, GraphConstraint> boundIndices = leftSolution.get(key).get(nodeVar).get(nodeRef);

                for (Set<String> key2 : out2.keySet()) {
                    String nodeVar2 = getNodeVar();
                    String nodeRef2 = out2.get(key2).get(nodeVar2).keySet().stream().findAny().get();
                    //      HashMap<Integer,GraphConstraint> boundIndices2 = out2.get(key2).get(nodeVar2).get(nodeRef2);

                    HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new HashMap<>();

                    for (String key4 : leftSolution.get(key).keySet()) {
                        if (!key4.equals(nodeVar)) {
                            binding.put(key4, leftSolution.get(key).get(key4));
                        }

                    }

                    for (Integer key3 : boundIndices.keySet()) {
                        if (boundIndices.get(key3).getFsValue().equals(nodeRef2)) {
                            binding.put(nodeVar, leftSolution.get(key).get(nodeVar));
                            binding.put(nodeVar2, out2.get(key2).get(nodeVar2));

                            Set<String> newKey = new HashSet<>();
                            newKey.addAll(key);
                            newKey.addAll(key2);

                            out3.put(newKey, binding);
                        }
                    }
                }
            }


            setSolution(out3);
      //      getParser().fsNodeBindings = out3;


        }
    }


    public QueryExpression getLeft() {
        return left;
    }

    public void setLeft(QueryExpression left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

}
