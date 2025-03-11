package de.ukon.liger.semantics.linearLogicElements;

import java.util.Set;

public class McContainer {
    public String node;
    public Set<String> mcNodes;


    public McContainer(String node, Set<String> mcNodes)
    {
        this.node = node;
        this.mcNodes = mcNodes;
    }
}
