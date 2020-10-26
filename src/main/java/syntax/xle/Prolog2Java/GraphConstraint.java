package syntax.xle.Prolog2Java;

import utilities.HelperMethods;

import java.io.Serializable;
import java.util.List;

public class GraphConstraint implements Serializable {



    //basic string based representation of graph nodes and labeled edges
    private Object reading;
    private String nodeIdentifier;
    private String relationLabel;
    private Object fsValue;


    public GraphConstraint()
    {}

    public GraphConstraint(String reading, Integer fsNode, String relationLabel, Object fsValue)
    {
        this.reading = reading;
        this.nodeIdentifier = fsNode.toString();
        this.relationLabel = relationLabel;
        this.fsValue = fsValue;
    //    this.pathNodes = new HashSet<>();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (reading != null) {
            sb.append("[" + reading + "]");
        }
        sb.append(" ");
        sb.append("#" + nodeIdentifier);
        sb.append(" ");
        sb.append(relationLabel);
        sb.append(" ");
        if(HelperMethods.isInteger(fsValue))
        {
            sb.append("#" + fsValue);
        }
        else
        {
            sb.append(fsValue);
        }

        return sb.toString();

    }


    public static GraphConstraint returnRoot(List<GraphConstraint> fs)
    {
        //TODO This only works for proper f-structures; not fractured ones
        for (GraphConstraint fsc : fs)
        {
            if (fsc.getFsNode().equals(0) && fsc.getRelationLabel().equals("PRED"))
            {
                return fsc;
            }
        }
        return null;
    }


    //TODO this may be more suitable somewhere else?




    //for a given graph-constraint give a valid FsPath that is compatible with the annotation of the sentence
    // currently being annotated (via variable handler)
/*
    public FsPath graphConstraintasPath(List<GraphConstraint> lg, GraphConstraint gc, VariableHandler vh)
    {

        FsPath out = new FsPath(gc,lg,vh,true);


        return out;
    }

*/
    //Getter and Setter
    public String getFsNode() {
        return nodeIdentifier;
    }

    public void setFsNode(String fsNode) {
        this.nodeIdentifier = fsNode;
    }

    public String getRelationLabel() {
        return relationLabel;
    }

    public void setRelationLabel(String relationLabel) {
        this.relationLabel = relationLabel;
    }

    public Object getFsValue() {
        return fsValue;
    }

    public void setFsValue(Object fsValue) {
        this.fsValue = fsValue;}


    public Object getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

}



