package de.ukon.liger.annotation;

import java.util.LinkedHashMap;

public class ArgumentAnnotation extends LigerAnnotation{

    public LigerAnnotation premise;
    public LigerAnnotation conclusion;
    public String argumentRelation;


    public ArgumentAnnotation(String id) {
        super(id);
    }

    public ArgumentAnnotation(String id,LigerAnnotation premise, LigerAnnotation conclusion) {
        super(id);
        this.premise = premise;
        this.conclusion = conclusion;

    }

    @Override
    public LinkedHashMap<String,Object> returnLigerAnnotation(){

        LinkedHashMap<String,Object> ligerMap = new LinkedHashMap<>();

        ligerMap.put("text",this.text);
        ligerMap.put("id",this.id);

        ligerMap.put("premise",this.premise.returnLigerAnnotation());
        ligerMap.put("conclusion",this.conclusion.returnLigerAnnotation());

        ligerMap.put("annotations",this.annotations);

        LinkedHashMap<String,Object> ligerArgumentMap = new LinkedHashMap<>();

        ligerArgumentMap.put("liger_argument",ligerMap);

        return ligerArgumentMap;
    }

}
