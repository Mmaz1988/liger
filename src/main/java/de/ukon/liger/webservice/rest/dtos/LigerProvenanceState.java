package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

/** A state (box) of the DRS graph: its name, the edge that enters it from its parent state, and
 *  the tokens of the meaning constructor it comes from -- for a box a nesting operator opens,
 *  the operator ("every", "no"); for a p-box, its trigger. */
public class LigerProvenanceState {
    /** DRS graph node id. */
    public String nodeId;
    /** The state's name, as P: writes it ({@code s3}). */
    public String label;
    /** The edge entering it (SUB, IMP, NOT, OR, SCOPE, PRSP, MERGE, UPDATE); null for the root. */
    public String enteredBy;
    /** The name of the node that edge comes from; null for the root. */
    public String parent;
    public Integer sourceIndex;
    public List<String> tokenIds = new ArrayList<>();
    /** 0-based {@code [start, end)} intervals; tokens separated only by whitespace are joined. */
    public List<int[]> spans = new ArrayList<>();
    public String text;

    public LigerProvenanceState() {}
}
