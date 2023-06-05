package de.ukon.liger.analysis.graphParser.LigerGraph;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.utilities.HelperMethods;
import org.ejml.dense.row.decomposition.svd.implicitqr.SvdImplicitQrAlgorithm_DDRM;

import java.util.*;
import java.util.stream.Collectors;

public class LigerGraph extends LigerObject {

    public List<LigerNode> nodes;
    public List<LigerEdge> edges;
    public ChoiceSpace cp;

    public Set<String> attributes;
    public Set<String> relations;

    public Set<String> values;

    public String id;

    public LigerGraph()
    {
    }

    public LigerGraph(List nodes, List edges, ChoiceSpace cp) {
        this.nodes = nodes;
        this.edges = edges;
        this.cp = cp;
    }

    public LigerGraph(List nodes, List edges, ChoiceSpace cp, Set attributes, Set relations, Set values) {
        this.nodes = nodes;
        this.edges = edges;
        this.attributes = attributes;
        this.relations = relations;
        this.values = values;
        this.cp = cp;
    }


    public static LigerGraph LinguisticStructure2Graph(LinguisticStructure ls)
    {
        List<LigerNode> ligerNodes = new ArrayList<>();
        List<LigerEdge> ligerEdges = new ArrayList<>();

        Set<String> attributes = new HashSet<>();
        Set<String> relations = new HashSet<>();
        Set<String> values = new HashSet<>();

       // Set<String> nodes = new HashSet<>();

        List<GraphConstraint> allConstraints = new ArrayList<>();
        allConstraints.addAll(ls.constraints);
        allConstraints.addAll(ls.annotation);


        /*
        Map<Object,Object> nodes = allConstraints.stream().collect(Collectors.toMap(GraphConstraint::getFsNode,GraphConstraint::getReading));
        nodes.putAll(allConstraints.stream().
                filter(x -> HelperMethods.isInteger(x)).
                collect(Collectors.toMap(GraphConstraint::getFsValue,GraphConstraint::getReading)));

         */
        Map<String,Set<Set<ChoiceVar>>> nodes = new HashMap<>();
        for (GraphConstraint g : allConstraints)
        {
            if (!nodes.containsKey(g.getFsNode()))
            {
                nodes.put(g.getFsNode(),new HashSet<>());
                nodes.get(g.getFsNode()).add(g.getReading());
            } else
            {
                nodes.get(g.getFsNode()).add(g.getReading());
            }

            if (HelperMethods.isInteger(g.getFsValue()))
            {
                if (!nodes.containsKey(g.getFsValue().toString()))
                {
                    nodes.put(g.getFsValue().toString(),new HashSet<>());
                    nodes.get(g.getFsValue().toString());
                }else
                {
                    nodes.get(g.getFsNode()).add(g.getReading());
                }
            }
        }


        for (String node : nodes.keySet())
        {
            //if node appears in multiple choices then use the highest choice
            //pairwise comparison between all available choices
            if (nodes.get(node).size() > 1)
            {
                /* TODO
                for choice a,b
                    if (dominate(a,b))
                        remove(b)
                   else if (dominate(b,a))
                        remove(a)
                        else{
                        remove(a)
                        b.add(a)
                        }
                 */

            }

            //Only works if there is no packing
            Set<ChoiceVar> chv = nodes.get(node).stream().findAny().get();

            LigerNode lnode = new LigerNode(chv);
            lnode.put("id",node);

            List<GraphConstraint> avps = allConstraints.stream().
                    filter(x -> x.getFsNode().equals(node) && !HelperMethods.isInteger(x.getFsValue())).
                    collect(Collectors.toList());

            allConstraints.removeAll(avps);

            List<LigerAVP> ligerAVPs = new ArrayList<>();

            for (GraphConstraint avp : avps)
            {
                ligerAVPs.add(new LigerAVP(avp.getRelationLabel(),avp.getFsValue().toString(),avp.getReading()));
                attributes.add(avp.getRelationLabel());
                values.add(avp.getFsValue().toString());
            }

            lnode.put("avps",ligerAVPs);

            ligerNodes.add(lnode);
        }

        for (GraphConstraint g : allConstraints)
        {
            LigerEdge ledge = new LigerEdge(g.getReading());

            ledge.put("source",g.getFsNode());
            ledge.put("target",g.getFsValue().toString());
            ledge.put("label", g.getRelationLabel());

            relations.add(g.getRelationLabel());

            ligerEdges.add(ledge);
        }


        /*
        List<LinkedHashMap> daughters = edges.stream().filter(x ->
                x.get("sourceVertexid").equals(top.get("id"))).collect(Collectors.toList());

         */
        return new LigerGraph(ligerNodes,ligerEdges,ls.cp,attributes,relations,values);
    }



    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("liger_graph, nodes_size: ");
        sb.append(this.nodes.size() + ", ");
        sb.append("edges_size: ");
        sb.append(this.edges.size() + ", ");



    return sb.toString();
    }


}


