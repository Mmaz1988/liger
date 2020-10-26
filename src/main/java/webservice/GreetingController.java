package webservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.tomcat.util.json.JSONParser;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import syntax.xle.Prolog2Java.GraphConstraint;

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

}
