package de.ukon.liger.analysis.graphParser.LigerGraph;

import ch.qos.logback.core.net.ObjectWriter;
import de.ukon.liger.packing.ChoiceVar;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LigerNode extends LigerObject {
    private final Map<String, Object> map = new LinkedHashMap<>();

    public Set<ChoiceVar> choices;

    public LigerNode(Set<ChoiceVar> choices)
    {
        this.choices = choices;
    };

    public void put(String key , Object value) { map.put(key, value); }
    public Object get(String value) { return map.get(value); }
}
