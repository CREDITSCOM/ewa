package com.credits.wallet.desktop.test;

import com.credits.common.utils.Converter;
import com.credits.wallet.desktop.test.init.TestState;
import com.credits.wallet.desktop.thrift.executor.APIResponse;
import com.credits.wallet.desktop.thrift.executor.ContractExecutor;
import com.credits.wallet.desktop.utils.SimpleInMemoryCompiler;
import com.credits.wallet.desktop.utils.SourceCodeUtils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SmartContractControllerTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(SmartContractControllerTest.class);

    private String sourceCode = "public class Contract extends SmartContract \n" +
            "{ \n" +
            "\tpublic Contract() {\n" +
            "\t\tSystem.out.println(\"Constructor\"); \n" +
            "\t} \n" +
            "\tpublic void initialize() { } \n" +
            "\tpublic void balanceGet() throws Exception { \n" +
            "\t\tSystem.out.println(\"getBalance()\"); \n" +
            //"\t\tjava.math.BigDecimal balance = getBalance(\"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", \"CS\"); \n" +
            //"\t\tSystem.out.println(\"getBalance = \" + balance); \n" +
            "\t} \n" +
            "\tpublic void sendZeroCS() throws Exception {\n" +
            "\t\tSystem.out.println(\"try to send 0 credits...\"); sendTransaction(\"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", \"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", 0, \"CS\"); System.out.println(\"success\"); \n" +
            "\t} \n" +
            "}";

    private String sourceCode02 =  "public class Contract extends SmartContract { public Contract() { System.out.println(\"Constructor\"); } public void initialize() { } public void balanceGet() throws Exception { System.out.println(\"getBalance()\"); java.math.BigDecimal balance = getBalance(\"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", \"CS\"); System.out.println(\"getBalance = \" + balance); } public void sendZeroCS() throws Exception { System.out.println(\"try to send 0 credits...\"); sendTransaction(\"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", \"1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2\", 0, \"CS\"); System.out.println(\"success\"); } }";

    @Before
    public void init() {
        TestState.init();
    }

    @Test
    public void handleExecuteTest() {



        // Call contract executor
        if (TestState.contractExecutorHost != null &&
                TestState.contractExecutorPort != null &&
                TestState.contractExecutorDir != null) {
            try {
                TTransport transport;

                transport = new TSocket(TestState.contractExecutorHost, TestState.contractExecutorPort);
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                ContractExecutor.Client client = new ContractExecutor.Client(protocol);

                String token = SourceCodeUtils.generateSmartContractToken();
//                String javaCode = SourceCodeUtils.normalizeSourceCode(this.sourceCode);
                String javaCode = this.sourceCode02;
                byte[] byteCode = SimpleInMemoryCompiler.compile(javaCode, "Contract", token);

                String address = "BoRKdBEbozwTKt5sirqx6ERv2DPsrvTk81hyztnndgWC";
                String method = "balanceGet";
                List<String> params = new ArrayList<>();
//                params.add("par01");
//                params.add("par02");

                LOGGER.info("Contract executor request: address = {}; method = {}; params = {}", address, method, params.toArray());

                APIResponse apiResponse = client.executeByteCode(address, Converter.bytesToByteBuffer(byteCode), method, params);

                LOGGER.info("Contract executor response: code = {}; message = {}", apiResponse.getCode(), apiResponse.getMessage());

                transport.close();
            } catch (Exception e) {
                e.printStackTrace();
                assert false;
            }
        }
    }
}
