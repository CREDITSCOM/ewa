package com.credits.service;

import com.credits.exception.ContractExecutorException;
import com.credits.vo.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class DatabaseInteractionService {

    private String dbUrl;
    private RestTemplate restTemplate;

    @Autowired
    public DatabaseInteractionService(RestTemplate restTemplate, @Value("${leveldb.url}") String url) {
        this.restTemplate = restTemplate;
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.dbUrl = url;
    }

    public Transaction[] get(String id) {
        String request = dbUrl + "?id={id}";
        Transaction[] response = restTemplate.getForObject(request, Transaction[].class, id);
        return response;
    }

    public void post(Transaction transaction) throws ContractExecutorException {
        ResponseEntity response = restTemplate.postForEntity(dbUrl, transaction, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new ContractExecutorException("Cannot save data, http status code: "
                + response.getStatusCode().value() + "Reason: " + response.getBody());
        }
    }
}
