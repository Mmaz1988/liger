package syntax.ud;

import syntax.SyntacticStructure;

public abstract class SyntaxOperator {

    public SyntaxOperator()
    {}

    public abstract SyntacticStructure parseSingle(String sentence);
}
