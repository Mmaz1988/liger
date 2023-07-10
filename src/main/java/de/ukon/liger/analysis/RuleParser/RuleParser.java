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

package de.ukon.liger.analysis.RuleParser;

import de.ukon.liger.analysis.LinguisticDictionary;
import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.SolutionKey;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.utilities.HelperMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RuleParser {


    private List<LinguisticStructure> fsList;
    private List<Rule> rules = new ArrayList<Rule>();



    private List<Rule> appliedRules = new ArrayList<>();
    private static Pattern graphPattern = Pattern.compile("(#.+?)\\s+(\\S+)\\s+(.+)");
    private Boolean replace;
    private Set<String> usedKeys = new HashSet<>();
    private Set<Set<String>> usedReadings = new HashSet<>();
    public LinguisticDictionary dict = new LinguisticDictionary();
    private final static Logger LOGGER = LoggerFactory.getLogger(RuleParser.class);


    public RuleParser(String input) {
        this.replace = false;
        this.rules = parseRuleFile(input);
    }

    public RuleParser(List<LinguisticStructure> fsList) {
        this.fsList = fsList;
        this.replace = false;
    }

    public RuleParser(List<LinguisticStructure> fsList, String input) {
        this.fsList = fsList;
        this.replace = false;
        this.rules = parseRuleFile(input);
    }

    public RuleParser(List<LinguisticStructure> fsList, String input, Boolean replace) {
        this.fsList = fsList;
        this.replace = replace;
        this.rules = parseRuleFile(input);
    }


    public RuleParser(List<LinguisticStructure> fsList, Path path) {
        this.fsList = fsList;
        this.replace = false;

        String fileString = null;
        try {
            fileString = new String(Files.readAllBytes(path));
            this.rules = parseRuleFile(fileString);
        }catch(Exception e)
        {
            LOGGER.error("Failed to load rule file");
            this.rules = new ArrayList<>();
       //     e.printStackTrace();
        }


    }

    public RuleParser(Path path) {
        this.replace = false;

        String fileString = null;
        try {
            fileString = new String(Files.readAllBytes(path));
            this.rules = parseRuleFile(fileString);
        }catch(Exception e)
        {
            LOGGER.error("Failed to load rule file");
            this.rules = new ArrayList<>();
            //     e.printStackTrace();
        }


    }

    public RuleParser(File path) {
        this.replace = false;

        try {
            String fileString = new String(Files.readAllBytes(Paths.get(path.toString())));
            this.rules = parseRuleFile(fileString);
        } catch (Exception e) {
            LOGGER.error("Failed to load rule file", e);
            this.rules = new ArrayList<>();
            //   e.printStackTrace();
        }


    }



    public void addAnnotation2(LinguisticStructure fs) {
        resetRuleParser();
        QueryParser qp = new QueryParser(fs);

        for (Integer key : qp.getFsIndices().keySet()) {
            usedKeys.add(qp.getFsIndices().get(key).getFsNode());
        }

        Integer key = qp.getFsIndices().keySet().size();

        for (int k = 0; k < rules.size(); k++) {
            Rule r = rules.get(k);
            r.setRuleIndex(k);

            if (k == 22)
            {
                System.out.println("Stop");
            }

            LOGGER.debug("Currently processing rule with index " + k + ":\n" +
                    "\t" + r.toString());

            HashMap<Integer, GraphConstraint> annotation = new HashMap<>();

            qp.resetParser();
            qp.generateQuery(r.getLeft());
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            if  (qpr.isSuccess && !r.getRight().equals("0"));
            {
                List<String> search = r.splitGoal();

                try {
                    boolean fixedContext = false;
                    Set<ChoiceVar> context = new HashSet<>();
                    //for (String searchString : search) {
                    for (int i = 0; i < search.size(); i++) {
                        String searchString = search.get(i).trim();
                        Matcher graphMatcher = graphPattern.matcher(searchString);

                        if (graphMatcher.matches()) {
                            Matcher nodeMatcher = HelperMethods.fsNodePattern.matcher(graphMatcher.group(1));
                            Matcher valueMatcher = HelperMethods.fsNodePattern.matcher(graphMatcher.group(3));
                            HashMap<Integer, GraphConstraint> newConstraints = new HashMap<>();

                            boolean valueMatches = valueMatcher.matches();

                            if (nodeMatcher.matches()) {
                                for (Set<SolutionKey> solutionKey : qpr.result.keySet()) {

                                    if (!fixedContext){
                                        context = extractContexts(qpr.result.get(solutionKey), newConstraints);
                                }
                                    if (variableIsAssigned(qpr, solutionKey, nodeMatcher.group(1))) {

                                        String key2 = qpr.result.get(solutionKey).get(nodeMatcher.group(1)).keySet().stream().findAny().get();

                                        if (valueMatches) {
                                            if (variableIsAssigned(qpr, solutionKey, valueMatcher.group(1))) {

                                                String key3 = qpr.result.get(solutionKey).get(valueMatcher.group(1)).keySet().stream().findAny().get();
                                                GraphConstraint c = new GraphConstraint();
                                                c.setReading(context);
                                                c.setFsNode(key2);
                                                c.setRelationLabel(graphMatcher.group(2));
                                                c.setFsValue(key3);

                                                //Readings experiment start
                                                for (Integer constraintKey : newConstraints.keySet()) {
                                                    //TODO return unused context


                                                    GraphConstraint c1 = newConstraints.get(constraintKey);
                                                    if (c1.getFsNode().equals(key2) && c1.getRelationLabel().equals(c.getRelationLabel()) &&
                                                            c1.getReading().equals(c.getReading())) {
                                                        ChoiceVar choice = new ChoiceVar("X1");
                                                        Set<ChoiceVar> newChoice = new HashSet<>();
                                                        newChoice.add(choice);
                                                        c.setReading(newChoice);
                                                    }
                                                }//Readings experiment end

                                                qpr.result.get(solutionKey).get(nodeMatcher.group(1)).get(key2).put(key, c);
                                                newConstraints.put(key, c);
                                                key++;


                                            } else {
                                                String newFsNode = returnUnusedVar();

                                                /*
                                                if (!newValues.keySet().contains(valueMatcher.group(1))) {
                                                    newValues.put(valueMatcher.group(1), new HashSet<>());
                                                }
                                                newValues.get(valueMatcher.group(1)).add(newFsNode);
                                                */


                                                //   qp.getFsVarAssignment().put(valueMatcher.group(1),new HashSet<>());
                                                //   qp.getFsVarAssignment().get(valueMatcher.group(1)).add(newFsNode);

                                                GraphConstraint c = new GraphConstraint();
                                                c.setReading(context);
                                                c.setFsNode(key2);
                                                c.setRelationLabel(graphMatcher.group(2));
                                                c.setFsValue(newFsNode);

                                                if (!qpr.result.get(solutionKey).keySet().contains(valueMatcher.group(1))) {
                                                    qpr.result.get(solutionKey).put(valueMatcher.group(1), new HashMap<>());
                                                }
                                                qpr.result.get(solutionKey).get(valueMatcher.group(1)).put(newFsNode, new HashMap<>());
                                                newConstraints.put(key, c);
                                                key++;

                                            }
                                        } else {

                                            String newValue = graphMatcher.group(3);
                                            String newLabel = graphMatcher.group(2);
                                            if (replace) {
                                                newValue = replaceVars(qpr, solutionKey, newValue);
                                                newLabel = replaceVars(qpr, solutionKey, newLabel);
                                            }


                                            boolean replaceValue = false;
                                            for (GraphConstraint c : fs.annotation) {
                                                if (c.getFsNode().equals(key2) && c.getRelationLabel().equals(newLabel) &&
                                                        c.getReading().equals(context)) {
                                                    if (r.isBranch()) {

                                                        //TODO Return new choice var dynimcally
                                                        ChoiceVar current = new ChoiceVar("A1");
                                                        ChoiceVar branch = new ChoiceVar("A2");

                                                        Set<ChoiceVar> currentSet = new HashSet<>();
                                                        currentSet.add(current);
                                                        Set<ChoiceVar> branchSet = new HashSet<>();
                                                        branchSet.add(branch);

                                                        c.setReading(currentSet);

                                                        GraphConstraint c1 = new GraphConstraint();
                                                        c1.setReading(branchSet);
                                                        c1.setFsNode(key2);
                                                        c1.setRelationLabel(newLabel);
                                                        c1.setFsValue(newValue);

                                                        context = branchSet;
                                                        fixedContext = true;

                                                        qpr.result.get(solutionKey).get(nodeMatcher.group(1)).get(key2).put(key, c1);
                                                        newConstraints.put(key, c1);
                                                        key++;

                                                        LOGGER.debug("Added new branch: " + branch.toString());

                                                        replaceValue = true;

                                                    } else {
                                                        //TODO log info about relation label
                                                    LOGGER.debug("Rewritten value: " + c.getFsValue() + " into: " + newValue);
                                                    c.setFsValue(newValue);
                                                    c.setRelationLabel(newLabel);
                                                    replaceValue = true;

                                          //          this.appliedRules.add(r);
                                                }
                                            }
                                            }

                                            //TODO
                                            if (!replaceValue) {

                                                GraphConstraint c = new GraphConstraint();
                                                c.setReading(context);
                                                c.setFsNode(key2);
                                                c.setRelationLabel(newLabel);
                                                c.setFsValue(newValue);


                                                //Reading experiment start

/*
                                                for (Integer constraintKey : newConstraints.keySet()) {
                                                    //TODO return unused context
                                                    ChoiceVar choice = new ChoiceVar("A1");
                                                    Set<ChoiceVar> newChoice = new HashSet<>();
                                                    newChoice.add(choice);

                                                    GraphConstraint c1 = newConstraints.get(constraintKey);
                                                    if (c1.getFsNode().equals(key2) && c1.getRelationLabel().equals(graphMatcher.group(2)) &&
                                                            c.getReading().equals(c1.getReading())) {
                                                        c.setReading(newChoice);
                                                    }
                                                }

 */
                                                //Reading experiment end


                                                qpr.result.get(solutionKey).get(nodeMatcher.group(1)).get(key2).put(key, c);
                                                newConstraints.put(key, c);
                                                key++;
                                            }
                                        }
                                    } else {


                                        String key2 = returnUnusedVar();

                                        qpr.result.get(solutionKey).put(nodeMatcher.group(1), new HashMap<>());
                                        qpr.result.get(solutionKey).get(nodeMatcher.group(1)).put(key2, new HashMap<>());
                                        //    qp.getFsVarAssignment().put(nodeMatcher.group(1), new HashSet<>());
                                        //   qp.getFsVarAssignment().get(nodeMatcher.group(1)).add(key2);

                                        if (valueMatches) {
                                            if (variableIsAssigned(qpr, solutionKey, valueMatcher.group(1))) {
                                                String key3 = qpr.result.get(solutionKey).get(valueMatcher.group(1)).keySet().stream().findAny().get();

                                                GraphConstraint c = new GraphConstraint();
                                                c.setReading(context);
                                                c.setFsNode(key2);
                                                c.setRelationLabel(graphMatcher.group(2));
                                                c.setFsValue(key3);

                                                qpr.result.get(solutionKey).get(nodeMatcher.group(1)).get(key2).put(key, c);
                                                newConstraints.put(key, c);
                                                key++;
                                            } else {
                                                String newFsNode = returnUnusedVar();

                                                GraphConstraint c = new GraphConstraint();
                                                c.setReading(context);
                                                c.setFsNode(key2);
                                                c.setRelationLabel(graphMatcher.group(2));
                                                c.setFsValue(newFsNode);

                                                qpr.result.get(solutionKey).get(nodeMatcher.group(1)).get(key2).put(key, c);
                                                newConstraints.put(key, c);
                                                key++;
                                            }
                                        } else {
                                            GraphConstraint c = new GraphConstraint();
                                            c.setReading(context);
                                            c.setFsNode(key2);
                                            c.setRelationLabel(graphMatcher.group(2));
                                            c.setFsValue(graphMatcher.group(3));

                                            if (replace) {
                                                c.setFsValue(replaceVars(qpr, solutionKey, (String) c.getFsValue()));
                                                c.setRelationLabel(replaceVars(qpr,solutionKey,c.getRelationLabel()));
                                            }

                                            qpr.result.get(solutionKey).get(nodeMatcher.group(1)).get(key2).put(key, c);
                                            newConstraints.put(key, c);
                                            key++;
                                        }
                                    }
                                }
                            }

                            annotation.putAll(newConstraints);
                            qp.getFsIndices().putAll(newConstraints);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to parse rule right-hand side");
                }
            }




            if (r.isRewrite())
            {
                List<GraphConstraint> removedFacts = new ArrayList<>();
                for (Set<SolutionKey> solution : qpr.result.keySet())
                {
                    for (String var : qpr.result.get(solution).keySet())
                    {
                        for (String index : qpr.result.get(solution).get(var).keySet())
                        {
                            for (Integer i : qpr.result.get(solution).get(var).get(index).keySet())
                            {
                                removedFacts.add(qp.getFsIndices().get(i));
                             //   usedKeys.remove(qp.getFsIndices().get(i).getFsNode());
                           //     qp.getFsIndices().remove(i);

                            }
                        }
                    }
                }

                Iterator<GraphConstraint> constraintIterator = fs.constraints.iterator();

//TODO Find a prettier way to remove
        while (constraintIterator.hasNext())
        {
            GraphConstraint next = constraintIterator.next();
                    for (GraphConstraint c1 : removedFacts)
                    {
                        if (next.equals(c1))
                        {
                            constraintIterator.remove();
                        }
                    }
                }

                LOGGER.debug("Removed the following facts:");
                String removed = String.join("\n",removedFacts.stream().map(Object::toString).collect(Collectors.toList()));
                LOGGER.debug("\n" + removed);

            }

            HashMap<Integer,GraphConstraint> newIndexedInidces = new HashMap<>();
            List<GraphConstraint> newConstraints = new ArrayList<>();

            int keys = 0;

            for (Integer i : qp.getFsIndices().keySet())
            {
                newIndexedInidces.put(keys,qp.getFsIndices().get(i));
                newConstraints.add(qp.getFsIndices().get(i));
                keys++;
            }

            qp.setFsIndices(newIndexedInidces);
         //   fs.constraints = newConstraints;

            if (!annotation.keySet().isEmpty()) {



                for (Integer i : annotation.keySet())
                {
                    newIndexedInidces.put(keys,annotation.get(i));
                    keys++;
                }

                //adds newly created constraints to the contents of queryParser so they are parsed in subsequent rules
                qp.setFsIndices(newIndexedInidces);
         //       fs.constraints = newConstraints;



                LOGGER.debug("Added the following facts:");
                List<String> addedFacts = new ArrayList<>();
                for (Integer akey : annotation.keySet()) {
                    fs.annotation.add(annotation.get(akey));
                    addedFacts.add(annotation.get(akey).toString());
                }
                String added = String.join("\n", addedFacts);
                LOGGER.debug("\n" + added);

                LOGGER.debug("\t" + "Rule has been applied!");
                this.appliedRules.add(r);

            }
        }
    }


    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }


    // if (qp.getFsVarAssignment().containsKey(nodeMatcher.group(1)))
    public Boolean variableIsAssigned(QueryParserResult qpr, Set<SolutionKey> solutionKey, String key) {
        return qpr.result.get(solutionKey).keySet().contains(key);
    }


    public String replaceVars(QueryParserResult qpr, Set<SolutionKey> solutionKey, String value) {
        Matcher matcher = HelperMethods.fsNodePattern.matcher(value);
        Pattern lexPattern = Pattern.compile("(lex\\((.*?),(.*?)\\))");

        String replacedFsVars = value;
        StringBuffer sb = new StringBuffer();

        //Replace fsNode variables
        while (matcher.find()) {

            if (qpr.result.get(solutionKey).containsKey(matcher.group(1))) {
                String key = qpr.result.get(solutionKey).get(matcher.group(1)).keySet().stream().findAny().get();
                matcher.appendReplacement(sb, key);
            } else {
                matcher.appendReplacement(sb, returnUnusedVar());
            }
        }
        matcher.appendTail(sb);
        replacedFsVars = sb.toString();

        //Replace value variables
        Matcher matcher2 = HelperMethods.valueVarPattern.matcher(replacedFsVars);

        StringBuffer sb2 = new StringBuffer();
        while (matcher2.find()) {
            for (Set<SolutionKey> key : qpr.valueBindings.keySet()) {
                if (solutionKey.containsAll(key)) {
                    if (qpr.valueBindings.get(key).containsKey(matcher2.group(1))) {
                        String key2 = qpr.valueBindings.get(key).get(matcher2.group(1));
                        matcher2.appendReplacement(sb2, key2);
                        break;
                    }
                }
            }
        }

        matcher2.appendTail(sb2);
        replacedFsVars = sb2.toString();


        Matcher matcher3 = lexPattern.matcher(replacedFsVars);
        StringBuffer sb3 = new StringBuffer();

        while (matcher3.find()) {
            String val = matcher3.group(2);
            String dict = matcher3.group(3);
            boolean replace = false;

            if (LinguisticDictionary.ld.containsKey(dict)) {
                for (String key : LinguisticDictionary.ld.get(dict).keySet()) {
                    if (LinguisticDictionary.ld.get(dict).get(key).contains(val)) {
                        matcher3.appendReplacement(sb3, key);
                        replace = true;
                    }
                }
                if (!replace) {
                    matcher3.appendReplacement(sb3, "undefined");
                }
            }
        }
        matcher3.appendTail(sb3);

        replacedFsVars = sb3.toString();
        replacedFsVars = HelperMethods.stripValeue2(replacedFsVars);

        return replacedFsVars;
    }

    public String returnUnusedVar() {
        int i = usedKeys.size();

        while (usedKeys.contains(Integer.toString(i))) {
            i++;
        }

        usedKeys.add(Integer.toString(i));
        return Integer.toString(i);
    }

    public Boolean getReplace() {
        return replace;
    }

    public void setReplace(Boolean replace) {
        this.replace = replace;
    }


    //Parse rule file
    public List<Rule> parseRuleFile(String fileString) {
        int lineCounter = 1;

        List<Rule> out = new ArrayList<>();

        if (fileString != null && fileString.length() > 0) {

            StringBuilder left = new StringBuilder();
            StringBuilder right = new StringBuilder();
            for (int i = 0; i < fileString.length(); i++) {

                char c = fileString.charAt(i);

                if (c == '\n') {
                    left.append(c);
                    lineCounter++;
                    continue;
                }

                if (c == '-' && fileString.charAt(i+1) == '-') {
                    c = fileString.charAt(i + 2);
                    i = i + 2;
                    if (c >= 97 && c <= 122 || c >= 48 && c <= 57) {

                        StringBuilder sb = new StringBuilder();

                        //or sequence of letters
                        while (((c >= 97 && c <= 122) || (c >= 48 && c <= 57))) {
                            sb.append(c);
                            i++;
                            if (i < fileString.length()) {
                                c = fileString.charAt(i);
                            } else {
                                break;
                            }
                        }

                        if (sb.toString().equals("replace")) {
                            i++;
                            StringBuilder bool = new StringBuilder();
                            while (!(fileString.charAt(i) == ')')) {
                                bool.append(fileString.charAt(i));
                                i++;
                            }

                            if (bool.toString().equals("true")) {
                                this.replace = true;
                            } else {
                                this.replace = false;
                            }
                            i++;
                        }
                   //     System.out.println(fileString.charAt(i));
                     continue;
                    }


                    while (String.valueOf(fileString.charAt(i)).matches(".")) {
                        i++;
                    }
                    if (c == '\n') {
                        lineCounter++;
                    }
                    continue;
                }

                if (c == '/' && fileString.charAt(i+1) == '/')
                {
                    i++;
                    while (String.valueOf(fileString.charAt(i)).matches("."))
                    {
                        i++;
                    }

                    if (!String.valueOf(fileString.charAt(i)).matches(".")) {
                        lineCounter++;
                        continue;
                    }
                }

                if (c =='=' &&
                        (fileString.charAt(i + 1) == '=' || fileString.charAt(i + 1) == '-' || fileString.charAt(i + 1) == '+')
                        && fileString.charAt(i + 2) == '>')
                {
                    boolean rewrite = false;
                    boolean branch = false;
                    if (fileString.charAt(i + 1) == '-')
                    {
                      rewrite = true;
                    } else if (fileString.charAt(i + 1) == '+')
                    {
                        branch = true;
                    }
                    i = i + 3;
                    c = fileString.charAt(i);
                    while (!(c == '.' && !String.valueOf(fileString.charAt(i + 1)).matches("."))) {

                        if (c == '\n') {
                            lineCounter++;
                        }

                        right.append(c);
                        i++;
                        c = fileString.charAt(i);
                        if (c == '.' && i == fileString.length() - 1) {
                            break;
                        }
                    }

                    Rule r = new Rule(left.toString().trim(),right.toString().trim(), rewrite,branch);
                    r.setLineNumber(lineCounter);
                    out.add(r);

                    left = new StringBuilder();
                    right = new StringBuilder();
                    continue;
                }



                if (i < fileString.length() - 1) {
                    c = fileString.charAt(i);
                    left.append(c);
                }


            }
        }

        return out;

    }

/*
    public void resolveAmbiguity(GraphConstraint a, GraphConstraint b, HashMap<Integer,GraphConstraint> fsConstraints)
    {
        Set<String> usedReadings = new HashSet<>();

        for (Integer key : fsConstraints.keySet())
        {
            usedReadings.addAll(fsConstraints.get(key).getReading());
        }
    }
*/

    public void resetRuleParser() {
        usedKeys = new HashSet<>();
        usedReadings = new HashSet<>();
    }


    public Set<ChoiceVar> extractContexts(HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> result,
                                          HashMap<Integer, GraphConstraint> newConstraints) {
        Set<ChoiceVar> out = new HashSet<>();

        for (String key : result.keySet()) {
            for (String key2 : result.get(key).keySet()) {

                 List<ChoiceVar> embeddedChoices = new ArrayList<>();
                 boolean containsTop = false;
                for (Integer key3 : result.get(key).get(key2).keySet()) {


                    if (result.get(key).get(key2).get(key3).getReading().stream().findAny().get().toString().equals("1")) {
                    containsTop = true;
                    } else
                    {
                        embeddedChoices.addAll(result.get(key).get(key2).get(key3).getReading());
                    }

/*
                    if (result.get(key).get(key2).get(key3).getReading().stream().noneMatch(s -> s.toString().equals("1")) &&
                            !newConstraints.keySet().contains(key3)) {
                        out.addAll(result.get(key).get(key2).get(key3).getReading());
                    }
 */
                }
                //TODO search for highest available context when there is no top context
                if (!containsTop)
                {
                    out.addAll(embeddedChoices);
                }
            }
        }
        if (!out.isEmpty()) {
            return out;
        } else {
            out.add(new ChoiceVar("1"));
            return out;
        }
    }

    public List<Rule> getAppliedRules() {
        return appliedRules;
    }

    public void setAppliedRules(List<Rule> appliedRules) {
        this.appliedRules = appliedRules;
    }
}






/*

    public void loadRules(String path)
    {
        List<Rule> rules = new ArrayList<>();

        try {
            File f = new File(path);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.startsWith("//"))
                {
                    continue;
                }
                if (line.contains("replace=true;"))
                {
                    this.replace = true;
                    continue;
                }
                Rule r = new Rule(line);
                rules.add(r);
            }
        }catch(Exception e)
        {
         System.out.println("Couldn't read rule file");
        }
        this.rules = rules;
    }


    public void addAnnotations()
    {
        for (SyntacticStructure fs : fsList)
        {
            addAnnotation2(fs);
        }

    }

 */

    /*
    public void addAnnotation(Fstructure fs)
    {
        analysis.QueryParser qp = new analysis.QueryParser(fs,true);



        Integer key = qp.getFsIndices().keySet().size();

        for (Rule r : rules)
        {

            HashMap<Integer, GraphConstraint> annotation = new HashMap<>();

            qp.resetParser();
            qp.generateQuery(r.getLeft());
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            if  (qpr.isSuccess)
            {


                List<String> search = new ArrayList<String>(Arrays.asList(r.getRight().split("&")));

                try {
                    //for (String searchString : search) {
                    for (int i = 0; i < search.size(); i++)
                    {

                       String searchString = search.get(i).trim();

                        Matcher graphMatcher = graphPattern.matcher(searchString);

                        if (graphMatcher.matches()) {
                            Matcher nodeMatcher = analysis.QueryParser.fsNodePattern.matcher(graphMatcher.group(1));
                            Matcher valueMatcher = analysis.QueryParser.fsNodePattern.matcher(graphMatcher.group(3));
                            HashMap<Integer,GraphConstraint> newConstraints = new HashMap<>();

                            boolean valueMatches = valueMatcher.matches();

                            if (nodeMatcher.matches()) {
                                if (variableIsAssigned(qpr,nodeMatcher.group(1)))
                                {
                                    HashMap<String,Set<String>> newValues = new HashMap<>();

                                    for (String key2 : qp.getFsVarAssignment().get(nodeMatcher.group(1)))
                                    {
                                        if (valueMatches) {
                                            if (qp.getFsVarAssignment().containsKey(valueMatcher.group(1))) {
                                                for (String key3 : qp.getFsVarAssignment().get(valueMatcher.group(1))) {
                                                    GraphConstraint c = new GraphConstraint();
                                                    c.setFsNode(key2);
                                                    c.setRelationLabel(graphMatcher.group(2));
                                                    c.setFsValue(key3);
                                                    newConstraints.put(key,c);
                                                    key++;

                                                }
                                            }
                                            else
                                            {
                                                String newFsNode = qp.returnUnusedVar();

                                                if (!newValues.keySet().contains(valueMatcher.group(1)))
                                                {
                                                    newValues.put(valueMatcher.group(1),new HashSet<>());
                                                }
                                                newValues.get(valueMatcher.group(1)).add(newFsNode);
                                             //   qp.getFsVarAssignment().put(valueMatcher.group(1),new HashSet<>());
                                             //   qp.getFsVarAssignment().get(valueMatcher.group(1)).add(newFsNode);
                                                GraphConstraint c = new GraphConstraint();
                                                c.setFsNode(key2);
                                                c.setRelationLabel(graphMatcher.group(2));
                                                c.setFsValue(newFsNode);

                                                newConstraints.put(key,c);
                                                key++;

                                            }
                                        }
                                        else
                                        {
                                            GraphConstraint c = new GraphConstraint();
                                            c.setFsNode(key2);
                                            c.setRelationLabel(graphMatcher.group(2));


                                            c.setFsValue(graphMatcher.group(3));
                                            newConstraints.put(key,c);
                                            key++;
                                        }
                                    }

                                    qp.getFsVarAssignment().putAll(newValues);

                                } else
                                {

                                    String key2 = qp.returnUnusedVar();
                                    qp.getFsVarAssignment().put(nodeMatcher.group(1),new HashSet<>());
                                    qp.getFsVarAssignment().get(nodeMatcher.group(1)).add(key2);


                                    if (valueMatches) {
                                        if (qp.getFsVarAssignment().containsKey(valueMatcher.group(1))) {
                                            for (String key3 : qp.getFsVarAssignment().get(valueMatcher.group(1))) {
                                                GraphConstraint c = new GraphConstraint();
                                                c.setFsNode(key2);
                                                c.setRelationLabel(graphMatcher.group(2));
                                                c.setFsValue(key3);


                                                qp.getVarAssignment().put(nodeMatcher.group(1),new HashSet<>());
                                                qp.getVarAssignment().get(nodeMatcher.group(1)).add(key);
                                                newConstraints.put(key,c);
                                                key++;

                                            }
                                        }
                                        else
                                        {
                                            String newFsNode = qp.returnUnusedVar();
                                            qp.getFsVarAssignment().put(valueMatcher.group(1),new HashSet<>());
                                            qp.getFsVarAssignment().get(valueMatcher.group(1)).add(newFsNode);
                                            GraphConstraint c = new GraphConstraint();
                                            c.setFsNode(key2);
                                            c.setRelationLabel(graphMatcher.group(2));
                                            c.setFsValue(newFsNode);


                                            qp.getVarAssignment().put(nodeMatcher.group(1),new HashSet<>());
                                            qp.getVarAssignment().get(nodeMatcher.group(1)).add(key);
                                            newConstraints.put(key,c);
                                            key++;

                                        }
                                    }
                                    else
                                    {
                                        GraphConstraint c = new GraphConstraint();
                                        c.setFsNode(key2);
                                        c.setRelationLabel(graphMatcher.group(2));


                                        c.setFsValue(graphMatcher.group(3));

                                        qp.getVarAssignment().put(nodeMatcher.group(1),new HashSet<>());
                                        qp.getVarAssignment().get(nodeMatcher.group(1)).add(key);
                                        newConstraints.put(key,c);
                                        key++;
                                    }

                                }

                            }
                            annotation.putAll(newConstraints);

                        }
                    }
                } catch(Exception e)
                {
                    System.out.println("Failed to parse rule right-hand side");
                }


            }



            qp.getFsIndices().putAll(annotation);

            for (Integer akey : annotation.keySet())
            {
             fs.annotation.add(annotation.get(akey));
            }


        }


    }

*/
