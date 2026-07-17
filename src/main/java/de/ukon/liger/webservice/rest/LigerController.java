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

package de.ukon.liger.webservice.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ukon.liger.analysis.RuleParser.Rule;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.HierarchyParser;
import de.ukon.liger.analysis.QueryParser.HierarchyRegistry;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.SolutionKey;
import de.ukon.liger.analysis.QueryParser.TemplateParser;
import de.ukon.liger.analysis.QueryParser.TemplateRegistry;
import de.ukon.liger.reasoning.AxiomExtractor;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.VariableHandler;
import de.ukon.liger.utilities.XLEStarter;
import de.ukon.liger.webservice.rest.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
public class LigerController {
    private final static Logger LOGGER = Logger.getLogger(LigerController.class.getName());

    @Autowired
    private LigerService ligerService;

    public LigerController(){

    };


    /************************************************************************
     Methods for processing single sentences
     TODO: allow for switching between packed and unpacked f-structures
     ************************************************************************/


    /** This method is used for analyzying a multistage grammar. This only allows mcs from the grammar
     */
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/parse_xle", produces = "application/json", consumes = "application/json")
    public LigerSolutionAnnotationResponse parseXLE(@RequestBody LigerRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEStarter starter = new XLEStarter();
        starter.generateXLEStarterFile();
        XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);

        String fsProlog = parser.parse2Prolog(request.sentence);

        List<LinguisticStructure> fsList = parser.parseSingle(request.sentence, true);

       // System.out.println(fs.getSubstructures("GLUE"));

        GlueSemantics sem = new GlueSemantics();
        List<LigerSolutionAnnotation> solutions = new ArrayList<>();

        for (int i = 0; i < fsList.size(); i++) {
            LinguisticStructure fs = fsList.get(i);
            LOGGER.fine(fs.constraints.toString());
            sem.annotateSyntheticMcIndices(fs);
            String semString = sem.returnMultiStageMeaningConstructors(fs);
            solutions.add(new LigerSolutionAnnotation(
                    solutionKeyFor(fs, i),
                    new LigerWebGraph(fs.constraints, fs.annotation),
                    fs.toJson(),
                    new LinkedHashSet<>(),
                    semString,
                    countMeaningConstructorSets(semString),
                    new ArrayList<>()
            ));
        }

        LOGGER.info("Finished LiGER annotation. Returning results...");
        return new LigerSolutionAnnotationResponse(request.sentence, solutions);
    }


    /** The method for applying a hybrid semantic analysis with meaning constructors in the grammar and additiona mcs from Liger
     */
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rules_xle", produces = "application/json", consumes = "application/json")
    public LigerSolutionAnnotationResponse applyRuleRequestXLE2(@RequestBody LigerRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEStarter starter = new XLEStarter();
        starter.generateXLEStarterFile();
        XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);

        //Parse sentence
        List<LinguisticStructure> fsList = parser.parseSingle(request.sentence,true);


        //Apply rewrite rules
        RuleParser rp = new RuleParser(fsList, request.ruleString);

        GlueSemantics sem = new GlueSemantics();
        List<LigerSolutionAnnotation> solutions = new ArrayList<>();

        for (int i = 0; i < fsList.size(); i++) {
            LinguisticStructure fs = fsList.get(i);
            LinkedHashSet<LigerRule> appliedLigerRules = new LinkedHashSet<>();

            rp.addAnnotation2(fs);
            sem.annotateSyntheticMcIndices(fs);

            LigerWebGraph lg = new LigerWebGraph(fs.constraints, fs.annotation);


            for (Rule r : rp.getAppliedRules()) {
                appliedLigerRules.add(new LigerRule(r.toString(), r.getRuleIndex(), r.getLineNumber()));
            }
            String currentSemString = sem.returnMeaningConstructors(fs, !starter.isGlue, false, true);


            //Extract axioms
            AxiomExtractor axiomExtractor = new AxiomExtractor();

            String logicType = "fof";
            if (request.logicType != null && !request.logicType.isEmpty()) {
                logicType = request.logicType;
            }

            List<String> axioms = axiomExtractor.extractAxiomsFromLigerAnnotations(fs, logicType);

            solutions.add(new LigerSolutionAnnotation(
                    solutionKeyFor(fs, i),
                    lg,
                    fs.toJson(),
                    appliedLigerRules,
                    currentSemString,
                    countMeaningConstructorSets(currentSemString),
                    axioms
            ));
        }

        LOGGER.info("Finished LiGER annotation. Returning results...");
        return new LigerSolutionAnnotationResponse(request.sentence, solutions);
    }


    /** Method that takes a linguistic structure sjon and a rule string and applies the rules to the linguistic structure.
     * This assumes the sentence field to be a json string serializable into a Linguistic structure.
     */

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rules_base", produces = "application/json", consumes = "application/json")
    public LigerRuleAnnotation applyRulesToLingStructure(@RequestBody LigerRequest request) throws IOException {


        ObjectMapper mapper = new ObjectMapper();

        //Assume that sentence is a json String describing a linguistic structure

        //parse Json string to hashmap
        LinkedHashMap lsmap = mapper.readValue(request.sentence, LinkedHashMap.class);

        List<LinguisticStructure> fsList = new ArrayList<>();
        LinguisticStructure ls = LinguisticStructure.parseFromJson(lsmap);

        ls.cp.choiceNodes = new ArrayList<>();
        ls.cp.choices.add(ls.cp.rootChoice);

        fsList.add(ls);

        //Apply rewrite rules
        RuleParser rp = new RuleParser(fsList, request.ruleString);

        GlueSemantics sem = new GlueSemantics();
        List<String> semString = new ArrayList<>();
        LigerWebGraph lg = null;
        List<String> axioms = null;

        LinkedHashMap<String,LinkedHashSet<LigerRule>> appliedRules = new LinkedHashMap<>();

        for (LinguisticStructure fs : fsList) {
            LinkedHashSet<LigerRule> appliedLigerRules = new LinkedHashSet<>();

            rp.addAnnotation2(fs);
            sem.annotateSyntheticMcIndices(fs);

            lg = new LigerWebGraph(fs.constraints, fs.annotation);


            for (Rule r : rp.getAppliedRules()) {
                appliedLigerRules.add(new LigerRule(r.toString(), r.getRuleIndex(), r.getLineNumber()));
            }
            appliedRules.put(fs.local_id, appliedLigerRules);
            semString.add(sem.returnMeaningConstructors(fs, false, false, true));


            //Extract axioms
            AxiomExtractor axiomExtractor = new AxiomExtractor();

            String logicType = "fof";
            if (request.logicType != null && !request.logicType.isEmpty()) {
                logicType = request.logicType;
            }

            axioms = axiomExtractor.extractAxiomsFromLigerAnnotations(fs, logicType);

        }

        LOGGER.info("Finished LiGER annotation. Returning results...");
        return new LigerRuleAnnotation(lg,
                appliedRules.values().stream().findFirst().orElseGet(LinkedHashSet::new),
                String.join("\n", semString), axioms);
    }

    private String solutionKeyFor(LinguisticStructure fs, int index) {
        if (fs != null && fs.local_id != null && !fs.local_id.isBlank()) {
            return fs.local_id;
        }

        return "solution-" + (index + 1);
    }

    private int countMeaningConstructorSets(String meaningConstructors) {
        if (meaningConstructors == null || meaningConstructors.isBlank()) {
            return 0;
        }

        int count = 0;
        int depth = 0;

        for (String line : meaningConstructors.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("//")) {
                continue;
            }

            if ("{".equals(trimmed)) {
                if (depth == 0) {
                    count++;
                }
                depth++;
            } else if ("}".equals(trimmed)) {
                depth = Math.max(0, depth - 1);
            }
        }

        return count;
    }

    @CrossOrigin
    @PostMapping(value = "/parse_uploaded_structure", produces = "application/json", consumes = "application/json")
    public LigerRuleAnnotation parseUploadedStructure(@RequestBody LigerStructureUploadRequest request) {
        try {
            return renderUploadedStructure(request);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @CrossOrigin
    @PostMapping(value = "/render_graph", produces = "application/json", consumes = "application/json")
    public LigerRuleAnnotation renderGraph(@RequestBody LigerStructureUploadRequest request) {
        try {
            return renderUploadedStructure(request);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @CrossOrigin
    @PostMapping(value = "/apply_rules_uploaded_structure", produces = "application/json", consumes = "application/json")
    public LigerRuleAnnotation applyRulesUploadedStructure(@RequestBody LigerStructureRuleRequest request) {
        try {
            return applyRulesToUploadedStructure(request);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @CrossOrigin
    @PostMapping(value = "/query_uploaded_structure", produces = "application/json", consumes = "application/json")
    public LigerStructureQueryResponse queryUploadedStructure(@RequestBody LigerStructureQueryRequest request) {
        LinguisticStructure fs;
        try {
            fs = parseUploadedLinguisticStructure(new LigerStructureUploadRequest(request.content, request.format, request.id));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }

        QueryRequestBundle queryBundle = stripEmbeddedQueryDefinitions(request.query);
        LOGGER.info("Graph inspector query request: templateCount="
                + queryBundle.templateRegistry().getTemplates().size()
                + ", hierarchyCount=" + queryBundle.hierarchyRegistry().getHierarchies().size()
                + ", sanitizedQuery='" + queryBundle.query() + "'");
        QueryParser qp = new QueryParser(
                queryBundle.query(),
                fs,
                queryBundle.templateRegistry(),
                queryBundle.hierarchyRegistry());

        List<QueryParserResult> results = qp.parseQueryWithTemplates(queryBundle.query());
        LOGGER.info("Graph inspector query parse completed: resultCount=" + results.size());

        QueryMatchSummary matchSummary = summarizeQueryMatches(results);
        LigerWebGraph graph = new LigerWebGraph(fs.constraints, fs.annotation);
        highlightQueryMatches(graph, matchSummary.nodeIds());

        return new LigerStructureQueryResponse(Boolean.toString(matchSummary.matchCount() > 0), matchSummary.matchCount(), graph, matchSummary.solutions());
    }

    @CrossOrigin
    @PostMapping(value = "/merge_uploaded_structures", produces = "application/json", consumes = "application/json")
    public LigerMergeResponse mergeUploadedStructures(@RequestBody LigerStructureMergeRequest request) {
        LinguisticStructure syntax = parseStructureMap(request.syntax);
        LinguisticStructure drs = parseStructureMap(request.drs);
        LinguisticStructure merged = LinguisticStructureMerger.merge(syntax, drs);
        return new LigerMergeResponse(new LigerWebGraph(merged.constraints, merged.annotation),merged.toJson());
    }

    private QueryMatchSummary summarizeQueryMatches(List<QueryParserResult> results) {
        LinkedHashMap<String, LigerQuerySolution> uniqueSolutions = new LinkedHashMap<>();
        Set<String> nodeIds = new LinkedHashSet<>();

        for (QueryParserResult result : results) {
            if (!Boolean.TRUE.equals(result.isSuccess)) {
                continue;
            }

            for (Map.Entry<Set<SolutionKey>, HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>>> entry : result.result.entrySet()) {
                String signature = solutionSignature(entry.getKey());
                if (uniqueSolutions.containsKey(signature)) {
                    continue;
                }

                uniqueSolutions.put(signature, buildSolutionView(signature, entry.getValue()));
                collectHighlightIds(entry.getKey(), nodeIds);
            }
        }

        return new QueryMatchSummary(uniqueSolutions.size(), nodeIds, new ArrayList<>(uniqueSolutions.values()));
    }

    private LigerQuerySolution buildSolutionView(String signature,
                                                 HashMap<String, HashMap<String, HashMap<Integer, GraphConstraint>>> binding) {
        LinkedHashMap<String, LinkedHashMap<String, List<String>>> variableMap = new LinkedHashMap<>();

        for (Map.Entry<String, HashMap<String, HashMap<Integer, GraphConstraint>>> variableEntry : binding.entrySet()) {
            LinkedHashMap<String, List<String>> nodeMap = new LinkedHashMap<>();

            for (Map.Entry<String, HashMap<Integer, GraphConstraint>> referenceEntry : variableEntry.getValue().entrySet()) {
                List<String> constraints = referenceEntry.getValue().values().stream()
                        .map(this::constraintSummary)
                        .sorted()
                        .collect(Collectors.toList());
                nodeMap.put(referenceEntry.getKey(), constraints);
            }

            variableMap.put(variableEntry.getKey(), nodeMap);
        }

        return new LigerQuerySolution(signature, variableMap);
    }

    private String constraintSummary(GraphConstraint constraint) {
        return constraint.getRelationLabel() + "=" + constraint.getFsValue();
    }

    private void collectHighlightIds(Set<SolutionKey> solutionKeys,
                                     Set<String> nodeIds) {
        for (SolutionKey solutionKey : solutionKeys) {
            nodeIds.add(solutionKey.reference);
        }
    }

    private void highlightQueryMatches(LigerWebGraph graph, Set<String> nodeIds) {
        for (LigerGraphComponent component : graph.graphElements) {
            if (component.data == null) {
                continue;
            }

            Object id = component.data.get("id");
            if (id != null && nodeIds.contains(String.valueOf(id))) {
                component.data.put("query_selector", "query-match");
            }
        }
    }

    private String solutionSignature(Set<SolutionKey> solutionKeys) {
        return solutionKeys.stream()
                .map(key -> key.variable + "=" + key.reference)
                .sorted()
                .collect(Collectors.joining("|"));
    }

    private record QueryMatchSummary(int matchCount, Set<String> nodeIds, List<LigerQuerySolution> solutions) {}

    private QueryRequestBundle stripEmbeddedQueryDefinitions(String query) {
        TemplateRegistry templateRegistry = new TemplateRegistry();
        HierarchyRegistry hierarchyRegistry = new HierarchyRegistry();

        if (query == null || query.isBlank()) {
            return new QueryRequestBundle("", templateRegistry, hierarchyRegistry);
        }

        StringBuilder templateDefinitions = new StringBuilder();
        StringBuilder hierarchyDefinitions = new StringBuilder();
        StringBuilder sanitized = new StringBuilder();

        String[] lines = query.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (isHierarchyDefinitionLine(trimmed)) {
                hierarchyDefinitions.append(trimmed).append(' ');
            } else if (isTemplateDefinitionLine(trimmed)) {
                templateDefinitions.append(trimmed).append(' ');
            } else if (trimmed.startsWith("//")) {
                continue;
            } else {
                sanitized.append(line);
            }

            if (i < lines.length - 1) {
                sanitized.append('\n');
            }
        }

        if (!templateDefinitions.isEmpty()) {
            templateRegistry = new TemplateParser().parse(templateDefinitions.toString());
        }
        if (!hierarchyDefinitions.isEmpty()) {
            hierarchyRegistry = new HierarchyParser().parse(hierarchyDefinitions.toString());
        }

        LOGGER.info("Stripped embedded query definitions: templateDefs='" + templateDefinitions + "', hierarchyDefs='"
                + hierarchyDefinitions + "', sanitized='" + sanitized.toString().trim() + "'");
        return new QueryRequestBundle(sanitized.toString().trim(), templateRegistry, hierarchyRegistry);
    }

    private boolean isTemplateDefinitionLine(String trimmed) {
        return !trimmed.startsWith("//")
                && trimmed.contains(":=")
                && !trimmed.contains("::=")
                && !trimmed.contains("==>")
                && !trimmed.contains("=->")
                && !trimmed.contains("+->");
    }

    private boolean isHierarchyDefinitionLine(String trimmed) {
        return !trimmed.startsWith("//") && trimmed.contains("::=");
    }

    private record QueryRequestBundle(String query, TemplateRegistry templateRegistry, HierarchyRegistry hierarchyRegistry) {}

    private LinguisticStructure parseUploadedLinguisticStructure(LigerStructureUploadRequest request) throws IOException {
        String format = request.format == null ? "json" : request.format.trim().toLowerCase(Locale.ROOT);
        String id = (request.id == null || request.id.isBlank()) ? "uploaded" : request.id;

        if ("prolog".equals(format) || "pl".equals(format)) {
            XLEoperator parser = new XLEoperator(new VariableHandler());
            return parser.fsString2Java(request.content, id).values().stream().findFirst()
                    .orElseThrow(() -> new IOException("Could not parse uploaded prolog structure."));
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            LinkedHashMap lsmap = mapper.readValue(request.content, LinkedHashMap.class);
            if (lsmap.containsKey("graphElements")) {
                throw new IOException("Graph-only JSON is not supported. Upload a LinguisticStructure JSON instead.");
            }

            LinguisticStructure ls = LinguisticStructure.parseFromJson(lsmap);

            if (ls.local_id == null || ls.local_id.isBlank()) {
                ls.local_id = id;
            }

            if (ls.cp == null) {
                ls.cp = new de.ukon.liger.packing.ChoiceSpace();
            }

            return ls;
        } catch (Exception e) {
            if (e instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("Invalid LinguisticStructure JSON. Expected id, text, constraints, annotations, and choiceSpace.", e);
        }
    }

    private LigerRuleAnnotation renderUploadedStructure(LigerStructureUploadRequest request) throws IOException {
        LinguisticStructure fs = parseUploadedLinguisticStructure(request);
        LigerRuleAnnotation response = new LigerRuleAnnotation(
                fs.text,
                new LigerWebGraph(fs.constraints, fs.annotation),
                new LinkedHashSet<>(),
                "",
                fs.annotation.size(),
                new ArrayList<>(),
                fs.toJson()
        );
        response.highlightedNodeIds = collectHighlightedNodeIds(fs);
        return response;
    }

    private LigerRuleAnnotation applyRulesToUploadedStructure(LigerStructureRuleRequest request) throws IOException {
        LinguisticStructure fs = parseUploadedLinguisticStructure(
                new LigerStructureUploadRequest(request.content, request.format, request.id)
        );

        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, request.ruleString == null ? "" : request.ruleString);
        rp.addAnnotation2(fs);

        LinkedHashSet<LigerRule> appliedRules = new LinkedHashSet<>();
        for (Rule r : rp.getAppliedRules()) {
            appliedRules.add(new LigerRule(r.toString(), r.getRuleIndex(), r.getLineNumber()));
        }

        LigerRuleAnnotation response = new LigerRuleAnnotation(
                new LigerWebGraph(fs.constraints, fs.annotation),
                appliedRules,
                fs.toJson()
        );
        response.sentence = fs.text;
        response.highlightedNodeIds = collectHighlightedNodeIds(fs);
        return response;
    }

    private LinkedHashSet<String> collectHighlightedNodeIds(LinguisticStructure fs) {
        LinkedHashSet<String> highlightedNodeIds = new LinkedHashSet<>();

        if (fs == null || fs.annotation == null) {
            return highlightedNodeIds;
        }

        for (GraphConstraint constraint : fs.annotation) {
            if (constraint == null) {
                continue;
            }

            String sourceNode = constraint.getFsNode();
            if (sourceNode != null && !sourceNode.isBlank()) {
                highlightedNodeIds.add(sourceNode);
            }

            Object value = constraint.getFsValue();
            if (value != null) {
                String targetNode = String.valueOf(value);
                if (de.ukon.liger.utilities.HelperMethods.isInteger(targetNode)) {
                    highlightedNodeIds.add(targetNode);
                }
            }
        }

        return highlightedNodeIds;
    }

    private LinguisticStructure parseStructureMap(LinkedHashMap<String, Object> json) {
        if (json == null) {
            return null;
        }

        LinguisticStructure structure = LinguisticStructure.parseFromJson(json);
        if (structure.local_id == null || structure.local_id.isBlank()) {
            structure.local_id = "uploaded";
        }
        if (structure.cp == null) {
            structure.cp = new de.ukon.liger.packing.ChoiceSpace();
        }
        return structure;
    }

    private List<LigerGraphComponent> buildUploadedGraphElements(LinguisticStructure fs) {
        LinkedHashMap<String, LigerGraphComponent> nodes = new LinkedHashMap<>();
        List<LigerGraphComponent> edges = new ArrayList<>();
        Map<String, String> projectionByNode = new LinkedHashMap<>();
        Map<String, String> nodeTypeByNode = new LinkedHashMap<>();
        Map<String, LinkedHashMap<String, String>> avpByNode = new LinkedHashMap<>();
        Set<String> allNodeIds = new LinkedHashSet<>();

        List<GraphConstraint> allConstraints = new ArrayList<>();
        allConstraints.addAll(fs.constraints);
        allConstraints.addAll(fs.annotation);

        for (GraphConstraint constraint : allConstraints) {
            String sourceNode = constraint.getFsNode();
            String sourceProjection = constraint.getProj();
            if (sourceNode != null && !sourceNode.isBlank()) {
                projectionByNode.putIfAbsent(sourceNode, sourceProjection);
                allNodeIds.add(sourceNode);
            }

            if ("NODE_TYPE".equals(constraint.getRelationLabel())) {
                nodeTypeByNode.put(sourceNode, String.valueOf(constraint.getFsValue()));
                continue;
            }

            String relationLabel = constraint.getRelationLabel();
            String relationValue = String.valueOf(constraint.getFsValue());

            if (de.ukon.liger.utilities.HelperMethods.isInteger(constraint.getFsValue())) {
                String targetRaw = relationValue;
                String targetProjection = projectionByNode.get(targetRaw);

                allNodeIds.add(targetRaw);
                nodes.putIfAbsent(sourceNode, createNode(sourceNode, sourceProjection, nodeTypeByNode.get(sourceNode), avpByNode.get(sourceNode)));
                nodes.putIfAbsent(targetRaw, createNode(targetRaw, targetProjection, nodeTypeByNode.get(targetRaw), avpByNode.get(targetRaw)));

                edges.add(new LigerWebEdge(
                        "edge-" + edges.size(),
                        sourceNode,
                        targetRaw,
                        relationLabel,
                        "edge"
                ));
            } else {
                avpByNode.computeIfAbsent(sourceNode, k -> new LinkedHashMap<>())
                        .put(relationLabel, relationValue);
                nodes.putIfAbsent(sourceNode, createNode(sourceNode, sourceProjection, nodeTypeByNode.get(sourceNode), avpByNode.get(sourceNode)));
            }
        }

        for (String nodeId : allNodeIds) {
            String projection = projectionByNode.get(nodeId);
            nodes.put(nodeId, createNode(nodeId, projection, nodeTypeByNode.get(nodeId), avpByNode.get(nodeId)));
        }

        List<LigerGraphComponent> graphElements = new ArrayList<>();
        graphElements.addAll(nodes.values());
        graphElements.addAll(edges);
        return graphElements;
    }

    private LigerWebNode createNode(String nodeId, String projection, String nodeType, Map<String, String> avp) {
        String resolvedNodeType = nodeType;
        String label = resolveNodeLabel(nodeId, nodeType, avp);
        if (avp != null && !avp.isEmpty()) {
            return new LigerWebNode(nodeId, resolvedNodeType, label, new HashMap<>(avp));
        }
        return new LigerWebNode(nodeId, resolvedNodeType, label);
    }

    private String resolveNodeLabel(String nodeId, String nodeType, Map<String, String> avp) {
        if (nodeType != null) {
            if (nodeType.equals("input") || nodeType.equals("cnode") || nodeType.equals("gnode") || nodeType.equals("annotation") || nodeType.equals("rule")) {
                if (avp != null) {
                    String name = avp.get("NAME");
                    if (name != null && !name.isBlank()) {
                        return name;
                    }
                }
            }
        }
        return nodeId;
    }


    /************************************************************************
     Methods for batch processing
     ************************************************************************/

//    //Method for applying hybrid semantic analysis to a testsuite
//    @CrossOrigin
//    //(origins = "http://localhost:63342")
//    @PostMapping(value = "/apply_rules_to_batch-deprecated", produces = "application/json", consumes = "application/json")
//    public LigerBatchParsingAnalysis applyRulesToTestsuite(@RequestBody LigerMultipleRequest request) throws IOException {
//
//        //    System.out.println(request.sentence);
//        //   System.out.println(request.ruleString);
//        XLEStarter starter = new XLEStarter();
//        starter.generateXLEStarterFile();
//        XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);
//
//        List<LigerGraphComponent> appliedRulesGraph = new ArrayList<>();
//
//        boolean rules = false;
//
//        RuleParser rp = null;
//
//
//        rp = new RuleParser(request.ruleString);
//
//
//        HashMap<String,LigerRuleAnnotation> output = new HashMap<>();
//        List<List<LigerRule>> allAppliedRules = new ArrayList<>();
//
//        StringBuilder reportBuilder = new StringBuilder();
//
//        if (!appliedRulesGraph.isEmpty()){
//            rules = true;
//        }
//
//        if (!rules){
//            reportBuilder.append("No rewrite rules applied to testsuite!");
//        }
//
//        reportBuilder.append(System.lineSeparator());
//        reportBuilder.append("ID:     Applied rules:     Added facts:     No of meaning constructors:\n");
//
//        List<String> keys = new ArrayList<>(request.sentences.keySet());
//
//        //sort keys by string final number
//
//        keys.sort(new Comparator<String>() {
//            @Override
//            public int compare(String s1, String s2) {
//                // Extract the numbers from the end of the strings
//                int num1 = Integer.parseInt(s1.replaceAll("\\D", ""));
//                int num2 = Integer.parseInt(s2.replaceAll("\\D", ""));
//
//                // Compare the numbers
//                return Integer.compare(num1, num2);
//            }
//        });
//
//        GlueSemantics sem = new GlueSemantics();
//
//        //Parsing routine
//        for (int i = 0; i < keys.size(); i++) {
//
//            String id = keys.get(i);
//            String sentence = request.sentences.get(id);
//
//            LigerWebGraph lg = null;
//            List<String> semString = new ArrayList<>();
//
//            List<LinguisticStructure> fsList = parser.parseSingle(sentence);
//            List<LigerRule> appliedLigerRules = new ArrayList<>();
//
//            int addedAnnotations = 0;
//
//            for (Rule r : rp.getAppliedRules()) {
//                appliedLigerRules.add(new LigerRule(r.toString(), r.getRuleIndex(), r.getLineNumber()));
//            }
//
//            for (LinguisticStructure fs : fsList) {
//
//                if (!request.ruleString.equals("")) {
//                    rp.addAnnotation2(fs);
//                }
//                // rp.addAnnotation2(fs);
//
//                semString.add(sem.returnMeaningConstructors(fs, !starter.isGlue, false));
//                addedAnnotations = addedAnnotations + fs.annotation.size();
//            }
//                String currentSemString = String.join("\n", semString);
//
//
//                List<String> meaningConstructors = List.of(currentSemString.split("\n"));
//                //remove lines which equal }\n or {\n
//                meaningConstructors = meaningConstructors.stream().filter(s -> !s.equals("}") && !s.equals("{") && !s.startsWith("//")).collect(Collectors.toList());
//
//                lg = new LigerWebGraph(fsList.get(0).constraints, fsList.get(0).annotation);
//
//                reportBuilder.append(String.format("%s\t\t%s\t\t%s\t\t%s", id, appliedLigerRules.size(), addedAnnotations, meaningConstructors.size()));
//                reportBuilder.append(System.lineSeparator());
//
//                output.put(id, new LigerRuleAnnotation(lg, appliedLigerRules, currentSemString));
//                allAppliedRules.add(appliedLigerRules);
//
//
//
//        }
//
//        appliedRulesGraph = createLigerAnnotationGraph(request.sentences,rp, allAppliedRules);
//
//        return new LigerBatchParsingAnalysis(output,appliedRulesGraph,reportBuilder.toString());
//        }

    //Method for applying hybrid semantic analysis to a testsuite
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rules_to_batch", produces = "application/json", consumes = "application/json")
    public LigerBatchParsingAnalysis applyRulesToTestsuiteNew(@RequestBody LigerMultipleRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEStarter starter = new XLEStarter();
        starter.generateXLEStarterFile();
        XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);

        List<LigerGraphComponent> appliedRulesGraph = new ArrayList<>();

        boolean rules = false;

        RuleParser rp = null;

        rp = new RuleParser(request.ruleString);

        HashMap<String,LigerRuleAnnotation> output = new HashMap<>();
        HashMap<String,LinkedHashSet<LigerRule>> allAppliedRules = new HashMap();

        StringBuilder reportBuilder = new StringBuilder();

        if (!appliedRulesGraph.isEmpty()){
            rules = true;
        }

        if (!rules){
            reportBuilder.append("No rewrite rules applied to testsuite!");
        }

        reportBuilder.append(System.lineSeparator());
        reportBuilder.append("ID:     Applied rules:     Added facts:     No of meaning constructors:\n");

        List<String> keys = new ArrayList<>(request.sentences.keySet());

        //sort keys by string final number

        keys.sort(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // Extract the numbers from the end of the strings
                int num1 = Integer.parseInt(s1.replaceAll("\\D", ""));
                int num2 = Integer.parseInt(s2.replaceAll("\\D", ""));

                // Compare the numbers
                return Integer.compare(num1, num2);
            }
        });

        GlueSemantics sem = new GlueSemantics();

        //Parsing routine
        for (int i = 0; i < keys.size(); i++) {

            String id = keys.get(i);
            String sentence = request.sentences.get(id);

            LigerWebGraph lg = null;
            List<String> axioms = new ArrayList<>();

            List<String> semString = new ArrayList<>();

            List<LinguisticStructure> fsList = parser.parseSingle(sentence);

            int addedAnnotations = 0;


            LinkedHashSet<LigerRule> appliedLigerRules = new LinkedHashSet<>();

            for (LinguisticStructure fs : fsList) {

                if (!request.ruleString.equals("")) {
                    rp.addAnnotation2(fs);
                }

                // rp.addAnnotation2(fs);
                sem.annotateSyntheticMcIndices(fs);

                for (Rule r : rp.getAppliedRules()) {
                    appliedLigerRules.add(new LigerRule(r.toString(), r.getRuleIndex(), r.getLineNumber()));
                }

                AxiomExtractor axiomExtractor = new AxiomExtractor();

                String logicType = "fof";
                if (request.logicType != null && !request.logicType.isEmpty()) {
                    logicType = request.logicType;
                }

                List<String> currentAxioms = axiomExtractor.extractAxiomsFromLigerAnnotations(fs, logicType);

                if (!(currentAxioms == null) && !currentAxioms.isEmpty()) {
                    axioms.addAll(currentAxioms.stream()
                            .filter(x -> !axioms.contains(x)).collect(Collectors.toList()));
                }

                semString.add(sem.returnMeaningConstructors(fs, !starter.isGlue, false, true));
                addedAnnotations = addedAnnotations + fs.annotation.size();
            }
            String currentSemString = String.join("\n", semString);


            List<String> meaningConstructors = List.of(currentSemString.split("\n"));
            //remove lines which equal }\n or {\n
            meaningConstructors = meaningConstructors.stream().filter(s -> !s.equals("}") && !s.equals("{") && !s.startsWith("//")).collect(Collectors.toList());

            lg = new LigerWebGraph(fsList.get(0).constraints, fsList.get(0).annotation);

            reportBuilder.append(String.format("%s\t\t%s\t\t%s\t\t%s", id, appliedLigerRules.size(), addedAnnotations, meaningConstructors.size()));
            reportBuilder.append(System.lineSeparator());

            output.put(id, new LigerRuleAnnotation(sentence, lg, appliedLigerRules, currentSemString, fsList.size(),axioms));
            allAppliedRules.put(id,appliedLigerRules);
        }

        appliedRulesGraph = createLigerAnnotationGraph(request.sentences,rp, allAppliedRules);

        LOGGER.info("Finished LiGER annotation. Returning results...");
        return new LigerBatchParsingAnalysis(output,appliedRulesGraph,reportBuilder.toString());
    }

        //Method for applying a multistage grammar to a testsuite
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/multistage_to_batch", produces = "application/json", consumes = "application/json")
    public LigerBatchParsingAnalysis applyMultiStageToTestsuite(@RequestBody LigerMultipleRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEStarter starter = new XLEStarter();
        starter.generateXLEStarterFile();
        XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);





        StringBuilder reportBuilder = new StringBuilder();

        HashMap<String,LigerRuleAnnotation> output = new HashMap<>();

        reportBuilder.append(System.lineSeparator());
        reportBuilder.append("ID:     No of meaning constructors:\n");

        List<String> keys = new ArrayList<>(request.sentences.keySet());

        //sort keys by string final number
        keys.sort(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // Extract the numbers from the end of the strings
                int num1 = Integer.parseInt(s1.replaceAll("\\D", ""));
                int num2 = Integer.parseInt(s2.replaceAll("\\D", ""));

                // Compare the numbers
                return Integer.compare(num1, num2);
            }
        });

        GlueSemantics sem = new GlueSemantics();

        for (int i = 0; i < keys.size(); i++) {

            String id = keys.get(i);
            String sentence = request.sentences.get(id);
            Integer numberOfMcs = 0;

            List<LinguisticStructure> fslist = parser.parseSingle(sentence);

            LigerWebGraph lg = null;
            List<String> semString = new ArrayList<>();

            for (LinguisticStructure fs : fslist) {

                lg = new LigerWebGraph(fs.constraints, fs.annotation);

               semString.add(sem.returnMultiStageMeaningConstructors(fs));
            }

            String mcs = String.join("\n", semString);

            if (mcs != null) {
                List<String> meaningConstructors = List.of(mcs.split("\n"));
                //remove lines which equal }\n or {\n
                numberOfMcs = meaningConstructors.stream().filter(s -> !s.equals("}") && !s.equals("{") && !s.startsWith("//")).collect(Collectors.toList()).size();
            } else {
                mcs = "";
            }
            reportBuilder.append(String.format("%s\t\t%s", id, numberOfMcs));
            reportBuilder.append(System.lineSeparator());

            //TODO fix treatment of axioms
            output.put(id,new LigerRuleAnnotation(null, null, mcs, new ArrayList<>()));

        }
        LOGGER.info("Finished LiGER annotation. Returning results...");
        return new LigerBatchParsingAnalysis(output,null,reportBuilder.toString());
    }

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rules_to_dependency_batch", produces = "application/json", consumes = "application/json")
    public LigerBatchParsingAnalysis applyRulesToStanzaTestsuite(@RequestBody LigerMultipleRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);

        List<LigerGraphComponent> appliedRulesGraph = new ArrayList<>();

        boolean rules = false;

        RuleParser rp = null;

        rp = new RuleParser(request.ruleString);

        HashMap<String,LigerRuleAnnotation> output = new HashMap<>();
        HashMap<String,LinkedHashSet<LigerRule>> allAppliedRules = new HashMap();

        StringBuilder reportBuilder = new StringBuilder();

        if (!appliedRulesGraph.isEmpty()){
            rules = true;
        }

        if (!rules){
            reportBuilder.append("No rewrite rules applied to testsuite!");
        }

        reportBuilder.append(System.lineSeparator());
        reportBuilder.append("ID:     Applied rules:     Added facts:     No of meaning constructors:\n");

        List<String> keys = new ArrayList<>(request.sentences.keySet());

        //sort keys by string final number

        keys.sort(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // Extract the numbers from the end of the strings
                int num1 = Integer.parseInt(s1.replaceAll("\\D", ""));
                int num2 = Integer.parseInt(s2.replaceAll("\\D", ""));

                // Compare the numbers
                return Integer.compare(num1, num2);
            }
        });

        GlueSemantics sem = new GlueSemantics();

        ObjectMapper mapper = new ObjectMapper();

        //Parsing routine
        for (int i = 0; i < keys.size(); i++) {

            String id = keys.get(i);
            String sentence = request.sentences.get(id);

            LigerWebGraph lg = null;
            List<String> semString = new ArrayList<>();

            LinkedHashMap lsmap = mapper.readValue(sentence, LinkedHashMap.class);

            List<LinguisticStructure> fsList = new ArrayList<>();
            LinguisticStructure ls = LinguisticStructure.parseFromJson(lsmap);

            ls.cp.choiceNodes = new ArrayList<>();
            ls.cp.choices.add(ls.cp.rootChoice);

            fsList.add(ls);

            int addedAnnotations = 0;

            LinkedHashSet<LigerRule> appliedLigerRules = new LinkedHashSet<>();

            for (LinguisticStructure fs : fsList) {

                if (!request.ruleString.equals("")) {
                    rp.addAnnotation2(fs);
                }

                // rp.addAnnotation2(fs);

                for (Rule r : rp.getAppliedRules()) {
                    appliedLigerRules.add(new LigerRule(r.toString(), r.getRuleIndex(), r.getLineNumber()));
                }

                semString.add(sem.returnMeaningConstructors(fs, false, false, true));
                addedAnnotations = addedAnnotations + fs.annotation.size();
            }
            String currentSemString = String.join("\n", semString);


            List<String> meaningConstructors = List.of(currentSemString.split("\n"));
            //remove lines which equal }\n or {\n
            meaningConstructors = meaningConstructors.stream().filter(s -> !s.equals("}") && !s.equals("{") && !s.startsWith("//")).collect(Collectors.toList());

            lg = new LigerWebGraph(fsList.get(0).constraints, fsList.get(0).annotation);

            reportBuilder.append(String.format("%s\t\t%s\t\t%s\t\t%s", id, appliedLigerRules.size(), addedAnnotations, meaningConstructors.size()));
            reportBuilder.append(System.lineSeparator());

            //TODO Make Stanza inference compatible
            output.put(id, new LigerRuleAnnotation(sentence, lg, appliedLigerRules, currentSemString, fsList.size(), new ArrayList<>()));
            allAppliedRules.put(id, appliedLigerRules);
        }

        appliedRulesGraph = createLigerAnnotationGraph(request.sentences,rp, allAppliedRules);

        LOGGER.info("Finished LiGER annotation. Returning results...");
        return new LigerBatchParsingAnalysis(output,appliedRulesGraph,reportBuilder.toString());
    }

    /************************************************************************
    Methods for file handling
     ************************************************************************/

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/list_grammars1", produces = "application/json", consumes = "application/json")
    public FileTree listGrammar1(@RequestBody GrammarString gs) throws Exception {

        String grammarPaths = Paths.get(gs.grammar).toString();
        FileTree ft = FileTree.buildFileTree(new File(grammarPaths));

        LOGGER.info("Listing files in " + gs.grammar + ".");

        return ft;
    }



    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/change_grammar", produces = "application/json", consumes = "application/json")
    public GrammarString changeGrammars(@RequestBody GrammarString gs) throws IOException {

        LOGGER.info("Changing grammar to " + gs.grammar + ".");

        XLEStarter starter = new XLEStarter();
        starter.updateGrammarPath(gs.grammar);
        starter.generateXLEStarterFile();

        LOGGER.info("Changed grammar to " + gs.grammar + ".");

        return new GrammarString("success");

    }


    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/load_rules", produces = "application/json", consumes = "application/json")
    public GrammarString loadRules(@RequestBody GrammarString gs) throws IOException {

        LOGGER.info("Loading rules from " + gs.grammar + ".");

        File f = new File(gs.grammar);
        //Load string content of file
        BufferedReader br = new BufferedReader(new java.io.FileReader(f));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
        }
        br.close();

        String ruleString = sb.toString();

        return new GrammarString(ruleString);

    }

    /************************************************************************
     Other stuff
     ************************************************************************/

    public List<LigerGraphComponent> createLigerAnnotationGraph(HashMap<String,String> sentences,  RuleParser rp, HashMap<String,LinkedHashSet<LigerRule>> allAppliedRules) {

        List<LigerGraphComponent> appliedRulesGraph = new ArrayList<>();

        for (int i = 0; i < rp.getRules().size(); i++) {
            Rule r = rp.getRules().get(i);
            HashMap<String, Object> node = new HashMap<>();
            node.put("rule", r.toString());
            node.put("id", i);
            node.put("line", r.getLineNumber());
            node.put("node_type", "rule");

            LigerGraphComponent lgc = new LigerGraphComponent(node);
            appliedRulesGraph.add(lgc);
        }

        HashMap<String, LigerGraphComponent> edges = new HashMap<>();

        for (String key  : allAppliedRules.keySet())
        {
            List<LigerRule> appliedRules =  allAppliedRules.get(key).stream().toList();

            for (int i = 0; i < appliedRules.size()-1; i = i + 1)
            {
                if (!edges.containsKey(appliedRules.get(i).index + "+" +
                        appliedRules.get(i+1).index))
                {

                    HashMap<String,Object> edge = new HashMap<>();
                    edge.put("source",appliedRules.get(i).index);

                    edge.put("target", appliedRules.get(i+1).index);
                    edge.put("timesUsed",1);
                    edge.put("edge_type","edge");


                    edge.put("id",appliedRules.get(i).index + "+" +
                            appliedRules.get(i+1).index);

                    LinkedHashSet<String> sentenceList = new LinkedHashSet<>();
                    sentenceList.add(sentences.get(key));

                    edge.put("sentences",sentenceList);

                    LigerGraphComponent lgc = new LigerGraphComponent(edge);

                    edges.put(appliedRules.get(i).index + "+" +
                            appliedRules.get(i+1).index, lgc);
                } else
                {
                    String edgeID = appliedRules.get(i).index + "+" +
                            appliedRules.get(i+1).index;

                    Object timesUsed = edges.get(edgeID).data.get("timesUsed");

                    edges.get(edgeID).data.put("timesUsed", (Integer) timesUsed + 1);
                    ((LinkedHashSet<String>) edges.get(edgeID).data.get("sentences")).add(sentences.get(key));
                }
            }
            // appliedRulesGraph.addAll(nodes.values());
        }
        appliedRulesGraph.addAll(edges.values());
        return appliedRulesGraph;
    }

}
