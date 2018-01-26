package com.credits.service.db.leveldb;

import com.credits.thrift.gen.api.API;
import com.credits.thrift.gen.api.Amount;
import com.credits.thrift.gen.api.TransactionInfo;
import com.credits.vo.usercode.Transaction;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

@Component
public class LevelDbInteractionServiceThriftImpl implements LevelDbInteractionService {

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
            e.printStackTrace();
        }
    }

    @Override
    public Transaction[] get(String id, int value) {
        return new Transaction[0];
    }

    @Override
    public void put(Transaction transaction) {
    }

    public Map<String, Amount> getBalance(String address) {
        Map<String, Amount> result = null;
        try {
            result = client.get_balance(address);
        } catch (TException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<com.credits.thrift.gen.api.Transaction> getTransactions(String address, String currency) {
        List<com.credits.thrift.gen.api.Transaction> result = null;
        try {
            result = client.get_transactions(address, currency);
        } catch (TException e) {
            e.printStackTrace();
        }
        return result;
    }

    public TransactionInfo getTransactionInfo(String source, String destination, Amount amount, long timestamp, String currency) {
        TransactionInfo result = null;
        try {
            result = client.get_transaction_info(source, destination, amount, timestamp, currency);
        } catch (TException e) {
            e.printStackTrace();
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
