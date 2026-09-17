package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

public class LigerProvenanceReferent {
    /** DRS graph node id. */
    public String nodeId;
    /** Display name, e.g. {@code x2}. */
    public String label;
    public Integer sourceIndex;
    /** The f-structure bounding this referent's description; null if it could not be determined. */
    public String fsNode;
    /** Conditions taking this referent as an argument, wherever they come from in the sentence. */
    public List<String> directConditionIds = new ArrayList<>();
    /** Conditions reachable from the referent whose tokens lie inside {@link #fsNode}. */
    public List<String> descriptionConditionIds = new ArrayList<>();
    public List<Integer> descriptionIndices = new ArrayList<>();
    /** Tokens of the description's conditions; words contributing no condition (e.g. a relative
     *  pronoun) leave gaps. */
    public List<String> tokenIds = new ArrayList<>();
    /** 0-based {@code [start, end)} intervals; tokens separated only by whitespace are joined. */
    public List<int[]> spans = new ArrayList<>();
    public String text;
    /** Every token projecting into {@link #fsNode}, whether or not it contributes a condition:
     *  the whole phrase, e.g. "the dog that barked" where the description is "the dog barked". */
    public List<String> phraseTokenIds = new ArrayList<>();
    public List<int[]> phraseSpans = new ArrayList<>();
    public String phraseText;

    public LigerProvenanceReferent() {}
}
