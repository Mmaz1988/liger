package de.ukon.liger.webservice.rest.dtos;

public class LigerArgument {
    public String premise;
    public String conclusion;
    public String relation;

    public String arg_id;

    public LigerArgument(String premise, String conclusion, String relation)
    {
        this.premise = premise;
        this.conclusion = conclusion;
        this.relation = relation;
    }
}
