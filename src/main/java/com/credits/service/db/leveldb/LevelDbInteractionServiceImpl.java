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
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
        builder = builder.messageConverters(converter);
        restTemplate = builder.build();
    }

    public Transaction[] get(String id, int value) {
        Transaction[] response = restTemplate.getForObject(getDbUrl, Transaction[].class, id, value);
        for (Transaction tr : response)
            System.out.println(tr);
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
