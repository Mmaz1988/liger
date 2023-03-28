package de.ukon.liger.analysis.graphParser;

import de.ukon.liger.analysis.QueryParser.*;
import de.ukon.liger.analysis.graphParser.LigerGraph.LigerEdge;
import de.ukon.liger.analysis.graphParser.LigerGraph.LigerGraph;
import de.ukon.liger.analysis.graphParser.queryExpressions.*;
import de.ukon.liger.analysis.graphParser.queryExpressions.Attribute;
import de.ukon.liger.analysis.graphParser.queryExpressions.Conjunction;
import de.ukon.liger.analysis.graphParser.queryExpressions.Equality;
import de.ukon.liger.analysis.graphParser.queryExpressions.Node;
import de.ukon.liger.analysis.graphParser.queryExpressions.QueryExpression;
import de.ukon.liger.analysis.graphParser.queryExpressions.Uncertainty;
import de.ukon.liger.analysis.graphParser.queryExpressions.Value;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.ud.UDoperator;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.utilities.VariableHandler;
import org.apache.juli.logging.Log;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Query;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class GraphParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(GraphParser.class);
    private VariableHandler vh = new VariableHandler();
    public static void main(String[] args) {

        UDoperator ud = new UDoperator();

        LinguisticStructure ls = ud.parseSingle("Mary used to visit her aunt");

        LigerGraph lg = LigerGraph.LinguisticStructure2Graph(ls);

        GraphParser gp = new GraphParser();

        gp.search("#a xcomp #b & #c root #d & -(#a tns #b & #c bla #b) & #a !(xcomp*>tns) #c & strip(%a) == lex(%b)", lg);

        System.out.println(lg);


    }


    public GraphParser()
    {

    }





    public LinkedList<QueryExpression> generateQuery(String query,LigerGraph lg) throws Exception {


        Set<Node> usedNodeVariables = new HashSet<>();
        Set<Value> usedValueVariables = new HashSet<>();

        LinkedList<QueryExpression> queryList = new LinkedList<>();

        boolean negation = false;
        int pos = 0;
    //    int bracketCounter = 0;

        while (pos < query.length())
        {

            while (Character.isWhitespace(query.charAt(pos)))
            {
                pos++;
            }

            char current = query.charAt(pos);

            if (current == '#') {
                pos++;
                current = query.charAt(pos);
                StringBuilder sb = new StringBuilder();
                sb.append(current);
                boolean element = false;
                pos++;
                while (!element) {
                    if (pos < query.length()) {
                        if (!Character.isWhitespace(query.charAt(pos))
                             //   && Character.isLetterOrDigit(query.charAt(pos))
                        ) {
                            if (!(query.charAt(pos) == ')')) {
                                sb.append(query.charAt(pos));
                                pos++;
                            } else
                            {
                                element = true;
                            }
                        } else {
                            element = true;
                        }
                    } else {
                        element = true;
                    }
                }

                Node node = null;

                if (!usedNodeVariables.isEmpty()) {
                    Set<Node> presentNode = usedNodeVariables.stream().filter(x -> x.name.equals("#" + sb.toString())).collect(Collectors.toSet());
                    if (!presentNode.isEmpty()) {
                        node = presentNode.stream().findAny().get();
                    }
                }

                if (node != null)
                {
                    queryList.addLast(node);
                } else {
                    Node nodeVar = new Node('#' + sb.toString(),lg);
                    queryList.addLast(nodeVar);
                    usedNodeVariables.add(nodeVar);
                }
              continue;
            }

            if (current == '%')
            {
                pos++;
                current = query.charAt(pos);
                StringBuilder sb = new StringBuilder();
                sb.append(current);
                boolean element = false;
                pos++;
                int brcounter = 0;
                while(!element) {
                    if (pos < query.length()) {
                        if (!Character.isWhitespace(query.charAt(pos))
                            //  && Character.isLetterOrDigit(query.charAt(pos))
                        ) {
                            if (!(query.charAt(pos) == ')')) {
                                sb.append(query.charAt(pos));
                                pos++;
                            } else
                            {
                             element = true;
                            }

                        } else {
                            element = true;
                        }
                    }
                    else
                    {
                        element = true;
                    }
                }

                Value node = null;

                if (!usedValueVariables.isEmpty()) {
                    Set<Value> presentNode = usedValueVariables.stream().filter(x -> x.name.equals("%" + sb.toString())).collect(Collectors.toSet());
                    if (!presentNode.isEmpty()) {
                        node = presentNode.stream().findAny().get();
                    }
                }

                if (node != null)
                {
                    queryList.addLast(node);
                } else {
                    Value valueVar = new Value('%' + sb.toString(),lg);
                    queryList.addLast(valueVar);
                    usedValueVariables.add(valueVar);
                }
                continue;
            }

                if (Character.isLetterOrDigit(current))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(current);
                    boolean element = false;
                    boolean modifiedValue = false;
                    pos++;
                    while(!element) {
                        if (pos < query.length()) {
                            if (!Character.isWhitespace(query.charAt(pos))) {

                                if (query.charAt(pos) == '(')
                                {
                                    pos++;
                                    if (query.charAt(pos) == '%')
                                    {
                                        pos++;
                                        StringBuilder sb1 = new StringBuilder();
                                        while(!(query.charAt(pos) == ')'))
                                        {
                                            sb1.append(query.charAt(pos));
                                            pos++;
                                        }

                                        Value node = null;

                                        if (!usedValueVariables.isEmpty()) {
                                            Set<Value> presentNode = usedValueVariables.stream().filter(x -> x.name.equals("%" + sb.toString())).collect(Collectors.toSet());
                                            if (!presentNode.isEmpty()) {
                                                node = presentNode.stream().findAny().get();
                                            }
                                        }

                                        if (node != null)
                                        {
                                            queryList.addLast(node);
                                        } else {
                                            Value valueVar = new Value('%' + sb1.toString(),lg,sb.toString());
                                            queryList.addLast(valueVar);
                                            usedValueVariables.add(valueVar);
                                        }
                                        continue;
                                    }
                                }

                                sb.append(query.charAt(pos));
                                pos++;


                            } else {
                                element = true;
                            }
                        }
                        else
                        {
                            element = true;
                        }
                    }



                    if (lg.values.contains(sb.toString()))
                    {
                    queryList.addLast(new Value(sb.toString(),lg));
                    } else if (lg.attributes.contains(sb.toString()) || lg.relations.contains(sb.toString()))
                    {
                        queryList.addLast(new Attribute(sb.toString(),lg));
                    } else if (negation)
                    {
                        queryList.addLast(new AttributeorValue(sb.toString(),lg));
                    }
                    pos++;
                    continue;
                }

                if (current == '&')
                {
                    queryList.add(new Conjunction("&",lg));
                    pos++;
                    continue;
                 }


                if (current == '^' || current == '!')
                {
                    if (query.charAt(pos + 1) == '(')
                    {

                        StringBuilder sb = new StringBuilder();
                        sb.append(current);
                        sb.append(query.charAt(pos + 1));
                        pos = pos + 2;

                        while (query.charAt(pos) != ')')
                        {
                            sb.append(query.charAt(pos));
                            pos++;
                        }
                        sb.append(query.charAt(pos));

                        queryList.addLast(new Uncertainty(sb.toString()));
                        pos++;
                        continue;
                    }
                }

            if (current == '-')
            {
                if (query.charAt(pos + 1) == '(')
                {
                    pos++;
                    queryList.addLast(new Negation("start",lg));
                    pos++;
                    negation = true;
                    continue;

                }
            }

            if (current == ')')
            {
                queryList.addLast(new Negation("end",lg));
                pos++;
                negation = false;
                continue;
            }

            if ((current == '=' || current == '!' ) && query.charAt(pos+1) == '=')
            {
                if (current == '=') {
                    queryList.addLast(new Equality("equal",lg));
                } else
                {
                    queryList.addLast(new Equality("unequal",lg));
                }
                pos = pos + 2;
            }

                }







     //   Deque<String> search = new LinkedList<String>(Arrays.asList(query.split("\\s+")));

      return queryList;
    }


    public Object search(String query, LigerGraph lg){




        try {
            LinkedList<QueryExpression> parseStructure = generateQuery(query,lg);

            if (parseStructure == null)
            {
                LOGGER.info("Query does not match graph.");
            }


            boolean negation;

            for (int i = 0; i < parseStructure.size(); i++)
            {
                QueryExpression current = parseStructure.get(i);


                if (current instanceof Attribute)
                {
                    i++;
                    if (parseStructure.get(i) instanceof Node)
                    {

                    }
                } else if (current instanceof Node)
                {
                    i++;
                    if (parseStructure.get(i) instanceof Attribute)
                    {

                    }
                }



            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }




    /*
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
                else if (HelperMethods.isAttribute(deque.get(i), getFsIndices())) {
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
    */


}
