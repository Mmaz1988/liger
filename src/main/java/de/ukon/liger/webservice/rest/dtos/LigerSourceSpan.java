package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

public class LigerSourceSpan {
    public String text;
    public Integer start;
    public Integer end;
    /** The individual tokens behind {@code text}; {@code start}/{@code end} only give their
     *  outer bounds, so gaps between them are visible here only. */
    public List<LigerSourceToken> tokens = new ArrayList<>();

    public LigerSourceSpan() {}

    public LigerSourceSpan(String text, Integer start, Integer end) {
        this.text = text;
        this.start = start;
        this.end = end;
    }

    public LigerSourceSpan(String text, Integer start, Integer end, List<LigerSourceToken> tokens) {
        this(text, start, end);
        this.tokens = tokens;
    }
}
