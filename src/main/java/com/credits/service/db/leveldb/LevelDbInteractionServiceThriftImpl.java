package com.credits.service.db.leveldb;

import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.PoolData;
import com.credits.leveldb.client.TransactionData;
import com.credits.leveldb.client.data.TransactionFlowData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
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
        client = ApiClient.getInstance(apiServerHost, apiServerPort);
    }


    @Override
    public BigDecimal getBalance(String address, String currency) throws Exception {
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
    public PoolData getPool(String poolNumber) throws Exception {
        return client.getPool(poolNumber);
    }

    @Override
    public void transactionFlow(String innerId,
                                String source,
                                String target,
                                BigDecimal amount,
                                BigDecimal balance,
                                String currency,
                                String signature) throws Exception {
        TransactionFlowData transactionFlowData =
            new TransactionFlowData(innerId, source, target, amount, balance, currency, signature);
        client.transactionFlow(transactionFlowData, false);
    }

    @Override
    public void transactionFlowWithFee(TransactionFlowData transactionFlowData, TransactionFlowData transactionFlowDataFee, boolean checkBalance) throws Exception {
        client.transactionFlowWithFee(transactionFlowData, transactionFlowDataFee, checkBalance);
    }
}
