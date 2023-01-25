package de.ukon.liger.annotation;

import java.util.LinkedHashMap;

public class Word extends LigerAnnotation {

    LigerAnnotation parent;
public Word(String id, String text, LigerAnnotation parent){
    super(id,text);
    this.parent = parent;
    this.type = AnnotationTypes.words;
}


@Override
public LinkedHashMap<String,Object> returnLigerAnnotation(){

    LinkedHashMap<String,Object> ligerMap = new LinkedHashMap<>();

    ligerMap.put("text",this.text);
    ligerMap.put("id",this.id);

    ligerMap.put("annotations",this.annotations);

    return ligerMap;
}


}
