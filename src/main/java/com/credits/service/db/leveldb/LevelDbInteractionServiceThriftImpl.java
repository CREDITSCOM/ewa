package com.credits.service.db.leveldb;

import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.PoolData;
import com.credits.leveldb.client.TransactionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class LevelDbInteractionServiceThriftImpl implements LevelDbInteractionService {

    private ApiClient client;

    @Value("${api.server.host}")
    private String apiServerHost;

    @Value("${api.server.port}")
    private Integer apiServerPort;

    @PostConstruct
    public void setUp() {
        client = new ApiClient(apiServerHost, apiServerPort);
    }


    @Override
    public Double getBalance(String address, String currency) throws Exception {
        return client.getBalance(address, currency);
    }

    @Override
    public TransactionData getTransaction(String transactionId) throws Exception {
        return client.getTransaction(transactionId);
    }

    @Override
    public List<TransactionData> getTransactions(String address, long offset, long limit) throws Exception {
        return client.getTransactions(address, offset, limit);
    }

    @Override
    public List<PoolData> getPoolList(long offset, long limit) throws Exception {
        return client.getPoolList(offset, limit);
    }

    @Override
    public PoolData getPool(long poolNumber) throws Exception {
        return client.getPool(poolNumber);
    }
}
