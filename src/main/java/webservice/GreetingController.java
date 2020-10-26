package webservice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import analysis.RuleParser.RuleParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.tools.javac.Main;
import glueSemantics.linearLogic.Premise;
import org.apache.tomcat.util.json.JSONParser;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import semantics.GlueSemantics;
import syntax.SyntacticStructure;
import syntax.ud.UDoperator;
import syntax.xle.Prolog2Java.GraphConstraint;
import test.QueryParserTest;
import utilities.DBASettings;

@CrossOrigin
@RestController
public class GreetingController {

	private static final String template = "This is the new sentence: %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@CrossOrigin
	//(origins = "http://localhost:63342")
	@PostMapping(value = "/annotate", produces = "application/json")
	public TestGraph annotationRequest(
			@RequestParam(value = "in",defaultValue = "Didn't pass sentence") String in)
	{

		TestNode node1 = new TestNode("1","input");
		TestNode node2 = new TestNode("2","annotation");
		TestNode edge1 = new TestNode("12","1","2","projection","proj");

		List<TestNode> nodeList = new ArrayList<>();
		nodeList.add(node1);
		nodeList.add(node2);
		nodeList.add(edge1);



		return new TestGraph(nodeList);
		//new Greeting(counter.incrementAndGet(),String.format(template,in));
	}

	@CrossOrigin
	//(origins = "http://localhost:63342")
	@PostMapping(value = "/annotate2", produces = "application/json")
	public TestGraph annotationRequest2(
			@RequestParam(value = "in",defaultValue = "Didn't pass sentence") String input)
	{

		/*
		TestNode node1 = new TestNode("1","input");
		TestNode node2 = new TestNode("2","annotation");
		TestNode edge1 = new TestNode("12","1","2","projection","proj");

		List<TestNode> nodeList = new ArrayList<>();
		nodeList.add(node1);
		nodeList.add(node2);
		nodeList.add(edge1);
*/
		 UDoperator parser = new UDoperator();

		SyntacticStructure fs = parser.parseSingle(input);
		System.out.println(fs.constraints);
		List<SyntacticStructure> fsList = new ArrayList<>();
		fsList.add(fs);

		RuleParser rp = new RuleParser(fsList, QueryParserTest.testFolderPath + "testRulesUD4.txt");
		rp.addAnnotation2(fs);


		GlueSemantics sem = new GlueSemantics();
		sem.calculateSemantics(fs);


		for (Premise p : sem.llprover.getSolutions()) {
			System.out.println(p.toString());
		}



		try {
			fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
		} catch(Exception e)
		{
			System.out.println("Sorting annotation failed.");
		}

		for (GraphConstraint g : fs.annotation) {
			System.out.println(g);
		}


                /*
                List<List<GraphConstraint>> substructure = fs.getSubstructures("FEATURES");

                for (List<GraphConstraint> sstr : substructure)
                {

                    //System.out.println(sstr);

                    for (GraphConstraint g : sstr)
                    {
                        if (g.getRelationLabel().equals("MODAL"))
                        {
                            System.out.println(fs.sentence + " " + g.getFsValue().toString());
                        }
                    }

                }
                */


		System.out.println("Done");


		return null;

		//return new TestGraph(nodeList);
		//new Greeting(counter.incrementAndGet(),String.format(template,in));
	}

}
