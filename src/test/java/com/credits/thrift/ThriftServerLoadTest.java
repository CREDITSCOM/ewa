package com.credits.thrift;

import org.apache.commons.io.FileUtils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

import static java.io.File.separator;

public class ThriftServerLoadTest {

    private File file;

    private ByteBuffer bytes;

    @Before
    public void setUp() throws TTransportException {
        String fileName = "Contract.java";
        file = new File(fileName);
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/thriftServerLoadTest/" + fileName)) {
            FileUtils.copyToFile(stream, file);
            bytes = ByteBuffer.wrap(FileUtils.readFileToByteArray(file));
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @After
    public void tearDown() {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileSystemUtils.deleteRecursively(new File(dir));
    }

    @Test
    public void store() {
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Thread t = new Thread("" + i) {
                @Override
                public void run() {
                    System.out.println("Starting new thread" + this.getName());
                    try {
                        TTransport transport = new TSocket("localhost", 9080);
                        transport.open();
                        TProtocol protocol = new TBinaryProtocol(transport);
                        ContractExecutor.Client client = new ContractExecutor.Client(protocol);
                        // TODO: 6/20/2018 required place client.executeBytecode() here
//                        APIResponse response = client.store(new ContractFile("Contract.java", bytes), String.valueOf(Math.abs(new Random().nextInt())), "ekiT2ej+PL+eeaydVVpkvuuLWDXY7r9pZTsO4wosnVuvN5CHjFO2aSR65IBI8zl9T4jMDkutsGPAVRAeYvOKnQ==");
//                        System.out.println(response.getCode() + " " + response.getMessage());
                        transport.close();
                    } catch (TTransportException e) {
                        System.out.println(e.getMessage() + e);
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        try {
            for (Thread t : threads) {
                if (t.isAlive()) {
                    t.join();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        file.delete();
    }

//    @Test
//    public void execute() throws TException {
//        client.execute("987", "foo", null);
//    }
}
