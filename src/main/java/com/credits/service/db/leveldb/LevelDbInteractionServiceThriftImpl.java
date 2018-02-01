package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.thrift.gen.api.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

@Component
public class LevelDbInteractionServiceThriftImpl implements LevelDbInteractionService {

    private final static Logger logger = LoggerFactory.getLogger(LevelDbInteractionServiceThriftImpl.class);

    private API.Client client;
    private TTransport transport;

    @Value("${api.server.host}")
    private String apiServerHost;

    @Value("${api.server.port}")
    private Integer apiServerPort;

    @PostConstruct
    public void setUp() {
        try {
            transport = new TSocket(apiServerHost, apiServerPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new API.Client(protocol);
        } catch (TTransportException e) {
            logger.error("Cannot connect to database. " + e);
        }
    }

    @Override
    public BalanceGetResult getBalance(String address, String currency) throws ContractExecutorException {
        BalanceGetResult result;
        try {
            result = client.BalanceGet(address, currency);
        } catch (TException e) {
            throw new ContractExecutorException("Cannot get balance. ", e);
        }
        return result;
    }

    @Override
    public TransactionGetResult getTransaction(String transactionId) throws ContractExecutorException {
        TransactionGetResult result;
        try {
            result = client.TransactionGet(transactionId);
        } catch (TException e) {
            throw new ContractExecutorException("Cannot get transaction. ", e);
        }
        return result;
    }

    @Override
    public TransactionsGetResult getTransactions(String address, long offset, long limit) throws ContractExecutorException {
        TransactionsGetResult result;
        try {
            result = client.TransactionsGet(address, offset, limit);
        } catch (TException e) {
            throw new ContractExecutorException("Cannot get transactions. ", e);
        }
        return result;
    }

    @Override
    public PoolListGetResult getPoolList(long offset, long limit) throws ContractExecutorException {
        PoolListGetResult result;
        try {
            result = client.PoolListGet(offset, limit);
        } catch (TException e) {
            throw new ContractExecutorException("Cannot get pool list. ", e);
        }
        return result;
    }

    @Override
    public PoolGetResult getPool(long poolNumber) throws ContractExecutorException {
        PoolGetResult result;
        try {
            result = client.PoolGet(poolNumber);
        } catch (TException e) {
            throw new ContractExecutorException("Cannot get pool. ", e);
        }
        return result;
    }

    @PreDestroy
    public void close() {
        if (transport != null) {
            transport.close();
        }
    }
}
