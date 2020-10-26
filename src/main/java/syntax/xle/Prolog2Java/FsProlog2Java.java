package syntax.xle.Prolog2Java;

import syntax.xle.FstructureElements.*;
import utilities.HelperMethods;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// This Object contains a linked hash map f-structure (following Brubeck Unhammer)

public class FsProlog2Java {


    public static Pattern ambiguities = Pattern.compile("cf\\((.+?),\\w+\\(");
    public static Pattern keys = Pattern.compile("var\\((\\d+)\\)");
    public static Pattern preds = Pattern.compile("attr\\(var\\((\\d+)\\),('PRED')\\),(semform\\(.*\\[.*\\],\\[.*\\]\\))");
    public static Pattern adjuncts = Pattern.compile("attr\\(var\\((\\d+)\\),'ADJUNCT'\\),var\\((\\d+)\\)");
    public static Pattern inSet = Pattern.compile("in_set\\((.*),var\\((.*?)\\)\\)");
    public static Pattern nonTerminals = Pattern.compile("attr\\(var\\((\\d+)\\),('.*')\\),var\\((\\d+)\\)");
    public static Pattern terminals = Pattern.compile("attr\\(var\\((\\d+)\\),('.*')\\),('.*')");
    public static Pattern cstructure = Pattern.compile("(semform_data|surfaceform)\\((.+?),(.+?),(.+?),(.+?)\\)");


    public ReadFsProlog In;
    //public LinkedHashMap<Integer, List<Object>> FsHash;


    public FsProlog2Java(ReadFsProlog input)
    {
        this.In = input;
    }

    public static  LinkedHashMap<String,LinkedHashMap<Integer, List<AttributeValuePair>>> fs2Hash(ReadFsProlog plFs) {
        // This method creates a hashmap from prolog input

        List<String> constraints = plFs.prolog;
        LinkedHashMap<String, LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash =
                new LinkedHashMap<String, LinkedHashMap<Integer, List<AttributeValuePair>>>();

        //Patterns for different kinds of f-structure constraints


        for (String constraint : constraints) {
            Matcher ambMatcher = ambiguities.matcher(constraint);
            Matcher keyMatcher = keys.matcher(constraint);
            Matcher predsMatcher = preds.matcher(constraint);
            Matcher adjunctMatcher = adjuncts.matcher(constraint);
            Matcher setMatcher = inSet.matcher(constraint);
            Matcher nonTerminalMatcher = nonTerminals.matcher(constraint);
            Matcher terminalsMatcher = terminals.matcher(constraint);
            Matcher cstructureMatcher = cstructure.matcher(constraint);



            if (ambMatcher.find()) {
                String key = ambMatcher.group(1);
                if (!fsHash.containsKey(key)) {
                    LinkedHashMap<Integer, List<AttributeValuePair>> fsConstraints = new LinkedHashMap<>();
                    fsHash.put(key, fsConstraints);
                }
            }
            else {
                System.out.println(constraint + " threw an error");
            }

            // Collects keys for hashMap
            while (keyMatcher.find()) {
                Integer var = Integer.parseInt(keyMatcher.group(1));
                if (!fsHash.get(ambMatcher.group(1)).containsKey(var)) {
                    Integer key = var;
                    List<AttributeValuePair> values = new ArrayList<AttributeValuePair>();
                    fsHash.get(ambMatcher.group(1)).put(key, values);
                }
            }

            //Processes preds
            if (predsMatcher.find()) {
                Integer key = Integer.parseInt(predsMatcher.group(1));
                List<AttributeValuePair> values = fsHash.get(ambMatcher.group(1)).get(key);
                PredAVP avp = new PredAVP(predsMatcher.group(2), predsMatcher.group(3));
                values.add(avp);
                continue;
            }

            //Processes adjuncts
            if (adjunctMatcher.find()) {

                List<AttributeValuePair> values =
                        fsHash.get(ambMatcher.group(1)).get(Integer.parseInt(adjunctMatcher.group(1)));
                Adjunct avp = new Adjunct(adjunctMatcher.group(2));
                values.add(avp);
                continue;
            }

            //Processes non-terminal nodes
            if (nonTerminalMatcher.find()) {
                Integer key = Integer.parseInt(nonTerminalMatcher.group(1));
                List<AttributeValuePair> values = fsHash.get(ambMatcher.group(1)).get(key);
                NonTerminalAVP avp = new NonTerminalAVP(nonTerminalMatcher.group(2), nonTerminalMatcher.group(3));
                values.add(avp);
                continue;
            }


            if (setMatcher.find()) {
                Integer key = Integer.parseInt(setMatcher.group(2));
                List<AttributeValuePair> values = fsHash.get(ambMatcher.group(1)).get(key);
                AdjunctSet avp = new AdjunctSet(setMatcher.group(1));
                values.add(avp);
                continue;
            }


            // Processes terminal nodes in the f-structure
            if (terminalsMatcher.find()) {
                Integer key = Integer.parseInt(terminalsMatcher.group(1));
                List<AttributeValuePair> values = fsHash.get(ambMatcher.group(1)).get(key);
                TerminalAVP avp = new TerminalAVP(terminalsMatcher.group(2), terminalsMatcher.group(3));
                values.add(avp);
                continue;
            }

            if (cstructureMatcher.find())
            {
                if (!fsHash.get(ambMatcher.group(1)).containsKey(-1))
                {
                    fsHash.get(ambMatcher.group(1)).put(-1,new ArrayList<AttributeValuePair>());
                }
                else {
                    List<AttributeValuePair> values = fsHash.get(ambMatcher.group(1)).get(-1);
                    CsCorrespondence csc = new CsCorrespondence(cstructureMatcher.group(1),
                            Arrays.asList(cstructureMatcher.group(2),
                                            cstructureMatcher.group(3),
                                            cstructureMatcher.group(4),
                                            cstructureMatcher.group(5)));
                    values.add(csc);
                }
            }


        }

        // Debug print fsHash

        for (String key1 : fsHash.keySet()) {
            for (Integer key : fsHash.get(key1).keySet()) {
                String keyO = key.toString();
                String value = fsHash.get(key1).get(key).toString();
                System.out.println(key1 + " " + keyO + " " + value);
            }
        }

        return fsHash;
    }


    public static List<GraphConstraint> fsHash2List(
            LinkedHashMap<String,
                    LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash)
    {

        Pattern string = Pattern.compile("'(.*)'");


        List<GraphConstraint> out = new ArrayList<GraphConstraint>();

        for(String ambKey : fsHash.keySet())

        {

            for (Integer key : fsHash.get(ambKey).keySet())
            {
                for (AttributeValuePair avp : fsHash.get(ambKey).get(key))
                {
                    String attribute = avp.attribute;
                    String value = avp.value;

                    Matcher am = string.matcher(avp.attribute);
                    Matcher vm = string.matcher(avp.value);

                    Set<Integer> pathNodes = new HashSet<>();
                    pathNodes.add(key);

                    //pathNodes = returnPathNodes(fsHash,key,ambKey,pathNodes);


                    if (am.matches())
                    {
                        attribute = am.group(1);
                    }

                    if (vm.matches())
                    {
                        if (HelperMethods.isInteger(value)) {
                            value = vm.group(1);
                        }
                    }

                    out.add(new GraphConstraint(ambKey,key,attribute,value));
                }


            }
        }

        return out;
    }



    // deletes entries in the fsHash that point at nothing
    public static LinkedHashMap<Integer,List<AttributeValuePair>>
        cleanFsHash(LinkedHashMap<Integer,List<AttributeValuePair>> fsHash) {

        for (Map.Entry<Integer,List<AttributeValuePair>> entry : fsHash.entrySet())
        {
            List<AttributeValuePair> values = entry.getValue();

            for (int i = 0; i < values.size(); i++)
            {
                if (values.get(i) instanceof NonTerminalAVP)
                {
                    if (! fsHash.containsKey(Integer.parseInt(values.get(i).value)) )
                    {
                        values.remove(values.get(i));

                    }
                    if (values.isEmpty())
                    {
                        fsHash.remove(entry.getKey());
                    }
                }
            }
        }
        return fsHash;
    }


    public static LinkedHashMap<Integer,List<AttributeValuePair>>
    deleteEmptyEntries(LinkedHashMap<Integer,List<AttributeValuePair>> fsHash)
    {
        for (Iterator<Map.Entry<Integer,List<AttributeValuePair>>> it = fsHash.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<Integer,List<AttributeValuePair>> entry = it.next();
            if (entry.getValue().isEmpty())
            {
                it.remove();
            }
        }
        return fsHash;
    }


    public static String fsHashToString(
            LinkedHashMap<String,
            LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash)
    {

        StringBuilder builder = new StringBuilder();


        for (String stringkey : fsHash.keySet())

        {
            SortedSet<Integer> keys = new TreeSet<Integer>(fsHash.get(stringkey).keySet());

            for (Integer key : keys) {
                String keyO = key.toString();
                String value = fsHash.get(stringkey).get(key).toString();
                builder.append(stringkey + " " + keyO + " " + value);
                builder.append(System.lineSeparator());
            }
        }

        return builder.toString();
    }



    //TODO This function needs to be fixed. It runs into an endless recursive loop sometimes. Probably due to the kei variable.
    public static Set<Integer> returnPathNodes(LinkedHashMap<String,
            LinkedHashMap<Integer, List<AttributeValuePair>>> fsHash,
                                               Integer key, String ambkey, Set<Integer> pathNodes)
    {
        for (Integer kei : fsHash.get(ambkey).keySet())
        {
            for (AttributeValuePair avp : fsHash.get(ambkey).get(kei))
            {
                try {
                    if ((Integer.parseInt(avp.value)) == key)
                    {
                        System.out.println(avp.toString());
                        pathNodes.add(kei);
                        return returnPathNodes(fsHash,kei,ambkey,pathNodes);
                    }
                }
                catch(Exception e)
                {
                    continue;
                }
            }
        }
        return pathNodes;

    }
}
