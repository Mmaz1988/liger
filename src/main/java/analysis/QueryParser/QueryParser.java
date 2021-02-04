package analysis.QueryParser;

import syntax.SyntacticStructure;
import syntax.GraphConstraint;
import utilities.HelperMethods;
import utilities.VariableHandler;

import java.util.*;
import java.util.regex.Matcher;

public class QueryParser {

    //TODO combine with GUI (set gui as field, to throw exceptions inside GUI
    private LinkedList<QueryExpression> queryList;
    private HashMap<Integer, GraphConstraint> fsIndices;
    private VariableHandler vh = new VariableHandler();
    private Set<String> usedKeys = new HashSet<>();

    public HashMap<Set<SolutionKey>, HashMap<String,String>> fsValueBindings = new HashMap<>();


    //TODO why are the values of the result hashmap empty?

    public QueryParser(String query, SyntacticStructure fs)
    {

        HashMap<Integer,GraphConstraint> fsIndexed = new HashMap<>();

        for (int i = 0; i < fs.constraints.size();i++)
        {
            usedKeys.add(fs.constraints.get(i).getFsNode());
            fsIndexed.put(i,fs.constraints.get(i));
        }
        //Part 1, String to QueryExpression element

        this.fsIndices = fsIndexed;
        generateQuery(query);

    }

    public QueryParser(SyntacticStructure fs)
    {

        HashMap<Integer,GraphConstraint> fsIndexed = new HashMap<>();

        for (int i = 0; i < fs.constraints.size();i++)
        {
            usedKeys.add(fs.constraints.get(i).getFsNode());
            fsIndexed.put(i,fs.constraints.get(i));
        }
        if (!fs.annotation.isEmpty()) {
            for (int i = 0; i < fs.annotation.size(); i++) {
                usedKeys.add(fs.constraints.get(i).getFsNode());
                fsIndexed.put(i, fs.constraints.get(i));
            }
        }
        //Part 1, String to QueryExpression element
    this.fsIndices = fsIndexed;
    }



    public QueryParser(String query, HashMap<Integer,GraphConstraint> fsIndices)
    {


        for (Integer key : fsIndices.keySet())
        {
            usedKeys.add(fsIndices.get(key).getFsNode());
        }

        //Part 1, String to QueryExpression element
        this.fsIndices = fsIndices;
        generateQuery(query);

    }



    public QueryParser(Deque<String> queryDeque, HashMap<Integer, GraphConstraint> fsIndices)
    {
        this.fsIndices = fsIndices;
        this.queryList = generateQuery(queryDeque);
    }


    public LinkedList<QueryExpression> generateQuery(Deque<String> queryDeque){

        LinkedList<QueryExpression> queryList = new LinkedList<>();
        List<String> deque = (LinkedList<String>) queryDeque;
        HashMap<String, Node> usedFsNodes = new HashMap<String, Node>();

        for (int i = 0; i < queryDeque.size(); i++) {

            Matcher fsM =  HelperMethods.fsNodePattern.matcher(deque.get(i));
            Matcher uM = HelperMethods.uncertaintyPattern.matcher(deque.get(i));

            try {
                if (fsM.matches()) {
                    try {
                        if (getVh().getReservedVariables().get(VariableHandler.variableType.FS_NODE)
                                .contains(fsM.group(1)) || isInteger(fsM.group(1)) ) {

                            if (!usedFsNodes.containsKey(fsM.group(1)) && !isInteger(fsM.group(1))) {
                                Node newNode = new Node(deque.get(i), fsM.group(1), getFsIndices(), this);
                                usedFsNodes.put(fsM.group(1),newNode);
                                queryList.add(i, newNode);
                            }
                            else
                                {
                                    queryList.add(i,usedFsNodes.get(fsM.group(1)));
                                }

                        } else {
                            throw new IllegalArgumentException("Invalid fsNode variable! (Range: #g - #n; or # + Integer)");
                        }
                    }
                    catch(IllegalArgumentException e)
                    {
                        System.out.println("Invalid fsNode variable! (Range: #f - #n)");
                        queryList = new LinkedList<QueryExpression>();
                        break;
                    }
                }else if (uM.matches())
                {

                    boolean insideOut = false;
                    if (uM.group(1).equals("^"))
                    {
                        insideOut = true;
                    }

                    queryList.add(i,new Uncertainty(uM.group(2),insideOut,getFsIndices(),this));
                }
                else if (isAttribute(deque.get(i), getFsIndices())) {
                    queryList.add(i, new Attribute(deque.get(i), getFsIndices(), this));
                } else if (isValue(deque.get(i), getFsIndices())) {
                    Boolean var = false;
                    Boolean strip = false;

                    String query = deque.get(i);
                    Matcher sm = HelperMethods.stripPattern.matcher(deque.get(i));
                    if (sm.find())
                    {
                        query = sm.group(2);
                        strip = true;
                    }

                    Matcher m = HelperMethods.valueVarPattern.matcher(query);
                    if (m.find())
                    {
                        var = true;
                    }

                    queryList.add(i, new Value(query, getFsIndices(),var,strip, this));
                } else if (deque.get(i).equals("\u0026")) {
                    queryList.add(i, new Conjunction("\u0026"));

                } else if (deque.get(i).equals("=="))
                {
                    queryList.add(i,new Equality(true,this));
                }
                else if (deque.get(i).equals("!="))
                {
                    queryList.add(i,new Equality(false, this ));
                }
                else
                    {
                    throw new IllegalArgumentException("Element not found in f-structure " + i);
                }
            } catch(IllegalArgumentException e)
            {
                System.out.println("Element \"" + ((LinkedList<String>) queryDeque).get(i) + "\" not contained in current f-structure." );
                queryList = new LinkedList<QueryExpression>();
                break;
            }
        }

        return queryList;
    }

    //Combines query terminals to queryexpressions to derive the nodes that are returned by a query

    public QueryParserResult parseQuery(LinkedList<QueryExpression> queryList)
    {

        if (!queryList.isEmpty()) {

            ListIterator<QueryExpression> it = queryList.listIterator();

            HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> result = new HashMap<>();


            QueryExpression previous = null;
            QueryExpression current = null;
            QueryExpression next = null;

            Boolean iterable = true;

            while (iterable) {

                try {
                    if (previous != null) {

                        //it.next()
                        if (current == null) {
                            current = it.next();
                        }
                        //  it.remove();

                        if (it.hasNext()) {
                            next = it.next();
                            it.previous();
                        } else {
                            next = null;
                        }

                        if (current instanceof Attribute && (previous instanceof Node ||
                                previous instanceof NodeExpression || previous instanceof UncertaintyExpression)) {

                            AttributeExpression ae = new AttributeExpression(previous, (Attribute) current);
                            it.add(ae);
                            result = ae.getSolution();


                        } else if (current instanceof Node && (previous instanceof AttributeExpression
                                || previous instanceof Attribute
                                || previous instanceof UncertaintyExpression
                                || previous instanceof ConjointExpression)) {
                            NodeExpression ne = new NodeExpression(previous, (Node) current);
                            it.add(ne);
                            result = ne.getSolution();

                        } else if (current instanceof Uncertainty && // next instanceof FsNode&&
                                (previous instanceof Node || previous instanceof NodeExpression)) {
                            UncertaintyExpression ue = new UncertaintyExpression(previous, (Uncertainty) current, next);

                            it.add(ue);
                            result = ue.getSolution();
                        } else if (current instanceof Value && (previous instanceof AttributeExpression ||
                                previous instanceof Attribute || previous instanceof UncertaintyExpression)) {
                            ValueExpression ve = new ValueExpression(previous, (Value) current);
                            it.add(ve);
                            result = ve.getSolution();
                        } else if (current instanceof Conjunction) {
                            ConjointExpression oe = new ConjointExpression(previous);
                            it.add(oe);
                            result = oe.getSolution();
                        } else if (current instanceof Value && previous instanceof ConjointExpression) {

                            current.setFsIndices(previous.getFsIndices());
                            current.setSolution(previous.getSolution());
                            //  it.next();

                        } else if (previous instanceof Value && current instanceof Equality && next instanceof Value) {
                            EqualityExpression ee =
                                    new EqualityExpression((Value) previous, (Equality) current, (Value) next);
                            it.add(ee);
                            result = ee.getSolution();
                            it.next();
                            it.remove();

                        } else {
                            throw new IllegalArgumentException("Invalid query string! Error at " + it.nextIndex());

                        }
                    } else {

                        previous = it.next();
                        result = previous.getSolution();
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("Invalid query snytax!");
                    e.printStackTrace();
                }

                if (!it.hasNext() && next == null) {
                    iterable = false;
                } else {
                    try {
                        it.previous();
                        previous = it.next();
                        current = it.next();
                    } catch (Exception e) {
                        System.out.println("Hit end of Query!");
                    }

                }

            }
            return new QueryParserResult(result,fsValueBindings);
        }
        return new QueryParserResult(new HashMap<>(),new HashMap<>());
    }






    public boolean isAttribute(String query, HashMap<Integer, GraphConstraint> fsIndices)
    {
        for (Integer key : fsIndices.keySet())
        {
            if (fsIndices.get(key).getRelationLabel().equals(query))
            {
                return true;
            }
        }

        return false;
    }


    public boolean isValue(String query, HashMap<Integer, GraphConstraint> fsIndices)
    {
        for (Integer key : fsIndices.keySet())
        {
            if (fsIndices.get(key).getFsValue().equals(query))
            {
                return true;
            }
        }

        Matcher m = HelperMethods.valueVarPattern.matcher(query);
        Matcher sm = HelperMethods.stripPattern.matcher(query);
        Matcher vm = HelperMethods.valueStringPattern.matcher(query);

        if (m.matches())
        {
            return true;
        }

        if (sm.matches())
        {
            return true;
        }

        if (vm.matches())
        {
            return true;
        }
return false;
    }


    public static boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    public void resetParser()
    {
        this.fsValueBindings = new HashMap<>();
        this.queryList = null;
    }


    public void generateQuery(String query)
    {
        Deque<String> search = new LinkedList<String>(Arrays.asList(query.split("\\s+")));

        this.queryList = generateQuery(search);
    }





    public LinkedList<QueryExpression> getQueryList() {
        return queryList;
    }

    public void setQueryList(LinkedList<QueryExpression> queryList) {
        this.queryList = queryList;
    }

    public HashMap<Integer, GraphConstraint> getFsIndices() {
        return fsIndices;
    }

    public void setFsIndices(HashMap<Integer, GraphConstraint> fsIndices) {
        this.fsIndices = fsIndices;
    }

    public VariableHandler getVh() {
        return vh;
    }

    public void setVh(VariableHandler vh) {
        this.vh = vh;
    }

    public Set<String> getUsedKeys() {
        return usedKeys;
    }

    public void setUsedKeys(Set<String> usedKeys) {
        this.usedKeys = usedKeys;
    }

}
