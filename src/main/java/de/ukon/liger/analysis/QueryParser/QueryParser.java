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
import java.util.regex.Pattern;

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
    private TemplateRegistry templateRegistry;
    private HierarchyRegistry hierarchyRegistry;
    private List<Superior> superiorConstraints = new ArrayList<>();


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

    public QueryParser(String query, LinguisticStructure fs, TemplateRegistry templateRegistry)
    {
        this(query, fs);
        this.templateRegistry = templateRegistry;
        generateQuery(query);
    }

    public QueryParser(String query, LinguisticStructure fs, HierarchyRegistry hierarchyRegistry)
    {
        this(query, fs);
        this.hierarchyRegistry = hierarchyRegistry;
        generateQuery(query);
    }

    public QueryParser(String query, LinguisticStructure fs, TemplateRegistry templateRegistry,
                       HierarchyRegistry hierarchyRegistry)
    {
        this(query, fs);
        this.templateRegistry = templateRegistry;
        this.hierarchyRegistry = hierarchyRegistry;
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

    public QueryParser(LinguisticStructure fs, TemplateRegistry templateRegistry, HierarchyRegistry hierarchyRegistry)
    {
        this(fs);
        this.templateRegistry = templateRegistry;
        this.hierarchyRegistry = hierarchyRegistry;
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
            String currentToken = deque.get(i);
            Matcher fsM =  HelperMethods.fsNodePattern.matcher(currentToken);
            Matcher uM = HelperMethods.uncertaintyPattern.matcher(currentToken);

            try {
                if (fsM.matches()) {
                    try {
                        if (getVh().getReservedVariables().get(VariableHandler.variableType.FS_NODE)
                                .contains(fsM.group(1)) || HelperMethods.isInteger(fsM.group(1)) ) {

                            if (!usedFsNodes.containsKey(fsM.group(1))) {
                                //if the first symbol of fsm.group(1) is * set boolean to true
                                boolean constant = false;
                            if (currentToken.charAt(0) == '*') {
                                    constant = true;
                                }

                                Node newNode = new Node(currentToken, fsM.group(1), getFsIndices(), constant, this);
                                usedFsNodes.put(fsM.group(1),newNode);
                                queryList.add(newNode);
                            }
                            else
                                {
                                    queryList.add(usedFsNodes.get(fsM.group(1)));
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

                    queryList.add(new Uncertainty(uM.group(2),insideOut,getFsIndices(),this, templateRegistry));
                }
                else if (isNegationToken(currentToken)) {
                    String innerQuery = extractWrappedQuery(currentToken);
                    LinkedList<QueryExpression> innerQueryList = generateQueryList(new LinkedList<>(tokenizeQuery(innerQuery)));
                    queryList.add(new QueryNegation(innerQuery, innerQueryList, getFsIndices(), this));
                }
                else if (isAttribute(currentToken, getFsIndices())) {
                    queryList.add(new Attribute(currentToken, getFsIndices(), this));
                } else if (isSuperiorToken(currentToken)) {
                    queryList.add(new Superior(currentToken, getFsIndices(), this));
                } else if (HelperMethods.isValue(currentToken, getFsIndices())) {
                    Boolean var = false;
                    Boolean strip = false;

                    String query = currentToken;

                    //if value is wrapped in quotes, strip the quotes
                    //e.g. "value" or 'value'
                    Pattern quotePattern = Pattern.compile("[\"'](.*)[\"']");
                    Matcher quoteMatcher = quotePattern.matcher(query);
                    if (quoteMatcher.find())
                    {
                        query = quoteMatcher.group(1);
                    }

                    Matcher sm = HelperMethods.stripPattern.matcher(currentToken);
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

                    queryList.add(new Value(query, getFsIndices(),var,strip, this));
                } else if (currentToken.equals("\u0026")) {
                    queryList.add(new Conjunction("\u0026"));

                } else if (currentToken.equals("=="))
                {
                    queryList.add(new Equality(true,this));
                }
                else if (currentToken.equals("!="))
                {
                    queryList.add(new Equality(false, this ));
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

            List<QueryExpression> structuralQuery = new ArrayList<>();
            this.superiorConstraints = new ArrayList<>();

            for (QueryExpression expression : queryList) {
                if (expression instanceof Superior) {
                    addSuperiorConstraint((Superior) expression);
                    continue;
                }

                structuralQuery.add(expression);
            }

            LinkedList<QueryExpression> parsedQueryList = new LinkedList<>(structuralQuery);

            ListIterator<QueryExpression> it = parsedQueryList.listIterator();

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
                                previous instanceof NodeExpression || previous instanceof UncertaintyExpression ||
                                previous instanceof NegationExpression)) {

                            AttributeExpression ae = new AttributeExpression(previous, (Attribute) current);
                            it.add(ae);
                            result = ae.getSolution();


                        } else if (current instanceof Node && (previous instanceof AttributeExpression
                                || previous instanceof Attribute
                                || previous instanceof UncertaintyExpression
                                || previous instanceof ConjointExpression
                                || previous instanceof NegationExpression)) {
                            NodeExpression ne = new NodeExpression(previous, (Node) current);
                            it.add(ne);
                            result = ne.getSolution();

                        } else if (current instanceof Uncertainty && // next instanceof FsNode&&
                                (previous instanceof Node || previous instanceof NodeExpression ||
                                        previous instanceof NegationExpression)) {
                            UncertaintyExpression ue = new UncertaintyExpression(previous, (Uncertainty) current, next);

                            it.add(ue);
                            result = ue.getSolution();
                        } else if (current instanceof Value && (previous instanceof AttributeExpression ||
                                previous instanceof Attribute || previous instanceof UncertaintyExpression ||
                                previous instanceof NegationExpression)) {
                            ValueExpression ve = new ValueExpression(previous, (Value) current);
                            it.add(ve);
                            result = ve.getSolution();
                        } else if (current instanceof Conjunction) {
                            ConjointExpression oe = new ConjointExpression(previous);
                            it.add(oe);
                            result = oe.getSolution();
                        } else if (current instanceof QueryNegation && previous == null) {
                            NegationExpression ne = new NegationExpression(null, (QueryNegation) current);
                            it.add(ne);
                            result = ne.getSolution();
                        } else if (current instanceof QueryNegation) {
                            NegationExpression ne = new NegationExpression(previous, (QueryNegation) current);
                            it.add(ne);
                            result = ne.getSolution();
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
                            throw new IllegalArgumentException("Invalid query string! Error at " + it.nextIndex() + " : " + current.toString());

                        }
                    } else {

                        previous = it.next();
                        if (previous instanceof QueryNegation) {
                            NegationExpression ne = new NegationExpression(null, (QueryNegation) previous);
                            it.add(ne);
                            previous = ne;
                            result = ne.getSolution();
                        } else {
                            result = previous.getSolution();
                        }
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
            if (!superiorConstraints.isEmpty()) {
                result = applySuperiorConstraints(result);
            }

            HashMap<Set<SolutionKey>, HashMap<String, String>> filteredValueBindings = new HashMap<>();
            for (Set<SolutionKey> key : result.keySet()) {
                HashMap<String, String> bindings = fsValueBindings.get(key);
                if (bindings != null) {
                    filteredValueBindings.put(key, new HashMap<>(bindings));
                }
            }

            return new QueryParserResult(result, filteredValueBindings);
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


    public void resetParser()
    {
        this.fsValueBindings = new HashMap<>();
        this.queryList = null;
        this.query = null;
        this.superiorConstraints = new ArrayList<>();
    }


    public void generateQuery(String query)
    {
        superiorConstraints = new ArrayList<>();
        Deque<String> search = new LinkedList<>(tokenizeQuery(query));

        this.queryList = generateQueryList(search);
    }

    public List<QueryParserResult> parseQueryWithTemplates(String query)
    {
        if (templateRegistry == null || templateRegistry.getTemplates().isEmpty()) {
            generateQuery(query);
            return Collections.singletonList(parseQuery(getQueryList()));
        }

        List<List<String>> expandedQueries = TemplateExpander.expandQuery(query, templateRegistry);
        List<QueryParserResult> results = new ArrayList<>();

        for (List<String> expandedQuery : expandedQueries) {
            LinkedList<String> tokens = new LinkedList<>(expandedQuery);
            LinkedList<QueryExpression> expandedQueryList = generateQueryList(tokens);
            results.add(parseQuery(expandedQueryList));
        }

        return results;
    }

    public static List<String> tokenizeQuery(String query) {
        List<String> tokens = new ArrayList<>();

        if (query == null || query.isBlank()) {
            return tokens;
        }

        StringBuilder current = new StringBuilder();
        int parenDepth = 0;
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);

            if (inQuote) {
                current.append(c);
                if (c == quoteChar) {
                    inQuote = false;
                }
                continue;
            }

            if (c == '\'' || c == '"') {
                inQuote = true;
                quoteChar = c;
                current.append(c);
                continue;
            }

            if (Character.isWhitespace(c) && parenDepth == 0) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            if (c == '&' && parenDepth == 0) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                tokens.add("&");
                continue;
            }

            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth = Math.max(0, parenDepth - 1);
            }

            current.append(c);
        }

        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    private boolean isNegationToken(String token) {
        return token.startsWith("-(") && token.endsWith(")");
    }

    private String extractWrappedQuery(String token) {
        if (token.length() < 3) {
            return "";
        }
        return token.substring(2, token.length() - 1).trim();
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

    public TemplateRegistry getTemplateRegistry() {
        return templateRegistry;
    }

    public HierarchyRegistry getHierarchyRegistry() {
        return hierarchyRegistry;
    }

    public void addSuperiorConstraint(Superior superior) {
        superiorConstraints.add(superior);
    }

    public List<Superior> getSuperiorConstraints() {
        return superiorConstraints;
    }

    public void setSuperiorConstraints(List<Superior> superiorConstraints) {
        this.superiorConstraints = superiorConstraints;
    }

    private HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> applySuperiorConstraints(
            HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> result) {

        if (superiorConstraints.isEmpty() || result.isEmpty()) {
            return result;
        }

        HashMap<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> filtered = new HashMap<>();

        for (Set<SolutionKey> solutionKey : result.keySet()) {
            boolean valid = true;
            for (Superior superior : superiorConstraints) {
                String superiorRef = resolveBindingReference(solutionKey, superior.getSuperiorVar());
                String inferiorRef = resolveBindingReference(solutionKey, superior.getInferiorVar());

                if (superiorRef == null || inferiorRef == null) {
                    throw new IllegalArgumentException(
                            "superior(" + superior.getHierarchyName() + "," + superior.getSuperiorVar() + "," + superior.getInferiorVar() + ") requires bound variables");
                }

                String superiorLabel = resolveHierarchyLabel(superior.getHierarchyName(), superiorRef);
                String inferiorLabel = resolveHierarchyLabel(superior.getHierarchyName(), inferiorRef);

                if (superiorLabel == null || inferiorLabel == null) {
                    throw new IllegalArgumentException(
                            "superior(" + superior.getHierarchyName() + "," + superior.getSuperiorVar() + "," + superior.getInferiorVar() + ") references nodes that are not present in the hierarchy");
                }

                if (!hierarchyRegistry.isSuperior(superior.getHierarchyName(), superiorLabel, inferiorLabel)) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                filtered.put(solutionKey, result.get(solutionKey));
            }
        }

        return filtered;
    }

    private String resolveBindingReference(Set<SolutionKey> solutionKey, String variable) {
        String normalizedVariable = variable.startsWith("#") ? variable.substring(1) : variable;
        for (SolutionKey key : solutionKey) {
            if (normalizedVariable.equals(key.variable) || variable.equals(key.variable)) {
                return key.reference;
            }
        }
        return null;
    }

    private String resolveHierarchyLabel(String hierarchyName, String nodeRef) {
        if (hierarchyRegistry == null || !hierarchyRegistry.contains(hierarchyName)) {
            return null;
        }

        for (Integer key : fsIndices.keySet()) {
            GraphConstraint constraint = fsIndices.get(key);
            if (nodeRef.equals(constraint.getFsValue()) && hierarchyRegistry.getHierarchy(hierarchyName).contains(constraint.getRelationLabel())) {
                return constraint.getRelationLabel();
            }
        }

        return null;
    }

    private boolean isSuperiorToken(String token) {
        return token.startsWith("superior(") && token.endsWith(")");
    }

}
