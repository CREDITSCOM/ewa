package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
<<<<<<< Updated upstream
=======
import com.credits.thrift.gen.api.Amount;
import com.credits.thrift.gen.api.TransactionInfo;
>>>>>>> Stashed changes
import com.credits.vo.usercode.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
<<<<<<< Updated upstream
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
=======
import org.springframework.http.ResponseEntity;
>>>>>>> Stashed changes
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
<<<<<<< Updated upstream
import java.util.Arrays;


@Component
=======
import java.util.List;
import java.util.Map;


//@Component
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======

    @Override
    public Map<String, Amount> getBalance(String address) {
        return null;
    }

    @Override
    public List<com.credits.thrift.gen.api.Transaction> getTransactions(String address, String currency) {
        return null;
    }

    @Override
    public TransactionInfo getTransactionInfo(String source, String destination, Amount amount, long timestamp, String currency) {
        return null;
    }
>>>>>>> Stashed changes
}
