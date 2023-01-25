package de.ukon.liger.annotation;

import com.google.gson.Gson;
import glueSemantics.semantics.lambda.SemType;

import java.lang.annotation.Annotation;
import java.util.*;

public class LigerAnnotation {

    public String text;

    //Annotations at current level (attribute/value pairs)
    public Map<String,Object> annotations;
    //Annotation at any level (words and spans); Only top level annotation should have elementAnnotations
    public LinkedHashMap<String,LigerAnnotation> elementAnnotations;
    public String id;
    public AnnotationTypes type;

    public LigerAnnotation(String id){
        this.annotations = new HashMap<>();
        this.elementAnnotations = new LinkedHashMap<>();
        this.id = id;
        this.type = AnnotationTypes.liger;
    }

    public LigerAnnotation(String id, String text){
        this.text = text;
        this.annotations = new HashMap<>();
        this.elementAnnotations = new LinkedHashMap<>();
        this.id = id;
        this.type = AnnotationTypes.liger;
    }

    public void addElementAnnotation(LigerAnnotation la){
        elementAnnotations.put(la.id,la);
    }


    public LinkedHashMap returnLigerAnnotation()
    {
        LinkedHashMap<String,Object> ligerMap = new LinkedHashMap<>();

        ligerMap.put("text",this.text);
        ligerMap.put("id",this.id);

        if (!annotations.keySet().isEmpty())
        {
            ligerMap.put("annotations",annotations);
        }

        for (AnnotationTypes type : AnnotationTypes.values())
        {
            List<LigerAnnotation> members = new ArrayList<>();

            elementAnnotations.forEach((k,val) -> {if (((LigerAnnotation) val).type.equals(type))
        {
            members.add(val);
        }});


            if (!members.isEmpty()) {
                ligerMap.put(type.toString(), new ArrayList<>());
                for (LigerAnnotation member : members) {
                    ((List) ligerMap.get(type.toString())).add(member.returnLigerAnnotation());
                }
            }
        }

        LinkedHashMap<String,Object> output = new LinkedHashMap<>();
        output.put("ligerAnnotation",ligerMap);

        return output;

    }

    public String returnAsJson()
    {
        Gson gson = new Gson();

        return gson.toJson(this.returnLigerAnnotation());
    }

}
