package de.ukon.liger.webservice.rest.dtos;

public class LigerStructureQueryRequest {
    public String content;
    public String format;
    public String id;
    public String query;

    public LigerStructureQueryRequest(String content, String format, String id, String query) {
        this.content = content;
        this.format = format;
        this.id = id;
        this.query = query;
    }
}
