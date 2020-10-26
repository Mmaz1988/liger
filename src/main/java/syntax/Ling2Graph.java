package syntax;

import java.util.Set;

public class Ling2Graph {


    SyntacticStructure input;
    //Output: Json string (graph representation)
    String json;
    Set<Integer> graphNodes;


    public Ling2Graph(SyntacticStructure input)
    {
        this.input = input;

    }
}
