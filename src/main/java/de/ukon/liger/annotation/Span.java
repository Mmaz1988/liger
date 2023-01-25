package de.ukon.liger.annotation;

import java.util.LinkedHashMap;

public class Span extends LigerAnnotation {


    public String startsWith;
    public String endsWith;
    public String name;
    public LigerAnnotation parent;

    public Span(String id, String text, AnnotationTypes name, String startsWith, String endsWith, LigerAnnotation parent)
    {
        super(id);
        this.text = text;
        this.type = name;
        this.startsWith = startsWith;
        this.endsWith = endsWith;
        this.parent = parent;

    }

    @Override
    public LinkedHashMap<String,Object> returnLigerAnnotation(){

        LinkedHashMap<String,Object> ligerMap = new LinkedHashMap<>();

        ligerMap.put("text",this.text);
        ligerMap.put("id",this.id);
        ligerMap.put("startsWith",this.startsWith);
        ligerMap.put("endsWith",this.endsWith);
        ligerMap.put("annotations",this.annotations);

        return ligerMap;
    }


}
