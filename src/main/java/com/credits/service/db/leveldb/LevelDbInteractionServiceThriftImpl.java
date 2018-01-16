package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.thrift.gen.SharedService;
import com.credits.vo.usercode.Transaction;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class LevelDbInteractionServiceThriftImpl implements LevelDbInteractionService {

    private SharedService.Client client;
    private TTransport transport;

    public LevelDbInteractionServiceThriftImpl() {
        try {
            transport = new TSocket("localhost", 9090);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new SharedService.Client(protocol);
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Transaction[] get(String id, int value) {
        return new Transaction[0];
    }

    @Override
    public void put(Transaction transaction) throws ContractExecutorException {

    }

    @PreDestroy
    public void close() {
        if (transport != null) {
            transport.close();
        }
    }
}
