package de.ukon.liger.semantics.linearLogicElements;


public class SubExpression {
    public Integer start;
    public Integer end;

    public SubExpression left;
    public SubExpression right;

    public SubExpression(Integer start, Integer end, SubExpression left, SubExpression right) {
        this.start = start;
        this.left = left;
        this.end = end;
        this.right = right;
    }
}
