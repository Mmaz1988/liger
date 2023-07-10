package de.ukon.liger.webservice.rest.dtos;

public class LigerRule {
    public String rule;
    public int index;
    public int lineNumber;

    public LigerRule(String rule, int index, int lineNumber) {
        this.rule = rule;
        this.index = index;
        this.lineNumber = lineNumber;
    }
}
