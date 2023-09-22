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

import de.ukon.liger.analysis.RuleParser.Rule;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.utilities.VariableHandler;
import de.ukon.liger.utilities.XLEStarter;
import de.ukon.liger.webservice.rest.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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

        //return new TestGraph(nodeList);
        //new Greeting(counter.incrementAndGet(),String.format(template,in));


    /*
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/semantics", produces = "application/json")
    public LigerWebGraph semanticsRequest2(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) throws IOException {

        UDoperator parser = new UDoperator();

        LinguisticStructure fs = parser.parseSingle(input);
        LOGGER.fine(fs.constraints.toString());
      //  System.out.println(fs.constraints);
        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, Paths.get(PathVariables.testPath + "testRulesUD1.txt"));
        rp.addAnnotation2(fs);

        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch (Exception e) {
          LOGGER.warning("Sorting annotation failed.");
        }

        GlueSemantics sem = new GlueSemantics();
        String semantics = sem.calculateSemantics(fs);


      //  return new TestGraph(nodeList,semantics);



        return new LigerWebGraph(fs.constraints,fs.annotation,semantics);
        //new Greeting(counter.incrementAndGet(),String.format(template,in));
    }

*/



    /*
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/semantics_xle", produces = "application/json")
    public LigerWebGraph semanticsRequestXLE(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) throws IOException {

        XLEoperator parser = new XLEoperator(new VariableHandler());

        LinguisticStructure fs = parser.parseSingle(input);
        LOGGER.fine(fs.constraints.toString());
        //  System.out.println(fs.constraints);
        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, Paths.get(PathVariables.testPath + "testRulesLFG9.txt"));
        rp.addAnnotation2(fs);

        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch (Exception e) {
            LOGGER.warning("Sorting annotation failed.");
        }
        GlueSemantics sem = new GlueSemantics();
        String semantics = sem.calculateSemantics(fs);

        //  return new TestGraph(nodeList,semantics);

        return new LigerWebGraph(fs.constraints,fs.annotation,semantics);
        //new Greeting(counter.incrementAndGet(),String.format(template,in));
    }
    */

/*
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rule_xle", produces = "application/json", consumes = "application/json")
    public LigerRuleAnnotation applyRuleRequestXLE(@RequestBody LigerRequest request) {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEoperator parser = new XLEoperator(new VariableHandler());

        LinguisticStructure fs = parser.parseSingle(request.sentence);
        LOGGER.fine(fs.constraints.toString());
        // System.out.println(fs.constraints);
        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, request.ruleString);
        rp.addAnnotation2(fs);

        GlueSemantics sem = new GlueSemantics();
        String semantics = sem.returnMeaningConstructors(fs);

        LigerWebGraph lg = new LigerWebGraph(fs.constraints,fs.annotation,semantics);

        return new LigerRuleAnnotation(lg,rp.getAppliedRules(),sem.returnMeaningConstructors(fs));
    }

 */

@CrossOrigin
    //(origins = "http://localhost:63342")
@PostMapping(value = "hybrid_analysis", produces = "application/json", consumes = "application/json")
public LigerRuleAnnotation hybridAnalysis(@RequestBody LigerRequest request) throws IOException {

    XLEStarter starter = new XLEStarter();
    starter.generateXLEStarterFile();
    XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);

    String fsProlog = parser.parse2Prolog(request.sentence);

    LinkedHashMap<String,LinguisticStructure> fsRef = parser.fsString2Java(fsProlog,"S1");

    LinguisticStructure fs = fsRef.get(fsRef.keySet().iterator().next());

    List<LinguisticStructure> fsList = new ArrayList<>();
    fsList.add(fs);

    RuleParser rp = new RuleParser(fsList, request.ruleString);

    rp.addAnnotation2(fs);

    List<LigerRule> appliedLigerRules = new ArrayList<>();
    for (Rule r : rp.getAppliedRules())
    {
        appliedLigerRules.add(new LigerRule(r.toString(),r.getRuleIndex(),r.getLineNumber()));
    }

    LigerWebGraph lg = new LigerWebGraph(fs.constraints,fs.annotation);

    GlueSemantics sem = new GlueSemantics();

    String extractedMCs = sem.extractMCsFromFs(fsProlog);
    String ligerMCs = sem.returnMeaningConstructors(fs);

    //Remove last line from extractedMCs
    extractedMCs = HelperMethods.unwrapMCs(extractedMCs);
    ligerMCs = HelperMethods.unwrapMCs(ligerMCs);

    String outputMCs = "{\n//grammar\n" + extractedMCs + "\n//liger\n" + ligerMCs + "\n}";


    return new LigerRuleAnnotation(lg,appliedLigerRules,outputMCs);

}

//Extract mcs from grammar. No call to LiGER.
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/parse_xle", produces = "application/json", consumes = "application/json")
    public LigerRuleAnnotation parseXLE(@RequestBody LigerRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEStarter starter = new XLEStarter();
        starter.generateXLEStarterFile();
        XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);


        String fsProlog = parser.parse2Prolog(request.sentence);

        LinkedHashMap<String,LinguisticStructure> fsRef = parser.fsString2Java(fsProlog,"S1");

        LinguisticStructure fs = fsRef.get(fsRef.keySet().iterator().next());

       // System.out.println(fs.getSubstructures("GLUE"));

        LOGGER.fine(fs.constraints.toString());

        LigerWebGraph lg = new LigerWebGraph(fs.constraints,fs.annotation);

        GlueSemantics sem = new GlueSemantics();

        HashMap<Set<ChoiceVar>, List<String>> test = sem.translateMeaningConstructors(fs);

        return new LigerRuleAnnotation(lg,null,sem.returnMultiStageMeaningConstructors(fs));
    }

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rules_xle", produces = "application/json", consumes = "application/json")
    public LigerRuleAnnotation applyRuleRequestXLE2(@RequestBody LigerRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEStarter starter = new XLEStarter();
        starter.generateXLEStarterFile();
        XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);

        LinguisticStructure fs = parser.parseSingle(request.sentence);
        LOGGER.fine(fs.constraints.toString());
        // System.out.println(fs.constraints);
        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, request.ruleString);
        rp.addAnnotation2(fs);

        GlueSemantics sem = new GlueSemantics();

        LigerWebGraph lg = new LigerWebGraph(fs.constraints,fs.annotation);

        List<LigerRule> appliedLigerRules = new ArrayList<>();
        for (Rule r : rp.getAppliedRules())
        {
            appliedLigerRules.add(new LigerRule(r.toString(),r.getRuleIndex(),r.getLineNumber()));
        }

        return new LigerRuleAnnotation(lg,appliedLigerRules,sem.returnMeaningConstructors(fs));
    }


    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rules_to_batch", produces = "application/json", consumes = "application/json")
    public LigerBatchParsingAnalysis applyRulesToTestsuite(@RequestBody LigerMultipleRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEStarter starter = new XLEStarter();
        starter.generateXLEStarterFile();
        XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);

        RuleParser rp = new RuleParser(request.ruleString);

        List<LigerGraphComponent> appliedRulesGraph = new ArrayList<>();



        for  (int i = 0; i < rp.getRules().size(); i++)
        {
            Rule r = rp.getRules().get(i);
            HashMap<String,Object> node = new HashMap<>();
            node.put("rule",r.toString());
            node.put("id", i);
            node.put("line", r.getLineNumber());
            node.put("node_type","rule");

            LigerGraphComponent lgc = new LigerGraphComponent(node);
            appliedRulesGraph.add(lgc);
        }


        HashMap<String,LigerRuleAnnotation> output = new HashMap<>();
        List<List<LigerRule>> allAppliedRules = new ArrayList<>();

        StringBuilder reportBuilder = new StringBuilder();



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


        for (int i = 0; i < keys.size(); i++) {

            String id = keys.get(i);
            String sentence = request.sentences.get(id);

            LinguisticStructure fs = parser.parseSingle(sentence);
            fs.local_id = id;
            LOGGER.fine(fs.constraints.toString());
            // System.out.println(fs.constraints);
            List<LinguisticStructure> fsList = new ArrayList<>();
            fsList.add(fs);

            rp.addAnnotation2(fs);

            GlueSemantics sem = new GlueSemantics();

            LigerWebGraph lg = new LigerWebGraph(fs.constraints, fs.annotation);

            List<LigerRule> appliedLigerRules = new ArrayList<>();
            for (Rule r : rp.getAppliedRules()) {
                appliedLigerRules.add(new LigerRule(r.toString(), r.getRuleIndex(), r.getLineNumber()));
            }

            List<String> meaningConstructors = List.of(sem.returnMeaningConstructors(fs).split("\n"));
            //remove lines which equal }\n or {\n
            meaningConstructors = meaningConstructors.stream().filter(s -> !s.equals("}") && !s.equals("{") && !s.startsWith("//")).collect(Collectors.toList());


            reportBuilder.append(String.format("%s\t\t%s\t\t%s\t\t%s", id, appliedLigerRules.size(), fs.annotation.size(),meaningConstructors.size()));
            reportBuilder.append(System.lineSeparator());

            output.put(id,new LigerRuleAnnotation(lg, appliedLigerRules, sem.returnMeaningConstructors(fs)));
            allAppliedRules.add(appliedLigerRules);
        }



       // HashMap<String, LigerGraphComponent> nodes = new HashMap<>();
        HashMap<String, LigerGraphComponent> edges = new HashMap<>();

        for (List<LigerRule> appliedRules : allAppliedRules)
        {
            for (int i = 0; i < appliedRules.size()-1; i = i + 1)
            {
                /*
                if (!nodes.containsKey(String.valueOf(appliedRules.get(i).index)))
                {
                    HashMap<String,Object> node = new HashMap<>();
                    node.put("rule",appliedRules.get(i).rule);
                    node.put("id", appliedRules.get(i).index);
                    node.put("line", appliedRules.get(i).lineNumber);
                    node.put("node_type","rule");

                    LigerGraphComponent lgc = new LigerGraphComponent(node);

                    nodes.put(String.valueOf(appliedRules.get(i).index),lgc);
                }

                if (!nodes.containsKey(String.valueOf(appliedRules.get(i+1).index)))
                {
                    HashMap<String,Object> node = new HashMap<>();
                    node.put("rule",appliedRules.get(i+1).rule);
                    node.put("id", appliedRules.get(i+1).index);
                    node.put("line", appliedRules.get(i+1).lineNumber);
                    node.put("node_type","rule");

                    LigerGraphComponent lgc = new LigerGraphComponent(node);

                    nodes.put(String.valueOf(appliedRules.get(i+1).index),lgc);
                }

                 */

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

                    LigerGraphComponent lgc = new LigerGraphComponent(edge);

                    edges.put(appliedRules.get(i).index + "+" +
                            appliedRules.get(i+1).index, lgc);
                } else
                {
                    String edgeID = appliedRules.get(i).index + "+" +
                            appliedRules.get(i+1).index;

                    Object timesUsed = edges.get(edgeID).data.get("timesUsed");

                    edges.get(edgeID).data.put("timesUsed", (Integer) timesUsed + 1);

                }




            }

           // appliedRulesGraph.addAll(nodes.values());
            appliedRulesGraph.addAll(edges.values());
        }




        return new LigerBatchParsingAnalysis(output,appliedRulesGraph,reportBuilder.toString());
        }

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


        for (int i = 0; i < keys.size(); i++) {

            String id = keys.get(i);
            String sentence = request.sentences.get(id);

            LinguisticStructure fs = parser.parseSingle(sentence);
            fs.local_id = id;
            LOGGER.fine(fs.constraints.toString());
            // System.out.println(fs.constraints);
            List<LinguisticStructure> fsList = new ArrayList<>();
            fsList.add(fs);



            GlueSemantics sem = new GlueSemantics();

            LigerWebGraph lg = new LigerWebGraph(fs.constraints, fs.annotation);

            Integer numberOfMcs = 0;

            String mcs = sem.returnMultiStageMeaningConstructors(fs);

            if (mcs != null) {
                List<String> meaningConstructors = List.of(mcs.split("\n"));
                //remove lines which equal }\n or {\n
                numberOfMcs = meaningConstructors.stream().filter(s -> !s.equals("}") && !s.equals("{") && !s.startsWith("//")).collect(Collectors.toList()).size();
            } else {
                mcs = "";
            }
            reportBuilder.append(String.format("%s\t\t%s", id, numberOfMcs));
            reportBuilder.append(System.lineSeparator());

            output.put(id,new LigerRuleAnnotation(null, null, mcs));

        }
        return new LigerBatchParsingAnalysis(output,null,reportBuilder.toString());
    }



/*
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/annotate_xle", produces = "application/json", consumes = "application/json")
    public LigerWebGraph annotateXLEoutput(@RequestBody LigerRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEoperator parser = new XLEoperator(new VariableHandler());
        try {
            LinguisticStructure fs = parser.parseSingle(request.sentence);

       // System.out.println(fs.constraints);
        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, request.ruleString,true);
        rp.addAnnotation2(fs);

        GlueSemantics sem = new GlueSemantics();
        String semantics = sem.returnMeaningConstructors(fs);


        return new LigerWebGraph(fs.constraints,fs.annotation,semantics);

        }catch(Exception e)
        {
            LOGGER.warning("Failed to load xle prolog file.");
        }
        return null;
    }
 */
}
