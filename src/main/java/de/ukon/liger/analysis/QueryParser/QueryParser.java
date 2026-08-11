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
    public HashMap<Solution, HashMap<String,String>> fsValueBindings = new HashMap<>();
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

    public QueryParser(String query, HashMap<Integer, GraphConstraint> fsIndices, ChoiceSpace cp,
                       TemplateRegistry templateRegistry, HierarchyRegistry hierarchyRegistry) {
        this.query = query;
        this.fsIndices = fsIndices;
        this.cp = cp;
        this.templateRegistry = templateRegistry;
        this.hierarchyRegistry = hierarchyRegistry;

        for (Integer key : fsIndices.keySet()) {
            usedKeys.add(fsIndices.get(key).getFsNode());
        }

        generateQuery(query);
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
        return generateQueryList(queryDeque, true);
    }

    private LinkedList<QueryExpression> generateQueryList(Deque<String> queryDeque, boolean addFinalEnd){

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
                                 .contains(fsM.group(1)) || HelperMethods.isNodeReference(fsM.group(1)) ) {

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
                    queryList.add(new QueryNegation(innerQuery,
                            generateQueryList(new LinkedList<>(tokenizeQuery(innerQuery)), false),
                            getFsIndices(),
                            this));
                }
                else if (isAttribute(currentToken, getFsIndices())) {
                    queryList.add(new Attribute(currentToken, getFsIndices(), this));
                } else if (isSuperiorToken(currentToken)) {
                    queryList.add(new Superior(currentToken, getFsIndices(), this));
                } else if (currentToken.startsWith("id(")) {
                    if (!HelperMethods.isIdExpression(currentToken)) {
                        throw new IllegalStateException("Invalid id() expression: " + currentToken);
                    }

                    Matcher idMatcher = HelperMethods.idPattern.matcher(currentToken);
                    if (!idMatcher.matches()) {
                        throw new IllegalStateException("Invalid id() expression: " + currentToken);
                    }

                    Value idValue = new Value(currentToken, getFsIndices(), false, false, this);
                    idValue.setIdRef(true);
                    idValue.setIdVar(idMatcher.group(1));
                    queryList.add(idValue);
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
                else if (currentToken.equals("<")) {
                    queryList.add(new Comparison(Comparison.Operator.LT, this));
                }
                else if (currentToken.equals(">")) {
                    queryList.add(new Comparison(Comparison.Operator.GT, this));
                }
                else if (currentToken.equals("<=")) {
                    queryList.add(new Comparison(Comparison.Operator.LE, this));
                }
                else if (currentToken.equals(">=")) {
                    queryList.add(new Comparison(Comparison.Operator.GE, this));
                }
                else
                    {
                    throw new IllegalArgumentException("Element not found in f-structure " + i);
                }
            } catch(IllegalArgumentException e)
            {
                String invalidToken = ((LinkedList<String>) queryDeque).get(i);
                if (invalidToken.startsWith("@")) {
                    // Template-bearing constructors may perform an initial parse
                    // before their registry is attached.
                    LOGGER.debug("Deferred template token '{}' at position {}", invalidToken, i);
                } else {
                    LOGGER.warn("Query token '{}' at position {} could not be resolved against the current structure",
                            invalidToken, i);
                }
                return new LinkedList<QueryExpression>();
            }
        }

        if (addFinalEnd) {
            queryList.add(new End());
        }

        return queryList;
    }

    //Combines query terminals to queryexpressions to derive the nodes that are returned by a query

    public QueryParserResult parseQuery(LinkedList<QueryExpression> queryList)
    {
        return parseQuery(queryList, null);
    }

    public QueryParserResult parseQuery(LinkedList<QueryExpression> queryList,
                                        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> seedSolution)
    {
        if (isStandaloneFilterQuery(queryList)) {
            return parseStandaloneFilterQuery(queryList);
        }

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

            LinkedList<QueryExpression> workingQuery = new LinkedList<>(structuralQuery);
            if (seedSolution != null) {
                workingQuery.addFirst(new SeedExpression(seedSolution, this));
            }

            LinkedList<QueryExpression> parsedQueryList = workingQuery;

            ListIterator<QueryExpression> it = parsedQueryList.listIterator();

            HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> result = new HashMap<>();


            QueryExpression previous = null;
            QueryExpression current = null;
            QueryExpression next = null;

            Boolean iterable = true;

            while (iterable) {

                try {
                    if (previous != null) {

                        //it.next()
                        if (current == null) {
                            if (!it.hasNext()) {
                                iterable = false;
                                continue;
                            }
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
                                previous instanceof SeedExpression)) {

                            AttributeExpression ae = new AttributeExpression(previous, (Attribute) current);
                            it.add(ae);
                            result = ae.getSolution();


                        } else if (current instanceof Node && (previous instanceof AttributeExpression
                                || previous instanceof Attribute
                                || previous instanceof UncertaintyExpression
                                || previous instanceof ConjointExpression
                                || previous instanceof SeedExpression)) {
                            NodeExpression ne = new NodeExpression(previous, (Node) current);
                            it.add(ne);
                            result = ne.getSolution();

                        } else if (current instanceof Uncertainty && // next instanceof FsNode&&
                                (previous instanceof Node || previous instanceof NodeExpression ||
                                        previous instanceof SeedExpression)) {
                            UncertaintyExpression ue = new UncertaintyExpression(previous, (Uncertainty) current, next);

                            it.add(ue);
                            result = ue.getSolution();
                        } else if (current instanceof Value && (previous instanceof AttributeExpression ||
                                previous instanceof Attribute || previous instanceof UncertaintyExpression ||
                                previous instanceof SeedExpression)) {
                            ValueExpression ve = new ValueExpression(previous, (Value) current);
                            it.add(ve);
                            result = ve.getSolution();
                        } else if (current instanceof Conjunction) {
                            ConjointExpression oe = new ConjointExpression(previous);
                            it.add(oe);
                            result = oe.getSolution();
                        } else if (current instanceof NegationExpression) {
                            result = current.getSolution();
                        } else if (current instanceof QueryNegation) {
                            NegationExpression ne = new NegationExpression(previous, (QueryNegation) current);
                            it.set(ne);
                            result = ne.getSolution();
                            previous = ne;
                            current = null;
                            continue;
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
                        else if (previous instanceof Value && current instanceof Comparison && next instanceof Value) {
                            ComparisonExpression ce = new ComparisonExpression((Value) previous, (Comparison) current, (Value) next);
                            it.add(ce);
                            result = ce.getSolution();
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
                            it.previous();
                            it.set(ne);
                            it.next();
                            previous = ne;
                            result = ne.getSolution();
                            continue;
                        }
                        result = previous.getSolution();
                        continue;
                    }
                } catch (Exception e) {
                    LOGGER.error("Invalid query syntax while evaluating '{}': {}", query, e.getMessage(), e);
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

            HashMap<Solution, HashMap<String, String>> filteredValueBindings = new HashMap<>();
            for (Solution key : result.keySet()) {
                HashMap<String, String> bindings = fsValueBindings.get(key);
                if (bindings != null) {
                    filteredValueBindings.put(key, new HashMap<>(bindings));
                }
            }

            return new QueryParserResult(result, filteredValueBindings);
        }
        return new QueryParserResult(new HashMap<>(),new HashMap<>());
    }

    private boolean isStandaloneFilterQuery(LinkedList<QueryExpression> queryList) {
        if (queryList == null || queryList.isEmpty()) {
            return false;
        }

        for (QueryExpression expression : queryList) {
            if (expression instanceof End || expression instanceof Conjunction) {
                continue;
            }
            if (expression instanceof Attribute || expression instanceof Value) {
                continue;
            }
            return false;
        }

        return true;
    }

    private QueryParserResult parseStandaloneFilterQuery(LinkedList<QueryExpression> queryList) {
        HashSet<Integer> candidateIndices = new HashSet<>(fsIndices.keySet());

        for (QueryExpression expression : queryList) {
            if (expression instanceof End || expression instanceof Conjunction) {
                continue;
            }

            if (expression instanceof Attribute attribute) {
                String lookup = normalizeStandaloneAttribute(attribute.getQuery());
                candidateIndices.removeIf(index -> {
                    GraphConstraint constraint = fsIndices.get(index);
                    return constraint == null || !lookup.equals(normalizeStandaloneAttribute(constraint.getRelationLabel()));
                });
                continue;
            }

            if (expression instanceof Value value) {
                String lookup = normalizeStandaloneValue(value.getQuery());
                candidateIndices.removeIf(index -> {
                    GraphConstraint constraint = fsIndices.get(index);
                    return constraint == null || !lookup.equals(normalizeStandaloneValue(String.valueOf(constraint.getFsValue())));
                });
            }
        }

        if (candidateIndices.isEmpty()) {
            return new QueryParserResult(new HashMap<>(), new HashMap<>());
        }

        HashMap<Integer, GraphConstraint> matches = new LinkedHashMap<>();
        for (Integer index : candidateIndices) {
            matches.put(index, fsIndices.get(index));
        }

        HashMap<String, HashMap<Integer, GraphConstraint>> reference = new LinkedHashMap<>();
        reference.put("__match__", matches);

        HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = new LinkedHashMap<>();
        binding.put("__match__", reference);

        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> result = new LinkedHashMap<>();
        result.put(new Solution(Collections.singleton(new SolutionKey("__match__", "__match__"))), binding);

        return new QueryParserResult(result, new HashMap<>());
    }

    private String normalizeStandaloneAttribute(String token) {
        if (token == null) {
            return "";
        }

        String normalized = token.trim();
        if (normalized.startsWith("edge=")) {
            normalized = normalized.substring("edge=".length());
        }
        return normalized;
    }

    private String normalizeStandaloneValue(String token) {
        if (token == null) {
            return "";
        }

        String normalized = token.trim();
        if (normalized.startsWith("value=")) {
            normalized = normalized.substring("value=".length());
        }
        if ((normalized.startsWith("'") && normalized.endsWith("'")) ||
                (normalized.startsWith("\"") && normalized.endsWith("\""))) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }






    public boolean isAttribute(String query, HashMap<Integer, GraphConstraint> fsIndices)
    {
        if (query != null && query.matches("edge=(.+)")) {
            return true;
        }

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
        return parseQueryWithTemplates(query, null);
    }

    public List<QueryParserResult> parseQueryWithTemplates(String query,
                                                           HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> seedSolution)
    {
        if (templateRegistry == null || templateRegistry.getTemplates().isEmpty()) {
            generateQuery(query);
            return Collections.singletonList(parseQuery(getQueryList(), seedSolution));
        }

        boolean hasTemplateInvocation = tokenizeQuery(query).stream().anyMatch(token -> token != null && token.startsWith("@"));
        if (!hasTemplateInvocation) {
            // Not an event: most queries don't invoke a template, and this runs once
            // per rule per branch. Kept at DEBUG only because it answers "why wasn't
            // my @template expanded?" when someone goes looking.
            LOGGER.debug("Template registry present, but query contains no template invocation. Skipping expansion for query='{}'", query);
            generateQuery(query);
            return Collections.singletonList(parseQuery(getQueryList(), seedSolution));
        }

        List<List<String>> expandedQueries = TemplateExpander.expandQuery(query, templateRegistry);
        LOGGER.debug("Expanding query against template registry: originalQuery='{}', expansionCount={}",
                query, expandedQueries.size());
        List<QueryParserResult> results = new ArrayList<>();

        for (List<String> expandedQuery : expandedQueries) {
            LinkedList<String> tokens = new LinkedList<>(expandedQuery);
            LinkedList<QueryExpression> expandedQueryList = generateQueryList(tokens);
            results.add(parseQuery(expandedQueryList, seedSolution));
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
                if (parenDepth == 0) {
                    String message = "Unmatched ')' in query: " + query;
                    LOGGER.error(message);
                    throw new IllegalArgumentException(message);
                }
                parenDepth--;
            }

            current.append(c);
        }

        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }

        if (inQuote) {
            String message = "Unterminated quote in query: " + query;
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
        if (parenDepth != 0) {
            String message = "Unmatched '(' in query: " + query;
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
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

    private HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> applySuperiorConstraints(
            HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> result) {

        if (superiorConstraints.isEmpty() || result.isEmpty()) {
            return result;
        }

        HashMap<Solution, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> filtered = new HashMap<>();

        for (Solution solutionKey : result.keySet()) {
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

    private String resolveBindingReference(Solution solutionKey, String variable) {
        String normalizedVariable = variable.startsWith("#") ? variable.substring(1) : variable;
        for (SolutionKey key : solutionKey.getSolutionKeys()) {
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
            if (HelperMethods.nodeIdsEqual(nodeRef, String.valueOf(constraint.getFsValue()))
                    && hierarchyRegistry.getHierarchy(hierarchyName).contains(constraint.getRelationLabel())) {
                return constraint.getRelationLabel();
            }
        }

        return null;
    }

    private boolean isSuperiorToken(String token) {
        return token.startsWith("superior(") && token.endsWith(")");
    }

}
