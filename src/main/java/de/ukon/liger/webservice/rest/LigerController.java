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
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.webservice.rest.dtos.*;
import org.springframework.web.bind.annotation.*;
import de.ukon.liger.semantics.GlueSemantics;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.ud.UDoperator;
import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

@CrossOrigin
@RestController
public class LigerController {
    private final static Logger LOGGER = Logger.getLogger(LigerController.class.getName());

    private UDoperator parser = new UDoperator();

    public LigerController(){};


    @CrossOrigin
    //(origins = "http://localhost:63342")
    @RequestMapping(value = "/graph-test", produces = "application/json")
    public LigerWebGraph produceGraph(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String in) {

        LigerWebNode node1 = new LigerWebNode("1", "input");
        LigerWebNode node2 = new LigerWebNode("2", "annotation");
        LigerWebEdge edge1 = new LigerWebEdge("12", "1", "2", "projection", "proj");

        List<LigerGraphComponent> nodeList = new ArrayList<>();
        nodeList.add(node1);
        nodeList.add(node2);
        nodeList.add(edge1);


        return new LigerWebGraph(nodeList);
        //new Greeting(counter.incrementAndGet(),String.format(template,in));
    }

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

        RuleParser rp = new RuleParser(fsList, Paths.get(PathVariables.testPath + "testRulesUD4c.txt"));
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

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/semantics", produces = "application/json")
    public LigerWebGraph semanticsRequest2(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) throws IOException {

        UDoperator parser = new UDoperator();

        /*
        char[] c = input.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        input = new String(c);
         */

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

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rule", produces = "application/json", consumes = "application/json")
    public LigerWebGraph applyRuleRequest(@RequestBody LigerRequest request) {

    //    System.out.println(request.sentence);
     //   System.out.println(request.ruleString);
        UDoperator parser = new UDoperator();

        LinguisticStructure fs = parser.parseSingle(request.sentence);
        LOGGER.fine(fs.constraints.toString());
       // System.out.println(fs.constraints);
        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, request.ruleString,true);
        rp.addAnnotation2(fs);

        GlueSemantics sem = new GlueSemantics();
        String semantics = sem.calculateSemantics(fs);


    return new LigerWebGraph(fs.constraints,fs.annotation,semantics);

    }


    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/annotate_xle", produces = "application/json", consumes = "application/json")
    public LigerWebGraph annotateXLEoutput(@RequestBody LigerRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEoperator parser = new XLEoperator(new VariableHandler());
        try {
            LinguisticStructure fs = parser.xle2Java("some string");

       // System.out.println(fs.constraints);
        List<LinguisticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, request.ruleString,true);
        rp.addAnnotation2(fs);

        GlueSemantics sem = new GlueSemantics();
        String semantics = sem.calculateSemantics(fs);


        return new LigerWebGraph(fs.constraints,fs.annotation,semantics);

        }catch(Exception e)
        {
            LOGGER.warning("Failed to load xle prolog file.");
        }
        return null;
    }


    /**
     * This method returns a json object that stores a boolean in a map<String,String> if the syntactic analysis of a sentence
     * satisfies a LiGER query.
     * @param takes an AnnotationRequest as input (a map<String,String> with two keys: "sentence" and "ruleString"
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

}
