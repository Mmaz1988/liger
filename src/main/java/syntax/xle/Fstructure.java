package syntax.xle;

import syntax.SyntacticStructure;
import syntax.xle.Prolog2Java.GraphConstraint;

import java.util.List;

public class Fstructure extends SyntacticStructure {

    public boolean packed;


    //TODO
    //public Integer global_id

    public Fstructure(String local_id, String sentence, List<GraphConstraint> fsFacts, boolean packed)
    {
        super(local_id,sentence,fsFacts);
        this.packed = packed;

    }
}
