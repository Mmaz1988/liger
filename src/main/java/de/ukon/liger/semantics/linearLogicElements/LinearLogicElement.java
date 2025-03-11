package de.ukon.liger.semantics.linearLogicElements;

public abstract class LinearLogicElement {

    public String localName;
    public LinearLogicElement() {
    }

    public abstract String toString();
    public abstract String toPrologString();
}
