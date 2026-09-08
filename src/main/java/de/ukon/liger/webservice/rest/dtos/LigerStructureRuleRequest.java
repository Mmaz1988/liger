package de.ukon.liger.webservice.rest.dtos;

public class LigerStructureRuleRequest {
    public String content;
    public String format;
    public String id;
    public String ruleString;

    /** Whether to render each rule branch to a LigerWebGraph as well. Absent means yes,
     *  so every existing caller is unaffected. See LigerStructureMergeRequest.includeGraph
     *  -- this one costs more, because it is paid once per branch rather than once per
     *  request. */
    public Boolean includeGraph;

    public LigerStructureRuleRequest() {
    }

    public LigerStructureRuleRequest(String content, String format, String id, String ruleString) {
        this.content = content;
        this.format = format;
        this.id = id;
        this.ruleString = ruleString;
    }
}
