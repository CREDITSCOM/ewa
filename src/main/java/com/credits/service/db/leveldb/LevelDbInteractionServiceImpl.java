package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.vo.usercode.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;


@Component
public class LevelDbInteractionServiceImpl implements LevelDbInteractionService {

    private RestTemplate restTemplate;

    @Resource
    private RestTemplateBuilder builder;

    @Value("${leveldb.url}")
    private String dbUrl;

    @Value("${leveldb.url}?id={id}&value={value}")
    private String getDbUrl;

    @PostConstruct
    private void postConstruct() {
        restTemplate = builder.build();
    }

    public Transaction[] get(String id, int value) {
        Transaction[] response = restTemplate.getForObject(getDbUrl, Transaction[].class, id, value);
        return response;
    }

    public void put(Transaction transaction) throws ContractExecutorException {
        ResponseEntity response = builder.build().postForEntity(dbUrl, transaction, Void.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new ContractExecutorException("Cannot save data, http status code: "
                + response.getStatusCode().value());
        }
    }
}
