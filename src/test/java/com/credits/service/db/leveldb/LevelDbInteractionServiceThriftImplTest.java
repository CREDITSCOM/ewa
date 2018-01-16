package com.credits.service.db.leveldb;

import com.credits.thrift.gen.SharedService;
import com.credits.thrift.gen.SharedStruct;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class LevelDbInteractionServiceThriftImplTest {
    public SharedService.Client client;
    public TTransport transport;

    public static void main(String[] args) {
        SharedService.Client client = null;
        TTransport transport = null;

        try {
            transport = new TSocket("localhost", 9090);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new SharedService.Client(protocol);

            client.putPair(1111, "diman");
            SharedStruct s = client.getStruct(1111);
            System.out.println(s.getKey());
            System.out.println(s.getValue());
        } catch (TException e) {
            e.printStackTrace();
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }

}
