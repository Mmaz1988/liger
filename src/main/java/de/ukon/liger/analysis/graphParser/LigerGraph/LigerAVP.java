package de.ukon.liger.analysis.graphParser.LigerGraph;

import de.ukon.liger.packing.ChoiceVar;

import java.util.Set;

public class LigerAVP extends LigerObject {
    public String attribute;
    public String value;
    public Set<ChoiceVar> choices;

    public LigerAVP(String attribute, String value, Set<ChoiceVar> choices)
    {
        this.attribute = attribute;
        this.value = value;
        this.choices = choices;
    }
}
