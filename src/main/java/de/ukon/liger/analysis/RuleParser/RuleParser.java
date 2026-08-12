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
import de.ukon.liger.analysis.QueryParser.Solution;
import de.ukon.liger.analysis.QueryParser.SolutionKey;
import de.ukon.liger.analysis.QueryParser.HierarchyParser;
import de.ukon.liger.analysis.QueryParser.HierarchyRegistry;
import de.ukon.liger.analysis.QueryParser.EmbeddedDefinitionExtractor;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
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



    private LinkedHashSet<Rule> appliedRules = new LinkedHashSet<>();
    private LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> addedAnnotationsByRule = new LinkedHashMap<>();
    private IdentityHashMap<LinguisticStructure, LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>>> addedAnnotationsByStructure = new IdentityHashMap<>();
    private LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> removedAnnotationsByRule = new LinkedHashMap<>();
    private static Pattern graphPattern = Pattern.compile("(#.+?)\\s+(\\S+)\\s+(.+)");
    private Boolean replace;
    private Set<String> usedKeys = new HashSet<>();
    private Set<Set<String>> usedReadings = new HashSet<>();
    private TemplateRegistry templateRegistry = new TemplateRegistry();
    private HierarchyRegistry hierarchyRegistry = new HierarchyRegistry();
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



    public Set<LinguisticStructure> addAnnotation2(Set<LinguisticStructure> structures) {
        resetRuleParser();

        LinkedHashSet<LinguisticStructure> active = structures == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(structures);

        for (LinguisticStructure structure : active) {
            seedUsedKeys(structure);
        }

        for (int k = 0; k < rules.size(); k++) {
            Rule r = rules.get(k);
            r.setRuleIndex(k);
            LinkedHashSet<LinguisticStructure> next = new LinkedHashSet<>();
            LinkedHashSet<GraphConstraint> ruleAddedAnnotations = new LinkedHashSet<>();

            for (LinguisticStructure structure : active) {
                QueryParser qp = new QueryParser(structure, templateRegistry, hierarchyRegistry);
                qp.resetParser();
                qp.generateQuery(r.getLeft());
                QueryParserResult qpr = templateRegistry != null && !templateRegistry.getTemplates().isEmpty()
                        ? qp.parseQueryWithTemplates(r.getLeft()).stream().filter(result -> result.isSuccess).findFirst()
                        .orElse(new QueryParserResult(new HashMap<>(), new HashMap<>()))
                        : qp.parseQuery(qp.getQueryList());

                if (!qpr.isSuccess) {
                    next.add(structure);
                    continue;
                }

                this.appliedRules.add(r);

                if (isQuestionRule(r)) {
                    boolean producedBranch = false;
                    for (Solution solution : dedupeSolutions(r, qpr)) {
                        LinguisticStructure branch = structure.copy();
                        if (r.isQuestionDelete()) {
                            removeFactsForSolution(branch, qpr, solution);
                        } else {
                            LinkedHashSet<GraphConstraint> added = addFactsForSolution(branch, qpr, solution, r);
                            ruleAddedAnnotations.addAll(added);
                            recordAddedAnnotations(branch, k, added);
                        }
                        next.add(branch);
                        producedBranch = true;
                    }
                    if (!producedBranch) {
                        next.add(structure);
                    }
                } else {
                    RuleParser temp = new RuleParser(new ArrayList<>());
                    temp.setReplace(this.replace);
                    temp.templateRegistry = this.templateRegistry;
                    temp.hierarchyRegistry = this.hierarchyRegistry;
                    temp.dict = this.dict;
                     temp.setRules(new ArrayList<>(Collections.singletonList(r)));
                     temp.addAnnotation2(structure);
                     LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> tempAddedAnnotations =
                             temp.getAddedAnnotationsByRule(structure);
                     r.setRuleIndex(k);
                     mergeAddedAnnotations(tempAddedAnnotations, Collections.singletonMap(0, k));
                     recordAddedAnnotations(structure, k, tempAddedAnnotations.get(0));
                     next.add(structure);
                }
            }

            if (!ruleAddedAnnotations.isEmpty()) {
                addedAnnotationsByRule.put(k, ruleAddedAnnotations);
            }

            active = next;
        }

        return active;
    }

    private LinkedHashSet<GraphConstraint> addFactsForSolution(LinguisticStructure branch,
                                                               QueryParserResult qpr,
                                                               Solution solution,
                                                               Rule r) {
        LinkedHashSet<GraphConstraint> added = new LinkedHashSet<>();
        if (!qpr.result.containsKey(solution)) {
            return added;
        }

        Set<ChoiceVar> context = extractContexts(qpr.result.get(solution), new HashMap<>());
        List<String> search = r.splitGoal();

        for (String searchString : search) {
            Matcher graphMatcher = graphPattern.matcher(searchString.trim());
            if (!graphMatcher.matches()) {
                continue;
            }

            Matcher nodeMatcher = HelperMethods.fsNodePattern.matcher(graphMatcher.group(1));
            Matcher valueMatcher = HelperMethods.fsNodePattern.matcher(graphMatcher.group(3));
            boolean valueMatches = valueMatcher.matches();

            if (!nodeMatcher.matches()) {
                continue;
            }

            String key2 = resolveNodeReference(qpr, solution, nodeMatcher.group(1));
            String newLabel = graphMatcher.group(2);
            String newValue;

            if (valueMatches) {
                newValue = resolveValueReference(qpr, solution, valueMatcher.group(1));
            } else {
                newValue = graphMatcher.group(3);
                if (replace) {
                    newValue = replaceVars(qpr, solution, newValue);
                    newLabel = replaceVars(qpr, solution, newLabel);
                }
            }

            GraphConstraint c = new GraphConstraint();
            c.setReading(context);
            c.setFsNode(key2);
            c.setRelationLabel(newLabel);
            c.setFsValue(newValue);
            branch.annotation.add(c);
            added.add(c);
        }

        return added;
    }

    private void removeFactsForSolution(LinguisticStructure branch, QueryParserResult qpr, Solution solution) {
        if (!qpr.result.containsKey(solution)) {
            return;
        }

        List<GraphConstraint> removedFacts = new ArrayList<>();
        for (String var : qpr.result.get(solution).keySet()) {
            for (String index : qpr.result.get(solution).get(var).keySet()) {
                removedFacts.addAll(qpr.result.get(solution).get(var).get(index).values());
            }
        }

        Iterator<GraphConstraint> constraintIterator = branch.constraints.iterator();
        while (constraintIterator.hasNext()) {
            GraphConstraint next = constraintIterator.next();
            for (GraphConstraint c1 : removedFacts) {
                if (next.equals(c1)) {
                    constraintIterator.remove();
                    break;
                }
            }
        }
    }

    private void mergeAddedAnnotations(LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> source,
                                       Map<Integer, Integer> tempIndexToOriginalIndex) {
        for (Map.Entry<Integer, LinkedHashSet<GraphConstraint>> entry : source.entrySet()) {
            Integer originalIndex = tempIndexToOriginalIndex.getOrDefault(entry.getKey(), entry.getKey());
            addedAnnotationsByRule.merge(originalIndex, new LinkedHashSet<>(entry.getValue()), (left, right) -> {
                left.addAll(right);
                return left;
            });
        }
    }

    private void recordAddedAnnotations(LinguisticStructure structure,
                                        Integer ruleIndex,
                                        Collection<GraphConstraint> annotations) {
        if (structure == null || ruleIndex == null || annotations == null || annotations.isEmpty()) {
            return;
        }

        LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> byRule =
                addedAnnotationsByStructure.computeIfAbsent(structure, ignored -> new LinkedHashMap<>());
        byRule.merge(ruleIndex, new LinkedHashSet<>(annotations), (left, right) -> {
            left.addAll(right);
            return left;
        });
    }

    private void seedUsedKeys(LinguisticStructure structure) {
        if (structure == null) {
            return;
        }

        if (structure.constraints != null) {
            for (GraphConstraint constraint : structure.constraints) {
                usedKeys.add(constraint.getFsNode());
            }
        }

        if (structure.annotation != null) {
            for (GraphConstraint constraint : structure.annotation) {
                usedKeys.add(constraint.getFsNode());
            }
        }
    }

    private boolean isQuestionRule(Rule r) {
        return r != null && ("?=>".equals(r.getOperator()) || "?->".equals(r.getOperator()));
    }

    /**
     * The solutions of a rule's left-hand side with redundant ones removed: two solutions that bind
     * every variable the right-hand side consumes to the same references, under the same choice
     * context, produce exactly the same annotations, so only the first of them is kept.
     *
     * <p>Solutions that leave a consumed variable unbound are always kept, see
     * {@link #ruleOutputSignature}. Insertion order is preserved so that the emitted annotations
     * and the branches derived from them stay stable across runs.
     */
    private List<Solution> dedupeSolutions(Rule r, QueryParserResult qpr) {
        RuleOutputVariables variables = collectRuleOutputVariables(r);
        List<Solution> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Solution solution : qpr.result.keySet()) {
            String signature = ruleOutputSignature(variables, qpr, solution);
            if (signature != null && !seen.add(signature)) {
                continue;
            }
            out.add(solution);
        }

        if (out.size() < qpr.result.size()) {
            LOGGER.debug("Conflated " + (qpr.result.size() - out.size()) + " of " + qpr.result.size() +
                    " equivalent solutions of rule:\n\t" + r);
        }

        return out;
    }

    /**
     * A stable key for everything a rule's right-hand side reads out of {@code solution}: the
     * binding of every variable it consumes plus the choice context the emitted facts inherit.
     *
     * <p>Returns {@code null} when any consumed variable is unbound. An unbound right-hand side
     * variable means the rule <em>introduces</em> a node for this match -- {@link #resolveNodeReference}
     * mints a fresh one per solution -- and every match is entitled to its own node. Such solutions
     * must therefore never be conflated with one another.
     */
    private String ruleOutputSignature(RuleOutputVariables variables, QueryParserResult qpr, Solution solution) {
        if (variables.deletesMatch()) {
            return null;
        }

        HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding = qpr.result.get(solution);
        StringBuilder signature = new StringBuilder();

        for (String variable : variables.nodeVariables()) {
            if (!variableIsAssigned(qpr, solution, variable)) {
                return null;
            }
            signature.append('#').append(variable).append('=')
                    .append(new TreeSet<>(binding.get(variable).keySet())).append('|');
        }

        for (String variable : variables.valueVariables()) {
            String valueBinding = lookupValueBinding(solution, variable, qpr.valueBindings);
            if (valueBinding == null) {
                return null;
            }
            signature.append(variable).append('=').append(valueBinding).append('|');
        }

        Set<String> context = new TreeSet<>();
        try {
            for (ChoiceVar choice : extractContexts(binding, new HashMap<>())) {
                context.add(choice.toString());
            }
        } catch (RuntimeException e) {
            // extractContexts assumes every matched constraint carries a reading and throws when one
            // does not. Without the context we cannot tell whether two solutions emit under the same
            // choice, so treat the solution as unique and leave the old behaviour untouched.
            return null;
        }

        return signature.append("context=").append(context).toString();
    }

    /**
     * The variables a rule's right-hand side consumes: fs-node names taken from {@code #a}, {@code *a}
     * and typed occurrences such as {@code #c_t}, and value variables taken from {@code %a}. Both the
     * node/value positions of a graph pattern and the terms {@link #replaceVars} substitutes into are
     * covered, because the whole conjunct is scanned.
     */
    private RuleOutputVariables collectRuleOutputVariables(Rule r) {
        if (r.getRight() == null || "0".equals(r.getRight().trim())) {
            return RuleOutputVariables.deletion();
        }

        Set<String> nodeVariables = new TreeSet<>();
        Set<String> valueVariables = new TreeSet<>();

        for (String conjunct : r.splitGoal()) {
            Matcher nodeMatcher = HelperMethods.fsNodePattern.matcher(conjunct);
            while (nodeMatcher.find()) {
                nodeVariables.add(stripTypeSuffix(nodeMatcher.group(1)));
            }

            Matcher valueMatcher = HelperMethods.valueVarPattern.matcher(conjunct);
            while (valueMatcher.find()) {
                valueVariables.add(valueMatcher.group(1));
            }
        }

        return new RuleOutputVariables(nodeVariables, valueVariables, false);
    }

    /**
     * Drops the semantic type from a typed variable occurrence, so that {@code c_t} and {@code c}
     * refer to the same binding. Mirrors what {@link #replaceVars} does before its own lookup.
     */
    private static String stripTypeSuffix(String variable) {
        String[] parts = variable.split("_");
        return parts.length == 2 ? parts[0] : variable;
    }

    private record RuleOutputVariables(Set<String> nodeVariables, Set<String> valueVariables, boolean deletesMatch) {
        /**
         * Marks a rule that deletes its match instead of annotating it. Such rules emit no
         * annotations, so there is nothing to conflate and their solutions are all kept.
         */
        static RuleOutputVariables deletion() {
            return new RuleOutputVariables(Collections.emptySet(), Collections.emptySet(), true);
        }
    }

    private String resolveNodeReference(QueryParserResult qpr, Solution solution, String reference) {
        if (qpr.result.containsKey(solution) && qpr.result.get(solution).containsKey(reference)) {
            return qpr.result.get(solution).get(reference).keySet().stream().findAny().orElseGet(this::returnUnusedVar);
        }
        return returnUnusedAnnotationNode();
    }

    private String resolveValueReference(QueryParserResult qpr, Solution solution, String reference) {
        if (qpr.result.containsKey(solution) && qpr.result.get(solution).containsKey(reference)) {
            return qpr.result.get(solution).get(reference).keySet().stream().findAny().orElseGet(this::returnUnusedVar);
        }
        return returnUnusedVar();
    }

    public void addAnnotation2(LinguisticStructure fs) {
        resetRuleParser();
        QueryParser qp = new QueryParser(fs, templateRegistry, hierarchyRegistry);

        for (Integer key : qp.getFsIndices().keySet()) {
            usedKeys.add(qp.getFsIndices().get(key).getFsNode());
        }

        Integer key = qp.getFsIndices().keySet().size();

        for (int k = 0; k < rules.size(); k++) {
            Rule r = rules.get(k);
            r.setRuleIndex(k);

            LOGGER.debug("Currently processing rule with index " + k + ":\n" +
                    "\t" + r.toString());

            HashMap<Integer, GraphConstraint> annotation = new HashMap<>();

            qp.resetParser();
            qp.generateQuery(r.getLeft());
            QueryParserResult qpr = templateRegistry != null && !templateRegistry.getTemplates().isEmpty()
                    ? qp.parseQueryWithTemplates(r.getLeft()).stream().filter(result -> result.isSuccess).findFirst()
                    .orElse(new QueryParserResult(new HashMap<>(), new HashMap<>()))
                    : qp.parseQuery(qp.getQueryList());

            if (qpr.isSuccess && !r.getRight().equals("0")) {

                List<String> search = r.splitGoal();
                List<Solution> solutions = dedupeSolutions(r, qpr);

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
                                for (Solution solutionKey : solutions) {

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

                                                qpr.result.get(solutionKey).get(nodeMatcher.group(1)).get(key2).put(key, c);
                                                newConstraints.put(key, c);
                                                key++;


                                            } else {
                                                 String newFsNode = returnUnusedAnnotationNode();

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

                                                    this.appliedRules.add(r);
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


                                         String key2 = returnUnusedAnnotationNode();

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
                                                 String newFsNode = returnUnusedAnnotationNode();

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
                boolean deleteOnly = r.getRight() != null && "0".equals(r.getRight().trim());

                Set<String> removedFacts = new LinkedHashSet<>();
                LinkedHashSet<GraphConstraint> removedConstraintSet = new LinkedHashSet<>();
                if (deleteOnly) {
                    for (Solution solution : qpr.result.keySet()) {
                        for (String var : qpr.result.get(solution).keySet()) {
                            for (String index : qpr.result.get(solution).get(var).keySet()) {
                                for (Integer i : qpr.result.get(solution).get(var).get(index).keySet()) {
                                    GraphConstraint candidate = qpr.result.get(solution).get(var).get(index).get(i);
                                    if (candidate != null) {
                                        removedFacts.add(edgeKey(candidate));
                                        removedConstraintSet.add(candidate);
                                    }
                                }
                            }
                        }
                    }
                }

                if (deleteOnly && fs.constraints != null) {
                    fs.constraints.removeIf(constraint -> removedFacts.contains(edgeKey(constraint)));
                }
                if (deleteOnly && fs.annotation != null) {
                    fs.annotation.removeIf(constraint -> removedFacts.contains(edgeKey(constraint)));
                }
                if (deleteOnly) {
                    qp.getFsIndices().entrySet().removeIf(entry -> removedFacts.contains(edgeKey(entry.getValue())));
                }

                if (deleteOnly && !removedConstraintSet.isEmpty()) {
                    removedAnnotationsByRule.put(r.getRuleIndex(), removedConstraintSet);
                }

                if (deleteOnly) {
                    LOGGER.debug("Removed the following facts:");
                    String removed = String.join("\n",removedFacts.stream().map(Object::toString).collect(Collectors.toList()));
                    LOGGER.debug("\n" + removed);
                }

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
                LinkedHashSet<GraphConstraint> factsForRule = new LinkedHashSet<>();
                for (Integer akey : annotation.keySet()) {
                    GraphConstraint addedConstraint = annotation.get(akey);
                    fs.annotation.add(addedConstraint);
                    factsForRule.add(addedConstraint);
                    addedFacts.add(addedConstraint.toString());
                }
                if (!factsForRule.isEmpty()) {
                    addedAnnotationsByRule.put(r.getRuleIndex(), factsForRule);
                    recordAddedAnnotations(fs, r.getRuleIndex(), factsForRule);
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
    public Boolean variableIsAssigned(QueryParserResult qpr, Solution solutionKey, String key) {
        return qpr.result.get(solutionKey).keySet().contains(key);
    }


    public String replaceVars(QueryParserResult qpr, Solution solutionKey, String value) {
        Matcher matcher = HelperMethods.fsNodePattern.matcher(value);
        Pattern lexPattern = Pattern.compile("(lex\\((.*?),(.*?)\\))");

        String replacedFsVars = value;
        StringBuffer sb = new StringBuffer();

        //Replace fsNode variables
        while (matcher.find()) {

            String replaceString = matcher.group(1);
            String type = "";
            boolean typed = false;

            if (replaceString.split("_").length == 2)
            {
                 replaceString = matcher.group(1).split("_")[0];
                 type = "_" + matcher.group(1).split("_")[1];
            }

            if (qpr.result.get(solutionKey).containsKey(replaceString)) {
                String key = qpr.result.get(solutionKey).get(replaceString).keySet().stream().findAny().get();


                matcher.appendReplacement(sb, key+type);
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
            String key2 = lookupValueBinding(solutionKey, matcher2.group(1), qpr.valueBindings);
            if (key2 != null) {
                matcher2.appendReplacement(sb2, key2);
            }
        }

        matcher2.appendTail(sb2);
        replacedFsVars = sb2.toString();


        //search dictionary
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

    private String edgeKey(GraphConstraint constraint) {
        String reading = constraint.getReading() == null ? "" : constraint.getReading().toString();
        return String.valueOf(constraint.getFsNode()) + "|" +
                String.valueOf(constraint.getRelationLabel()) + "|" +
                String.valueOf(constraint.getFsValue()) + "|" + reading;
    }

    public String returnUnusedVar() {
        int i = usedKeys.size();

        while (usedKeys.contains(Integer.toString(i))) {
            i++;
        }

        usedKeys.add(Integer.toString(i));
        return Integer.toString(i);
    }

    private String returnUnusedAnnotationNode() {
        int i = 1;
        String candidate;
        do {
            candidate = "a" + i++;
        } while (usedKeys.contains(candidate));

        usedKeys.add(candidate);
        return candidate;
    }

    private String lookupValueBinding(Solution solutionKey,
                                      String valueVar,
                                      HashMap<Solution, HashMap<String, String>> valueBindings) {
        HashMap<String, String> exactMatch = valueBindings.get(solutionKey);
        if (exactMatch != null && exactMatch.containsKey(valueVar)) {
            return exactMatch.get(valueVar);
        }

        String bestMatch = null;
        int bestSize = -1;

        for (Solution key : valueBindings.keySet()) {
            if (solutionKey.getSolutionKeys().containsAll(key.getSolutionKeys()) && key.getSolutionKeys().size() > bestSize) {
                HashMap<String, String> bindings = valueBindings.get(key);
                if (bindings != null && bindings.containsKey(valueVar)) {
                    bestMatch = bindings.get(valueVar);
                    bestSize = key.size();
                }
            }
        }

        return bestMatch;
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
            fileString = stripEmbeddedDefinitions(fileString);

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

                if ((c =='=' || c == '?') &&
                        (fileString.charAt(i + 1) == '=' || fileString.charAt(i + 1) == '-')
                        && fileString.charAt(i + 2) == '>')
                {
                    boolean rewrite = false;
                    boolean branch = false;
                    boolean questionDelete = false;
                    if (c == '?' && fileString.charAt(i + 1) == '=')
                    {
                        branch = true;
                    } else if (c == '?' && fileString.charAt(i + 1) == '-')
                    {
                        questionDelete = true;
                    } else if (fileString.charAt(i + 1) == '-')
                    {
                      rewrite = true;
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

                    String leftString = left.toString().trim();
                    leftString = leftString.replaceAll("\\s+"," ");
                    String rightString = right.toString().trim();
                    rightString = rightString.replaceAll("\\s+"," ");

                    Rule r = new Rule(leftString,rightString, rewrite,branch,questionDelete);
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
        appliedRules = new LinkedHashSet<>();
        addedAnnotationsByRule = new LinkedHashMap<>();
        addedAnnotationsByStructure = new IdentityHashMap<>();
        removedAnnotationsByRule = new LinkedHashMap<>();
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

    public LinkedHashSet<Rule> getAppliedRules() {
        return this.appliedRules;
    }

    public LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> getAddedAnnotationsByRule() {
        return this.addedAnnotationsByRule;
    }

    public LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> getAddedAnnotationsByRule(LinguisticStructure structure) {
        LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> annotations = addedAnnotationsByStructure.get(structure);
        return annotations == null ? new LinkedHashMap<>() : annotations;
    }

    public LinkedHashMap<Integer, LinkedHashSet<GraphConstraint>> getRemovedAnnotationsByRule() {
        return this.removedAnnotationsByRule;
    }

    public void setAppliedRules(LinkedHashSet<Rule> appliedRules) {
        this.appliedRules = appliedRules;
    }

    public TemplateRegistry getTemplateRegistry() {
        return templateRegistry;
    }

    public HierarchyRegistry getHierarchyRegistry() {
        return hierarchyRegistry;
    }

    private boolean isTemplateDefinitionLine(String trimmed) {
        return !trimmed.startsWith("//")
                && trimmed.contains(":=")
                && !trimmed.contains("::=")
                && !trimmed.contains("==>")
                && !trimmed.contains("=->")
                && !trimmed.contains("?=>")
                && !trimmed.contains("?->");
    }

    private boolean isHierarchyDefinitionLine(String trimmed) {
        return !trimmed.startsWith("//") && trimmed.contains("::=");
    }

    private String stripEmbeddedDefinitions(String fileString) {
        EmbeddedDefinitionExtractor.Result result = EmbeddedDefinitionExtractor.extract(fileString);
        this.templateRegistry = result.templateRegistry();
        this.hierarchyRegistry = result.hierarchyRegistry();
        return result.query();
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
         LOGGER.error("Couldn't read rule file");
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
                    LOGGER.error("Failed to parse rule right-hand side");
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
