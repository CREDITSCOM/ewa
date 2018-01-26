package com.credits.service.db.leveldb;

import com.credits.thrift.gen.api.API;
import com.credits.thrift.gen.api.Amount;
import com.credits.thrift.gen.api.Transaction;
import com.credits.thrift.gen.api.TransactionInfo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LevelDbInteractionServiceTest {
    private TTransport transport;
    private API.Client client;

    @Before
    public void setUp() {
        try {
            transport = new TSocket("localhost", 9090);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new API.Client(protocol);
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    @After
    public void close() {
        if (transport != null) {
            transport.close();
        }
    }

    @Test
    public void perform() throws TException {
        System.out.println("get_balance()");
        java.util.Map<java.lang.String,Amount> balance = client.get_balance("3QJmV3qfvL9SuYo34YihAf3sRCW3qSinyC");
        System.out.println("get_balance=" + balance);

        System.out.println("get_transactions()");
        java.util.List<Transaction> transactions = client.get_transactions("3QJmV3qfvL9SuYo34YihAf3sRCW3qSinyC", "BTC");
        System.out.println("get_transactions=" + transactions);

        System.out.println("get_transaction_info()");
        TransactionInfo info = client.get_transaction_info("3QJmV3qfvL9SuYo34YihAf3sRCW3qSinyC", "3QvxvxuotS5PuTjmVUpWN6sVkfzUfX3RFV", new Amount(13, 37), 0, "DASH");
        System.out.println("get_transaction_info=" + info);
    }

}
