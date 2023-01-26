package de.ukon.liger.webservice.rest;

import de.ukon.liger.webservice.rest.dtos.GkrDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

@Service
public class LigerService {

    private RestTemplate restTemplate = new RestTemplate();


    public LinkedHashMap accessGKR(GkrDTO gkrData){

        final HttpHeaders httpHeaders = new HttpHeaders();

        //define dto with sentence and context for gkr



        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<GkrDTO> requestHttpEntity = new HttpEntity<>(gkrData, httpHeaders);

        String gkrURL = "https://public.gkr.services.lingvis.io/gkr/sentence";

       LinkedHashMap result =  restTemplate.postForObject(gkrURL, requestHttpEntity,LinkedHashMap.class);

        return result;
    }

}
