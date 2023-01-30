package de.ukon.liger.webservice.rest.dtos;

import java.util.LinkedHashMap;

public class LigerArgumentListQuery {
    public LinkedHashMap<String,LigerArgument> mpg_arguments;
    public String query;

    public LigerArgumentListQuery(LinkedHashMap<String,LigerArgument> mpgArguments,
                                  String query)
    {
        this.mpg_arguments = mpgArguments;
        this.query = query;
    }
}


