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

package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.utilities.VariableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;

public class QueryParser {

    //TODO combine with GUI (set gui as field, to throw exceptions inside GUI
    private String query;
    private LinkedList<QueryExpression> queryList;
    private HashMap<Integer, GraphConstraint> fsIndices;
    private VariableHandler vh = new VariableHandler();
    private Set<String> usedKeys = new HashSet<>();
    public HashMap<Set<SolutionKey>, HashMap<String,String>> fsValueBindings = new HashMap<>();
    private final static Logger LOGGER = LoggerFactory.getLogger(QueryParser.class);
    public ChoiceSpace cp;


    //TODO why are the values of the result hashmap empty?

    public QueryParser(String query, LinguisticStructure fs)
    {
        this.query = query;

        HashMap<Integer,GraphConstraint> fsIndexed = new HashMap<>();

        for (int i = 0; i < fs.constraints.size();i++)
        {
            usedKeys.add(fs.constraints.get(i).getFsNode());
            fsIndexed.put(i,fs.constraints.get(i));
        }
        if (!fs.annotation.isEmpty()) {
            for (int i = 0; i < fs.annotation.size(); i++) {
                int j = i + fs.constraints.size();
                usedKeys.add(fs.annotation.get(i).getFsNode());
                fsIndexed.put(j, fs.annotation.get(i));
            }
        }
        //Part 1, String to QueryExpression element

        this.fsIndices = fsIndexed;
        this.cp = fs.cp;
        generateQuery(query);

    }

    public QueryParser(LinguisticStructure fs)
    {

        HashMap<Integer,GraphConstraint> fsIndexed = new HashMap<>();

        for (int i = 0; i < fs.constraints.size();i++)
        {
            usedKeys.add(fs.constraints.get(i).getFsNode());
            fsIndexed.put(i,fs.constraints.get(i));
        }
        if (!fs.annotation.isEmpty()) {
            for (int i = 0; i < fs.annotation.size(); i++) {
                int j = i + fs.constraints.size();
                usedKeys.add(fs.annotation.get(i).getFsNode());
                fsIndexed.put(j, fs.annotation.get(i));
            }
        }
        //Part 1, String to QueryExpression element
    this.fsIndices = fsIndexed;
        this.cp = fs.cp;
    }

/*

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
 */

    public LinkedList<QueryExpression> generateQueryList(Deque<String> queryDeque){

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
                        LOGGER.warn("Invalid fsNode variable! (Range: #f - #n)");
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
                LOGGER.trace("Element \"" + ((LinkedList<String>) queryDeque).get(i) + "\" not contained in current f-structure." );
                return new LinkedList<QueryExpression>();
            }
        }

        queryList.add(new End());

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
                    } else if (current instanceof End)
                    {
                        EndExpression ee = new EndExpression(previous);
                        result = ee.getSolution();
                    }
                        else if (previous instanceof Value && current instanceof Equality && next instanceof Value) {
                            EqualityExpression ee =
                                    new EqualityExpression((Value) previous, (Equality) current, (Value) next);
                            it.add(ee);
                            result = ee.getSolution();
                            it.next();
                            it.remove();
                        }


                        else {
                            throw new IllegalArgumentException("Invalid query string! Error at " + it.nextIndex());

                        }
                    } else {

                        previous = it.next();
                        result = previous.getSolution();
                        continue;
                    }
                } catch (Exception e) {
                    LOGGER.error("Invalid query snytax!", e);
                   // e.printStackTrace();
                }

                if (!it.hasNext() && next == null) {
                    iterable = false;
                } else {
                    try {
                        it.previous();
                        previous = it.next();
                        current = it.next();
                    } catch (Exception e) {
                       LOGGER.trace("Hit end of Query!");
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
        this.query = null;
    }


    public void generateQuery(String query)
    {
        Deque<String> search = new LinkedList<String>(Arrays.asList(query.split("\\s+")));

        this.queryList = generateQueryList(search);
    }

    public void generateQuery()
    {

        Deque<String> search = new LinkedList<String>(Arrays.asList(query.split("\\s+")));

        this.queryList = generateQueryList(search);
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
