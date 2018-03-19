package com.credits.thrift;

import com.credits.exception.ContractExecutorException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ThriftServerTest {

    private MultipartFile file;

    private ContractExecutor.Client client;

    @Before
    public void setUp() throws ContractExecutorException, TTransportException {
        String fileName = "ContractExecutorServiceTestCode.java";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            file = new MockMultipartFile(fileName, fileName, null, stream);
        } catch (IOException e) {
            System.out.println(e);
        }

        TTransport transport;
        transport = new TSocket("localhost", 9080);
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);
        client = new ContractExecutor.Client(protocol);
    }

    @Test
    public void store() throws TException, IOException {
        APIResponse response = client.store(new ContractFile("ContractExecutorServiceTestCode.java", ByteBuffer.wrap(file.getBytes())), "987");
        System.out.println(response.getCode() + " " + response.getMessage());
    }

    @Test
    public void execute() throws TException, IOException {
        client.execute("987", "foo", null);
    }
}
