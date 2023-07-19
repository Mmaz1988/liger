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

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.RuleParser.Rule;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.annotators.SegmenterMain;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.ud.UDoperator;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import de.ukon.liger.utilities.XLEStarter;
import de.ukon.liger.webservice.rest.dtos.*;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

@CrossOrigin
@RestController
public class LigerController {
    private final static Logger LOGGER = Logger.getLogger(LigerController.class.getName());

    private UDoperator parser = new UDoperator();

    @Autowired
    private LigerService ligerService;
    private StanfordCoreNLP pipeline;

    public LigerController(){

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,sentiment,udfeats");
        pipeline = new StanfordCoreNLP(props);

    };

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/parse", produces = "application/json")
    public LigerWebGraph parseRequest(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) {

        UDoperator parser = new UDoperator();

        LinguisticStructure fs = parser.parseSingle(input);
       // System.out.println(fs.constraints);
        LOGGER.fine(fs.constraints.toString());

        return new LigerWebGraph(fs);

    }

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/annotate", produces = "application/json")
    public LigerWebGraph annotationRequest2(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) throws IOException {

        UDoperator parser = new UDoperator();

        LinguisticStructure fs = parser.parseSingle(input);
        LOGGER.fine(fs.constraints.toString());
        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, Paths.get(PathVariables.testPath + "testRulesUD4b.txt"));
        rp.addAnnotation2(fs);

        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch (Exception e) {
            LOGGER.warning("Sorting annotation failed.");
        }

        StringBuilder resultBuilder = new StringBuilder();
        for (GraphConstraint g : fs.annotation) {
              resultBuilder.append(g.toString());
        }

        LOGGER.info("Annotation output:\n" + resultBuilder.toString());




       LOGGER.info("Done");

        return new LigerWebGraph(fs.constraints,fs.annotation);

        //return new TestGraph(nodeList);
        //new Greeting(counter.incrementAndGet(),String.format(template,in));
    }

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

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rules", produces = "application/json", consumes = "application/json")
    public LigerRuleAnnotation applyRuleRequest(@RequestBody LigerRequest request) {

    //    System.out.println(request.sentence);
     //   System.out.println(request.ruleString);
        UDoperator parser = new UDoperator();

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

        List<LigerRule> appliedLigerRules = new ArrayList<>();
        for (Rule r : rp.getAppliedRules())
        {
            appliedLigerRules.add(new LigerRule(r.toString(),r.getRuleIndex(),r.getLineNumber()));
        }


        return new LigerRuleAnnotation(lg,appliedLigerRules,sem.returnMeaningConstructors(fs));
    }

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

    //TODO for test purposes only
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rules_xle", produces = "application/json", consumes = "application/json")
    public LigerRuleAnnotation applyRuleRequestXLE2(@RequestBody LigerRequest request) {

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
    public LigerBatchParsingAnalysis applyRulesToTestsuite(@RequestBody LigerMultipleRequest request) {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEStarter starter = new XLEStarter();
        starter.generateXLEStarterFile();
        XLEoperator parser = new XLEoperator(new VariableHandler(), starter.operatingSystem);

        RuleParser rp = new RuleParser(request.ruleString);

        HashMap<Integer,LigerRuleAnnotation> output = new HashMap<>();
        List<List<LigerRule>> allAppliedRules = new ArrayList<>();

        for (int i = 0; i < request.sentences.size(); i++) {

            String sentence = request.sentences.get(i);

            LinguisticStructure fs = parser.parseSingle(sentence);
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


            output.put(i,new LigerRuleAnnotation(lg, appliedLigerRules, sem.returnMeaningConstructors(fs)));
            allAppliedRules.add(appliedLigerRules);
        }

        List<LigerGraphComponent> appliedRulesGraph = new ArrayList<>();

        HashMap<String, LigerGraphComponent> nodes = new HashMap<>();
        HashMap<String, LigerGraphComponent> edges = new HashMap<>();

        for (List<LigerRule> appliedRules : allAppliedRules)
        {
            for (int i = 0; i < appliedRules.size()-1; i = i + 1)
            {
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

                if (!edges.containsKey(appliedRules.get(i).index + "+" +
                     appliedRules.get(i+1).index))
                {
                    HashMap<String,Object> edge = new HashMap<>();
                    edge.put("source",appliedRules.get(i).index);

                    if (edge.get("source").equals(70))
                    {
                        System.out.println("Stop");
                    }

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

            appliedRulesGraph.addAll(nodes.values());
            appliedRulesGraph.addAll(edges.values());
        }




        return new LigerBatchParsingAnalysis(output,appliedRulesGraph);
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

    /**
     * This method returns a json object that stores a boolean in a map<String,String> if the syntactic analysis of a sentence
     * satisfies a LiGER query.
     * takes an AnnotationRequest as input (a map<String,String> with two keys: "sentence" and "ruleString"
     *              "ruleString" here corresponds to the query!
     * @return a singleton map (key: "success") indicating whether a search was successful or failed
     * @throws IOException
     */

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/query", produces = "application/json", consumes = "application/json")
    public Map<String,String> checkQuery(@RequestBody LigerRequest request) throws IOException {

        LinguisticStructure fs = parser.parseSingle(request.sentence);
        LOGGER.fine(fs.constraints.toString());
        //  System.out.println(fs.constraints);

        QueryParser qp = new QueryParser(fs);
        qp.generateQuery(request.ruleString);

        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        Map<String,String> success = new HashMap();
        success.put("success",qpr.isSuccess.toString());

        return success;
    }

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/query_args", produces = "application/json", consumes = "application/json")
    public Set<String> checkArgsForQuery(@RequestBody LigerArgumentListQuery request) throws IOException {

        Set<String> matchingArgs = new HashSet<>();

        for (String id : request.mpg_arguments.keySet()) {

            LigerArgument  arg = request.mpg_arguments.get(id);

            CoreDocument doc = new CoreDocument(arg.premise);


            this.pipeline.annotate(doc);

            for (CoreSentence sent : doc.sentences()) {

                LinguisticStructure fs = parser.parseSingle(sent.text());
                //LOGGER.fine(fs.constraints.toString());
                //  System.out.println(fs.constraints);

                QueryParser qp = new QueryParser(fs);
                qp.generateQuery(request.query);

                QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

                if (qpr.isSuccess) {
                    matchingArgs.add(id);
                }
                break;
            }
        }
        return matchingArgs;
    }



    /**
     * Segments and annotates an incoming argument using Stanford CoreNLP.
     * @param request
     * @return
     * @throws IOException
     */


    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/annotate_argument", produces = "application/json", consumes = "application/json")
    public Map<String,Object> annotateArgument(@RequestBody LigerArgument request) throws IOException {

        /*
        GkrDTO gkrDTOpremise = new GkrDTO(request.premise,"");
        GkrDTO gkrDTOconclusion = new GkrDTO(request.conclusion,"");

        LinkedHashMap premiseGKR = ligerService.accessGKR(gkrDTOpremise);
        LinkedHashMap conclusionGKR = ligerService.accessGKR(gkrDTOconclusion);

        List<LinkedHashMap> argumentGKRs = new ArrayList<>();

        argumentGKRs.add(0,premiseGKR);
        argumentGKRs.add(1,conclusionGKR);
*/

        Map<String,Object> output = SegmenterMain.coreAnnotationArgument(request, this.pipeline, parser);
        return output;
    }



    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/annotate_gkr", produces = "application/json", consumes = "application/json")
    public Map<String,Object> annotateArgument(@RequestBody LigerRequest request) throws IOException {


        GkrDTO gkrData = new GkrDTO(request.sentence, request.ruleString);

      LinkedHashMap o = ligerService.accessGKR(gkrData);

        return o;
    }

}
