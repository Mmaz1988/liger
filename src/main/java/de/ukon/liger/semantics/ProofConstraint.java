package de.ukon.liger.semantics;

import java.util.HashMap;
import java.util.Set;

public class ProofConstraint {

    public String node;

    public Set<String> elements;
    public HashMap<String,Set<String>> daughters;

    public ProofConstraint(String node,
                            Set<String> elements, HashMap<String,
                            Set<String>> daughters)
    {
        this.node = node;
        this.elements = elements;
        this.daughters = daughters;
    }
}
