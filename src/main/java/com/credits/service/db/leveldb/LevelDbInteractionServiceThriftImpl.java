package com.credits.service.db.leveldb;

import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.PoolData;
import com.credits.leveldb.client.data.TransactionData;
import com.credits.leveldb.client.data.TransactionFlowData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
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
    public BigDecimal getBalance(String address, String currency) throws LevelDbClientException, CreditsNodeException {
        return client.getBalance(address, currency);
    }

    @Override
    public TransactionData getTransaction(String transactionId) throws LevelDbClientException, CreditsNodeException {
        return client.getTransaction(transactionId);
    }

    @Override
    public List<TransactionData> getTransactions(String address, long offset, long limit) throws LevelDbClientException, CreditsNodeException {
        return client.getTransactions(address, offset, limit);
    }

    @Override
    public List<PoolData> getPoolList(long offset, long limit) throws LevelDbClientException, CreditsNodeException {
        return client.getPoolList(offset, limit);
    }

    @Override
    public PoolData getPoolInfo(byte[] hash, long index) throws LevelDbClientException, CreditsNodeException {
        return client.getPoolInfo(hash, index);
    }

    @Override
    public void transactionFlow(String innerId,
                                String source,
                                String target,
                                BigDecimal amount,
                                BigDecimal balance,
                                String currency,
                                String signature,
                                BigDecimal fee) throws LevelDbClientException, CreditsNodeException {
        TransactionFlowData transactionFlowData =
            new TransactionFlowData(innerId, source, target, amount, balance, currency, signature, fee);
        client.transactionFlow(transactionFlowData, true);
    }
}
