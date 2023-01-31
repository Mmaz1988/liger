package de.ukon.liger.webservice.rest.dtos;

public class GkrDTO {

    public String sentence;
    public String context;



    public GkrDTO(String sentence, String context) {
        this.sentence = sentence;
        this.context = context;
    }
}
