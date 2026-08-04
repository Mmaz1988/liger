package de.ukon.liger.webservice.rest.dtos;

public class LigerSourceSpan {
    public String text;
    public Integer start;
    public Integer end;

    public LigerSourceSpan() {}

    public LigerSourceSpan(String text, Integer start, Integer end) {
        this.text = text;
        this.start = start;
        this.end = end;
    }
}
