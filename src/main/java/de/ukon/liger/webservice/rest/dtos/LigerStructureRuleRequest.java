package de.ukon.liger.webservice.rest.dtos;

public class LigerStructureRuleRequest {
    public String content;
    public String format;
    public String id;
    public String ruleString;

    public LigerStructureRuleRequest() {
    }

    public LigerStructureRuleRequest(String content, String format, String id, String ruleString) {
        this.content = content;
        this.format = format;
        this.id = id;
        this.ruleString = ruleString;
    }
}
