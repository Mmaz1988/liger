package de.ukon.liger.semantics.linearLogicElements;

public class LLQuantFormula extends LinearLogicElement {
    String quantifier;
    LLResource binder;
    LinearLogicElement scope;

    public LLQuantFormula(String quantifier, LLResource binder, LinearLogicElement scope) {
        this.quantifier = quantifier;
        this.binder = binder;
        this.scope = scope;
        this.localName = this.scope.localName;
    }

    @Override
    public String toString() {
        return this.quantifier + this.binder.toString() + "." + this.scope.toString();
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
        sb.append(this.binder.toPrologString());
        sb.append(System.lineSeparator());
        sb.append(this.scope.toPrologString());
        sb.append(System.lineSeparator());
        sb.append("@(LLQUANT " + this.scope.localName + " " + this.binder.localName + ")" );

        return sb.toString();
    }
}
