package de.ukon.liger.semantics;

import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.QueryParser.SolutionKey;
import de.ukon.liger.semantics.linearLogicElements.McContainer;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.xle.Fstructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CStructureTraverser {


    public String rootId;

    public Fstructure fs;
    public Set<String> visitedProofNodes = new HashSet<>();
    public Set<String> visitedGlueNodes = new HashSet<>();

    public HashMap<String, Set<String>> associatedMCs = new HashMap<>();
    public HashMap<String, Set<String>> associatedMCs2 = new HashMap<>();

    public HashMap<String,Set<String>> glueTree = new HashMap<>();
    public HashMap<String, HashSet<String>> glueTree2;
    // Instance initializer block to initialize the HashMap
    {
        glueTree2 = new HashMap<>();
        glueTree2.put("root", new HashSet<>());
    }
    private static Pattern daughterPattern = Pattern.compile("DAUGHTER(\\d+)");
    private final static Logger LOGGER = LoggerFactory.getLogger(CStructureTraverser.class);

    //for debugging, a complete list of mappings from prooftree nodes to c-structure nodes
    public LinkedHashMap<String,List<String>> prooftreeToCStructure = new LinkedHashMap<>();

    public CStructureTraverser(String rootId, Fstructure fs) {
        this.rootId = rootId;
        this.fs = fs;
    }

    /**
     * STEPS for traversing C-Structure:
     * Find out if Cproj node is associated with a proof tree description
     * Link proof tree descriptions
     * Find out if Cproj is node is directly mentioned in a proof tree node (either as element, as element of a daugther node)
     * Find out if Cproj node is associated with a glue meaning constructor
     *
     *
     */
    private LinkedList<ProofConstraint> proofTree = new LinkedList<>();
    private HashMap<String,Set<String>> equalities = new HashMap<>();

    public void traverseCstructure2(Object cstructure, String anchor){

        String currentRoot = (String) ((LinkedHashMap) cstructure).keySet().stream().findAny().get();
        String currentCproj = getCProj(currentRoot);
        boolean subOrdinateAnchor = false;

        if (anchor == null)
        {
            anchor = "root";
            equalities.put(anchor, new HashSet<>());
        }

        if (currentRoot != null) {
            //Check if currentRoot is associated with a proof consstraint
            ProofConstraint potentialPC = null;
            potentialPC = buildProofTree(fs, currentRoot);

            if (potentialPC != null) {
                proofTree.addFirst(potentialPC);
                LOGGER.info("Current proof constraint: " + potentialPC.node + " " + potentialPC.elements + " " + potentialPC.daughters);

                //Root initial proof constraint to make sure all proof constraints are ultimately linked to one tree
                if (anchor.equals("root") && proofTree.size() == 1)
                {
                    equalities.get("root").add(potentialPC.node);
                }
            }


            String previousAnchor = anchor;

            //Check if the current c-projection is covered by any proof constraint
            for (ProofConstraint pc : proofTree) {
                if (!pc.elements.isEmpty() && currentCproj != null) {
                    if (pc.elements.contains(currentCproj)) {


                        boolean subordinateAnchor = false;
                            if (pc.daughters.keySet().contains(previousAnchor))
                            {
                                subordinateAnchor = true;
                            }

                        if (!subordinateAnchor) {
                            anchor = previousAnchor;
                            if (!equalities.containsKey(previousAnchor))
                            {
                                equalities.put(previousAnchor,new HashSet<>());
                                equalities.get(previousAnchor).add(anchor);
                            }} else {

                            // String newAnchor = previousAnchor;
                            for (String key : equalities.keySet())
                            {
                                if (equalities.get(key).contains(pc.node))
                                {
                                    anchor = key;
                                    break;
                                }
                            }
                        }

                        if (!glueTree2.containsKey(anchor)) {
                            glueTree2.put(anchor, new HashSet<>());
                        }
                        break;
                    } else {
                        boolean isDaughter = false;
                        for (String daughter : pc.daughters.keySet()) {
                            if (pc.daughters.get(daughter).contains(currentCproj)) {
                                anchor = daughter;
                                isDaughter = true;

                                //add relation to previous Anchor
                                if (!glueTree2.containsKey(previousAnchor)) {
                                    glueTree2.put(previousAnchor, new HashSet<>());
                                }
                                glueTree2.get(previousAnchor).add(daughter);
                                break;
                            }
                        }
                        if (isDaughter) {
                            break;
                        }
                    }
                }
            }
            //If no new anchor is found, the previous anchor remains

            //Check if c-structure node is associated with any mcs
            McContainer mcs = findMCNodes(currentCproj, fs);
            if (mcs != null) {
                if (!associatedMCs2.containsKey(anchor)) {
                    associatedMCs2.put(anchor, new HashSet<>());
                }
                associatedMCs2.get(anchor).addAll(mcs.mcNodes);
            }


            //recursively traverse down the tree
            if (((LinkedHashMap) cstructure).get(currentRoot) != null) {
                //if available, recurse down right node first
                if (((LinkedHashMap) cstructure).get(currentRoot) instanceof Object[]) {
                    Object[] keySet = (Object[]) ((LinkedHashMap) cstructure).get(currentRoot);
                    //right nodes first
                    if (keySet[1] != null) {
                        // Object key = ((LinkedHashMap) keySet[1]).keySet().stream().findAny().get();
                        traverseCstructure2(keySet[1], anchor);
                    }
                    //  Object key2 = ((LinkedHashMap) keySet[0]).keySet().stream().findAny().get();
                    traverseCstructure2(keySet[0], anchor);
                }
            }
        }
    }




    public void traverseCStructure(Object cstructure, List<ProofConstraint> proofTree, String rootId, String currentParent) {
        ProofConstraint potentialPC = null;
        //Check if there is a proof object associated with the current rootNode
        if (proofTree == null || proofTree.isEmpty()) {
            proofTree = new ArrayList<>();
            ProofConstraint proofConstraint = buildProofTree(fs, rootId);
            if (proofConstraint != null) {
                proofTree.add(proofConstraint);
                LOGGER.info("Current parent node: " + currentParent);
                LOGGER.info("Current proof tree: " + proofConstraint.node + " " + proofConstraint.elements + " " + proofConstraint.daughters);
            }
        } else {
            potentialPC = buildProofTree(fs, rootId);
            if (potentialPC != null) {
                //potentialPC.node = currentParent;
                /*
                if (!glueTree.containsKey(currentParent))
                {
                 glueTree.put(currentParent,new HashSet<>());
                }
                glueTree.get(currentParent).add(potentialPC.node);
                 */
                proofTree.add(potentialPC);
                LOGGER.info("Current parent node: " + currentParent);
                LOGGER.info("Current proof tree: " + potentialPC.node + " " + potentialPC.elements + " " + potentialPC.daughters);
            }
        }
        //Determine where mcs are anchored (currentParent)


        if (proofTree.size() == 2)
        {
            System.out.println("Debug stop");
        }

        String currentCproj = getCProj(rootId);

        ProofConstraint currentPC = null;

        if (potentialPC != null) {
            currentPC = potentialPC;
            if (currentPC.elements.contains(currentCproj)) {
                currentParent = currentPC.node;
                    /*
                    if (lastParent != null){
                    glueTree.get(lastParent).add(currentParent);
                    }
                    */
            } else {
                for (String key : currentPC.daughters.keySet()) {
                    if (currentPC.daughters.get(key).contains(currentCproj)) {
                        currentParent = key;
                            /*
                            if (lastParent != null){
                                glueTree.get(lastParent).add(currentParent);
                            }
                             */
                        break;
                    }
                }
            }
        }

        if (currentCproj != null && currentPC == null) {

            if (!proofTree.isEmpty()){

                /*
                String lastParent = null;
                if (currentParent != null) {
                    lastParent = currentParent;
                }
                 */

            for (int i = proofTree.size() - 1; i >= 0; i--)
            {
                ProofConstraint proofConstraint = proofTree.get(i);

                if (proofConstraint.elements.contains(currentCproj)) {
                    currentPC = proofConstraint;
                    currentParent = proofConstraint.node;
                    /*
                    if (lastParent != null){
                    glueTree.get(lastParent).add(currentParent);
                    }
                     */
                    break;
                } else {
                    for (String key : proofConstraint.daughters.keySet()) {
                        if (proofConstraint.daughters.get(key).contains(currentCproj)) {
                            currentPC = proofConstraint;
                            currentParent = key;
                            /*
                            if (lastParent != null){
                                glueTree.get(lastParent).add(currentParent);
                            }
                             */
                            break;
                        }
                    }
                }
            }
            }

            if (currentPC == null && !proofTree.isEmpty())
            {
                currentPC = proofTree.get(proofTree.size()-1);
                currentParent = currentPC.node;
            }
            //for debugging
            if (currentParent != null) {
                if (!prooftreeToCStructure.containsKey(currentParent)) {
                    prooftreeToCStructure.put(currentParent, new ArrayList<>());
                }
                prooftreeToCStructure.get(currentParent).add(rootId);
            }


            //Create glue tree encoding scope of mcs
            if (currentParent != null && currentPC != null) {
                if (!glueTree.containsKey(currentParent)) {
                    glueTree.put(currentParent, new HashSet<>());
                }

                for (String daughter : currentPC.daughters.keySet()) {
                    if (!daughter.equals(currentParent)) {
                        glueTree.get(currentParent).add(daughter);
                    }
                }
            }

            // glueTree.get(currentParent).addAll(proofConstraint.daughters.keySet());

            if (!visitedGlueNodes.contains(currentCproj)) {
                //Find MCs and associate with currentParent if available
                McContainer mcs = findMCNodes(currentCproj, fs);
                if (mcs != null) {

                    if (currentParent == null) {
                        if (!associatedMCs.containsKey("null")) {
                            associatedMCs.put("null", new HashSet<>());
                        }
                        associatedMCs.get("null").addAll(mcs.mcNodes);
                    } else {
                        if (!associatedMCs.containsKey(currentParent)) {
                            associatedMCs.put(currentParent, new HashSet<>());
                        }
                        associatedMCs.get(currentParent).addAll(mcs.mcNodes);
                    }
                    visitedGlueNodes.add(currentParent);
                }
            }
        }


        if (((LinkedHashMap) cstructure).get(rootId) != null) {
            //if available, recurse down right node first
            if (((LinkedHashMap) cstructure).get(rootId) instanceof Object[]) {
                Object[] keySet = (Object[]) ((LinkedHashMap) cstructure).get(rootId);
                if (keySet[1] != null) {
                    Object key = ((LinkedHashMap) keySet[1]).keySet().stream().findAny().get();
                    traverseCStructure(keySet[1], proofTree, (String) key, currentParent);
                }
                Object key2 = ((LinkedHashMap) keySet[0]).keySet().stream().findAny().get();
                traverseCStructure(keySet[0], proofTree, (String) key2, currentParent);
            }
        }
    }

    public String getCProj(String node) {
        Set<GraphConstraint> projSet = fs.cStructureFacts.stream().
                filter(x -> x.getFsNode().equals(node) && x.getRelationLabel().equals("cproj")).
                collect(Collectors.toSet());
        if (!projSet.isEmpty()) {
            return projSet.stream().findAny().map(GraphConstraint::getFsValue).get().toString();
        }
        return null;
    }

    public String getCProjProofNode(String node) {
        QueryParser qp = new QueryParser(fs);
        qp.generateQuery("*" + node + " !(cproj>t::) #t");
        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        if (qpr.isSuccess) {
            if (qpr.result.size() == 1) {
                String tNode = qpr.result.keySet().stream().findAny().get().stream().filter(c -> c.variable.equals("t")).map(c -> c.reference).findFirst().get();
                return tNode;
            }
        }
            return null;
    }

    public String getPhi(String node) {
        Set<GraphConstraint> projSet = fs.cStructureFacts.stream().
                filter(x -> x.getFsNode().equals(node) && x.getRelationLabel().equals("phi")).
                collect(Collectors.toSet());
        if (!projSet.isEmpty()) {
            return projSet.stream().findAny().map(GraphConstraint::getFsValue).get().toString();
        }
        return null;
    }

    public ProofConstraint buildProofTree(Fstructure fs, String rootNode) {
        QueryParser qp = new QueryParser(fs);
        qp.generateQuery("*" + rootNode + " !(cproj>t::) #t");
        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        if (!qpr.isSuccess)
        {
            qp.generateQuery("*" + rootNode + " !(phi>t::) #t");
           qpr = qp.parseQuery(qp.getQueryList());
        }

        if (qpr.isSuccess) {
            if (qpr.result.size() == 1) {
                String tNode = qpr.result.keySet().stream().findAny().get().stream().filter(c -> c.variable.equals("t")).map(c -> c.reference).findFirst().get();

                if (visitedProofNodes.contains(tNode))
                {//If tnode in visited Proof nodes, an adeqquate proofconstraint must already exist
                    return null;
                }

                visitedProofNodes.add(tNode);
                String daughterNode = null;
                List<GraphConstraint> proofConstraints = fs.returnFullGraph().stream().filter(c -> c.getFsNode().equals(tNode)).collect(Collectors.toList());

                Set<String> elementConstraints = new HashSet<>();
                HashMap<String, Set<String>> daughters = new HashMap<>();

                for (GraphConstraint c : proofConstraints) {

                    if (c.getRelationLabel().equals("ELEMENTS")) {
                        Set<String> elementSetConstraints = fs.returnFullGraph().stream().filter(x ->
                                        x.getFsNode().equals(c.getFsValue()) && x.getRelationLabel().equals("in_set")).
                                map(GraphConstraint::getFsValue).map(Object::toString).collect(Collectors.toSet());

                        if (!elementSetConstraints.isEmpty()) {
                            elementConstraints = elementSetConstraints;
                        }
                    }
                    Matcher m = daughterPattern.matcher(c.getRelationLabel());
                    if (m.matches()) {

                        daughterNode = fs.returnFullGraph().stream().filter(gc ->
                                        gc.getFsNode().equals(c.getFsValue()) && gc.getRelationLabel().equals("ELEMENTS")).
                                findFirst().map(gc -> gc.getFsValue().toString()).orElse("");

                        if (!daughters.containsKey(c.getRelationLabel())) {
                            daughters.put(daughterNode, new HashSet<>());
                        }

                        if (!daughterNode.isEmpty()) {
                            String finalDaughterNode = daughterNode;
                            Set<String> elementSetConstraints = fs.returnFullGraph().stream().filter(x ->
                                            finalDaughterNode.equals(x.getFsNode()) && x.getRelationLabel().equals("in_set")).
                                    map(GraphConstraint::getFsValue).map(Object::toString).collect(Collectors.toSet());

                            if (!elementSetConstraints.isEmpty()) {
                                daughters.get(daughterNode).addAll(elementSetConstraints);
                            }
                        }
                    }
                }
                return new ProofConstraint(tNode, elementConstraints, daughters);
            } else {
                LOGGER.warn("Mapping from c-structure to t-structure failed. Mapping is incoherent...");
                return null;
            }
        }

        return null;
    }


    public McContainer findMCNodes(String cstrNode, Fstructure fs) {
        QueryParser qp = new QueryParser(fs);
        qp.generateQuery("*" + cstrNode + " g:: #g !(GLUE>in_set) #s");

        QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

        if (qpr.isSuccess) {
            String gNode = qpr.result.keySet().stream().findAny().get().stream().filter(c -> c.variable.equals("g")).map(c -> c.reference).findFirst().get();

            Set<String> mcNodes = new HashSet<>();

            for (Set<SolutionKey> key : qpr.result.keySet())

            mcNodes.addAll(key.stream().filter(c -> c.variable.equals("s")).map(c -> c.reference).collect(Collectors.toSet()));

            return new McContainer(gNode, mcNodes);

        }
        return null;
    }

    public String findGlueTreeRoot() {
        //Find key in glueTree that is not value of any other key
        Set<String> glueTreeRoots = glueTree.keySet().stream().
                filter(x -> glueTree.values().stream().
                        noneMatch(y -> y.contains(x))).collect(Collectors.toSet());

        if (glueTreeRoots.size() == 1) {
            return glueTreeRoots.stream().findAny().get();
        }
        LOGGER.warn("Failed to find glue tree root. Glue tree is incoherent...");
        return null;
    }


    public void translateGlueTreeToList(String rootNode, List<Object> treeString) {
        if (associatedMCs2.containsKey(rootNode)) {
            treeString.add("{");
            treeString.addAll(associatedMCs2.get(rootNode));
        }
        if (glueTree2.containsKey(rootNode)) {
            treeString.add("//" + rootNode + ": " + "{" + associatedMCs2.get(rootNode) + "}");
            for (String node : glueTree2.get(rootNode)) {
                    translateGlueTreeToList(node, treeString);
                }
        }
        if (associatedMCs2.containsKey(rootNode)) {
            treeString.add("}");
        }
    }

    public String printGlueTree()
    {
        return glueTree2.toString();
    }
}