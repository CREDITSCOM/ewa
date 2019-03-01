package com.credits.thrift;

import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static java.io.File.separator;

public class ThriftServerLoadTest {

    private File file;

    @Before
    public void setUp() {
        String fileName = "Contract.java";
        file = new File(fileName);
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/thriftServerLoadTest/" + fileName)) {
            FileUtils.copyToFile(stream, file);
            ByteBuffer.wrap(FileUtils.readFileToByteArray(file));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @After
    public void tearDown() throws IOException {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileUtils.deleteDirectory(new File(dir));
    }

    @Ignore
    @Test
    public void store() {
        NodeApiService nodeApiService = NodeApiServiceImpl.getInstance("localhost", 9080);
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Thread t = new Thread("" + i) {
                @Override
                public void run() {
                    System.out.println("Starting new thread" + this.getName());
                    //                        TTransport transport = new TSocket("localhost", 9080);
                    //                        transport.open();
                    //                        TProtocol protocol = new TBinaryProtocol(transport);
                    //                        ContractExecutor.Client client = new ContractExecutor.Client(protocol);
                    nodeApiService.getBalance("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe");
                    // TODO: 6/20/2018 required place client.executeBytecode() here
                    //                        APIResponse response = client.store(new ContractFile("MySmartContract.java", bytes), String.valueOf(Math.abs(new Random().nextInt())), "ekiT2ej+PL+eeaydVVpkvuuLWDXY7r9pZTsO4wosnVuvN5CHjFO2aSR65IBI8zl9T4jMDkutsGPAVRAeYvOKnQ==");
                    //                        System.out.println(response.getCode() + " " + response.getMessage());
                    //                        transport.close();
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
//        file.delete();
    }
}
