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

package webservice;

import analysis.RuleParser.RuleParser;
import main.DbaMain;
import org.springframework.web.bind.annotation.*;
import semantics.GlueSemantics;
import syntax.GraphConstraint;
import syntax.SyntacticStructure;
import syntax.ud.UDoperator;
import syntax.xle.XLEoperator;
import utilities.PathVariables;
import utilities.VariableHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

@CrossOrigin
@RestController
public class AnnotatorController {
    private final static Logger LOGGER = Logger.getLogger(DbaMain.class.getName());

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @RequestMapping(value = "/graph-test", produces = "application/json")
    public TestGraph produceGraph(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String in) {

        TestNode node1 = new TestNode("1", "input");
        TestNode node2 = new TestNode("2", "annotation");
        TestNode edge1 = new TestNode("12", "1", "2", "projection", "proj");

        List<TestNode> nodeList = new ArrayList<>();
        nodeList.add(node1);
        nodeList.add(node2);
        nodeList.add(edge1);


        return new TestGraph(nodeList);
        //new Greeting(counter.incrementAndGet(),String.format(template,in));
    }

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/parse", produces = "application/json")
    public TestGraph parseRequest(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) {

        UDoperator parser = new UDoperator();

        SyntacticStructure fs = parser.parseSingle(input);
       // System.out.println(fs.constraints);
        LOGGER.fine(fs.constraints.toString());

        return new TestGraph(fs);

    }

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/annotate", produces = "application/json")
    public TestGraph annotationRequest2(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) {

        UDoperator parser = new UDoperator();

        SyntacticStructure fs = parser.parseSingle(input);
        LOGGER.fine(fs.constraints.toString());
        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, new File(PathVariables.testPath + "testRulesUD4c.txt"));
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


        return new TestGraph(fs.constraints,fs.annotation);

        //return new TestGraph(nodeList);
        //new Greeting(counter.incrementAndGet(),String.format(template,in));
    }

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/semantics", produces = "application/json")
    public TestGraph semanticsRequest2(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) {

        UDoperator parser = new UDoperator();

        /*
        char[] c = input.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        input = new String(c);
         */

        SyntacticStructure fs = parser.parseSingle(input.toLowerCase());
      //  System.out.println(fs.constraints);
        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, new File(PathVariables.testPath + "testRulesUD1.txt"));
        rp.addAnnotation2(fs);

        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch (Exception e) {
          LOGGER.warning("Sorting annotation failed.");
        }

        GlueSemantics sem = new GlueSemantics();
        String semantics = sem.calculateSemantics(fs);


      //  return new TestGraph(nodeList,semantics);



        return new TestGraph(fs.constraints,fs.annotation,semantics);
        //new Greeting(counter.incrementAndGet(),String.format(template,in));
    }

    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/apply_rule", produces = "application/json", consumes = "application/json")
    public TestGraph applyRuleRequest(@RequestBody AnnotationRequest request) {

    //    System.out.println(request.sentence);
     //   System.out.println(request.ruleString);
        UDoperator parser = new UDoperator();

        SyntacticStructure fs = parser.parseSingle(request.sentence);
       // System.out.println(fs.constraints);
        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, request.ruleString,true);
        rp.addAnnotation2(fs);

        GlueSemantics sem = new GlueSemantics();
        String semantics = sem.calculateSemantics(fs);


    return new TestGraph(fs.constraints,fs.annotation,semantics);

    }


    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/annotate_xle", produces = "application/json", consumes = "application/json")
    public TestGraph annotateXLEoutput(@RequestBody AnnotationRequest request) throws IOException {

        //    System.out.println(request.sentence);
        //   System.out.println(request.ruleString);
        XLEoperator parser = new XLEoperator(new VariableHandler());
        try {
            SyntacticStructure fs = parser.xle2Java("some string");

       // System.out.println(fs.constraints);
        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, request.ruleString,true);
        rp.addAnnotation2(fs);

        GlueSemantics sem = new GlueSemantics();
        String semantics = sem.calculateSemantics(fs);


        return new TestGraph(fs.constraints,fs.annotation,semantics);

        }catch(Exception e)
        {
            LOGGER.warning("Failed to load xle prolog file.");
        }
        return null;
    }


}
