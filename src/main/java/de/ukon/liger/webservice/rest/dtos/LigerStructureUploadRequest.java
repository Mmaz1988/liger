package de.ukon.liger.webservice.rest.dtos;

public class LigerStructureUploadRequest {
    public String content;
    public String format;
    public String id;

    public LigerStructureUploadRequest(String content, String format, String id) {
        this.content = content;
        this.format = format;
        this.id = id;
    }
}
