package webservice;

import analysis.RuleParser.RuleParser;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import syntax.SyntacticStructure;
import syntax.ud.UDoperator;
import syntax.xle.Prolog2Java.GraphConstraint;
import test.QueryParserTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@CrossOrigin
@RestController
public class AnnotatorController {
    @CrossOrigin
    //(origins = "http://localhost:63342")
    @PostMapping(value = "/graph-test", produces = "application/json")
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
    @PostMapping(value = "/annotate", produces = "application/json")
    public TestGraph annotationRequest2(
            @RequestParam(value = "in", defaultValue = "Didn't pass sentence") String input) {

        UDoperator parser = new UDoperator();

        SyntacticStructure fs = parser.parseSingle(input);
        System.out.println(fs.constraints);
        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, QueryParserTest.testFolderPath + "testRulesUD4.txt");
        rp.addAnnotation2(fs);

        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch (Exception e) {
            System.out.println("Sorting annotation failed.");
        }

        for (GraphConstraint g : fs.annotation) {
            System.out.println(g);
        }


        System.out.println("Done");


        return null;

        //return new TestGraph(nodeList);
        //new Greeting(counter.incrementAndGet(),String.format(template,in));
    }

}
