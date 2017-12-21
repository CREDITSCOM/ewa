package com.credits.service;

import com.credits.vo.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


//@Component
public class DatabaseInteractionService {

    private final static String ROOT_URL = "http://localhost:8080/submitJava/do";
    private final static String REQUEST_GET_FORM = "%s?id=%s";

    private RestTemplate restTemplate;

    //@Autowired
    public DatabaseInteractionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public Transaction[] get(String id) {
        String request = String.format(REQUEST_GET_FORM, ROOT_URL, id);
        Transaction[] response = restTemplate.getForObject(request, Transaction[].class);
        for (Transaction t : response)
        System.out.println(t);
        return response;
    }

    public void post(Transaction transaction) {
        String s = restTemplate.postForObject(ROOT_URL, transaction, String.class);
    }

    public static void main(String[] args) {
        RestTemplate rt = new RestTemplate();
        DatabaseInteractionService databaseInteractionService = new DatabaseInteractionService(rt);
        //databaseInteractionService.get("1");
        databaseInteractionService.post(new Transaction("123", 456, '+'));
    }

}
