package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

public class LigerSourceSpanResponse {
    public List<LigerSourceSpan> spans = new ArrayList<>();

    public LigerSourceSpanResponse() {}

    public LigerSourceSpanResponse(List<LigerSourceSpan> spans) {
        this.spans = spans;
    }
}
