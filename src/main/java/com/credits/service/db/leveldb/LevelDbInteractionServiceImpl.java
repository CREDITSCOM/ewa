package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.thrift.gen.api.Amount;
import com.credits.thrift.gen.api.TransactionInfo;
import com.credits.vo.usercode.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


//@Component
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
}
