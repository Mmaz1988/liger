package de.ukon.liger.analysis.graphParser.LigerGraph;

import de.ukon.liger.packing.ChoiceVar;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LigerEdge extends LigerObject {

        private final Map<String, String> map = new LinkedHashMap();
        private Set<ChoiceVar> choices;

        public LigerEdge(Set<ChoiceVar> choices)
        {
            this.choices = choices;
        };

        public void put(String key , String value) { map.put(key, value); }
        public String get(String value) { return map.get(value); }
    }


