package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.thrift.gen.api.API;
import com.credits.thrift.gen.api.Amount;
import com.credits.thrift.gen.api.TransactionInfo;
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

    public Map<String, Amount> getBalance(String address) throws ContractExecutorException {
        Map<String, Amount> result;
        try {
            result = client.get_balance(address);
        } catch (TException e) {
            throw new ContractExecutorException("Cannot get balance", e);
        }
        return result;
    }

    public List<com.credits.thrift.gen.api.Transaction> getTransactions(String address, String currency) throws ContractExecutorException {
        List<com.credits.thrift.gen.api.Transaction> result;
        try {
            result = client.get_transactions(address, currency);
        } catch (TException e) {
            throw new ContractExecutorException("Cannot get transactions", e);
        }
        return result;
    }

    public TransactionInfo getTransactionInfo(String source, String destination, Amount amount, long timestamp, String currency) throws ContractExecutorException {
        TransactionInfo result;
        try {
            result = client.get_transaction_info(source, destination, amount, timestamp, currency);
        } catch (TException e) {
            throw new ContractExecutorException("Cannot get transaction info", e);
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
