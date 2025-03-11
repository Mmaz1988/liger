/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

package de.ukon.liger.syntax;

import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.utilities.HelperMethods;
import org.springframework.boot.actuate.endpoint.web.Link;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GraphConstraint implements Serializable {



    //basic string based representation of graph nodes and labeled edges
    private Set<ChoiceVar> reading;
    private String nodeIdentifier;
    private String relationLabel;
    private String fsValue;
    private Boolean projection;

    private String proj;
    private boolean root;


    public GraphConstraint()
    {this.projection = false;}

    public GraphConstraint(Set<ChoiceVar> reading, Integer fsNode, String relationLabel, String fsValue)
    {
        this.reading = reading;
        this.nodeIdentifier = fsNode.toString();
        this.relationLabel = relationLabel;
        this.fsValue = fsValue;
        this.projection = false;
    //    this.pathNodes = new HashSet<>();
    }

    public GraphConstraint(Set<ChoiceVar> reading, Integer fsNode, String relationLabel, String fsValue, Boolean projection)
    {
        this.reading = reading;
        this.nodeIdentifier = fsNode.toString();
        this.relationLabel = relationLabel;
        this.fsValue = fsValue;
        this.projection = projection;
        //    this.pathNodes = new HashSet<>();
    }

    public GraphConstraint(Set<ChoiceVar> reading, Integer fsNode, String relationLabel, String fsValue, String projection)
    {
        this.reading = reading;
        this.nodeIdentifier = fsNode.toString();
        this.relationLabel = relationLabel;
        this.fsValue = fsValue;
        this.proj = projection;
        //    this.pathNodes = new HashSet<>();
    }

    public GraphConstraint(Set<ChoiceVar> reading, String fsNode, String relationLabel, String fsValue, String projection, boolean root)
    {
        this.reading = reading;
        this.nodeIdentifier = fsNode;
        this.relationLabel = relationLabel;
        this.fsValue = fsValue;
        this.proj = projection;
        this.root = root;
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


    public LinkedHashMap<String,Object> toJson()
    {
        LinkedHashMap<String,Object> constraintProperties = new LinkedHashMap<>();

        List<LinkedHashMap> choices = new ArrayList<>();

        for (ChoiceVar cv : this.getReading())
        {
            choices.add(cv.toJson());
        }

        constraintProperties.put("choiceVars",choices);
        constraintProperties.put("sourceNode",this.nodeIdentifier);
        constraintProperties.put("relationLabel",this.relationLabel);
        constraintProperties.put("targetNode",this.fsValue);
        constraintProperties.put("projection",this.projection.toString());



        return constraintProperties;
    }

    public static GraphConstraint parseJson(LinkedHashMap<String,Object> input) {
        GraphConstraint g = new GraphConstraint();
        if (input.containsKey("projection")) {
            g.projection = Boolean.parseBoolean((String) input.get("projection"));
        }

        Set<ChoiceVar> choiceVars = new HashSet<>();

        if (input.containsKey("choiceVars")) {
            for (LinkedHashMap cv : (List<LinkedHashMap>) input.get("choiceVars")) {
                choiceVars.add(ChoiceVar.parseJson(cv));
            }
        }

        g.setReading(choiceVars);

        g.setFsNode((String) input.get("sourceNode"));
        g.setRelationLabel((String) input.get("relationLabel"));
        g.setFsValue((String) input.get("targetNode"));

        return g;
    }


    public String toPrologString(){

        String value = fsValue.toString();

        if (HelperMethods.isInteger(value))
        {
            value = "var(" + value + ")";
        }

        String choice;

        if (reading.size() == 1)
        {
            choice = reading.stream().findAny().get().toString();
        }

        else {
            choice = "or(" + reading.stream().map(ChoiceVar::toString).collect(Collectors.joining(",")) + ")";
        }


        if (this.getRelationLabel().equals("in_set") || this.getRelationLabel().equals("subsume"))
        {
            return String.format("cf(%1$s,%2$s(%3$s,var(%4$s)))",choice,getRelationLabel(),value,nodeIdentifier);
        }
        else if (this.projection != null && this.projection)
        {
            return String.format("cf(%1$s,eq(proj(var(%2$s),'%3$s'),%4$s))",choice,nodeIdentifier,getRelationLabel(),value);
        }
        else
        {
            return String.format("cf(%1$s,eq(attr(var(%2$s),'%3$s'),%4$s))",choice,nodeIdentifier,getRelationLabel(),value);
        }


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

    public void setFsValue(String fsValue) {
        this.fsValue = fsValue;}


    public Set<ChoiceVar> getReading() {
        return reading;
    }

    public void setReading(Set<ChoiceVar> reading) {
        this.reading = reading;
    }

    public String getProj() {
        return proj;
    }

    public void setProj(String proj) {
        this.proj = proj;
    }


    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

}



