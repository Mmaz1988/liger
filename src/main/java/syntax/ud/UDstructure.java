package syntax.ud;


import syntax.SyntacticStructure;
import syntax.xle.Prolog2Java.GraphConstraint;

import java.util.List;

public class UDstructure extends SyntacticStructure {

    public UDstructure(String id, String sentence, List<GraphConstraint> syntax)
    {
        super(id,sentence,syntax);
    }
}
