package webservice;

import java.util.List;

public class TestGraph {

    public final List<TestNode> graphElements;
    public String semantics;

    public TestGraph(List<TestNode> graphElements)
    {
        this.graphElements = graphElements;
    }

    public TestGraph(List<TestNode> graphElements, String semantics)
    {this.graphElements = graphElements;
    this.semantics = semantics;}
}
