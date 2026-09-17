package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

public class LigerProvenanceCondition {
    /** DRS graph node id; the occurrence key for this condition. */
    public String nodeId;
    public String symbol;
    /** DRS graph node ids of the arguments, in TERM order. */
    public List<String> args = new ArrayList<>();
    public Integer sourceIndex;
    public List<String> tokenIds = new ArrayList<>();
    /** 0-based {@code [start, end)} intervals; tokens separated only by whitespace are joined. */
    public List<int[]> spans = new ArrayList<>();
    public String text;

    public LigerProvenanceCondition() {}
}
