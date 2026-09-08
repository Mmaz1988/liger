package de.ukon.liger.semantics.linearLogicElements;

public class LLImplication extends LinearLogicElement {

    LinearLogicElement left;
    LinearLogicElement right;

    public LLImplication(LinearLogicElement left, LinearLogicElement right, String localName) {
        this.left = left;
        this.right = right;
        this.localName = localName;
    }

    @Override
    public String toString() {
        return "(" + this.left.toString() + " -o " + this.right.toString() + ")";
    }

    /*
    LLTEST(N A AT C CT) = @(LLCLOSURE %mc1 N)
       	 @(LLATOM %ANT A AT)
	 @(LLATOM %CONS C CT)
	 @(LLIMP %mc %ANT %CONS)
	 @(LLATOM %CONS1 C CT)
	 @(LLIMP %mc1 %mc %CONS1).
     */

    @Override
    public String toPrologString() {

        StringBuilder sb = new StringBuilder();
        sb.append(this.left.toPrologString());
        sb.append(System.lineSeparator());
        sb.append(this.right.toPrologString());
        sb.append(System.lineSeparator());
        sb.append("@(LLIMP " + this.localName + " " + this.left.localName + " " + this.right.localName + ")");




        return sb.toString();
    }
}
