package com.credits.client.node.integration;

import com.credits.client.node.pojo.SmartContractTransactionFlowData;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.client.node.util.ObjectKeeper;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.credits.client.node.service.NodeApiServiceImpl.account;
import static java.io.File.separator;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class NodeIntegrationTest {
    private static Logger LOGGER = LoggerFactory.getLogger(NodeIntegrationTest.class);

    NodeApiServiceImpl mockNodeApiService;
    String path;

    @Before
    public void setUp() {
        mockNodeApiService = NodeApiServiceImpl.getInstance("127.0.0.1", 9090);
        account = "5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe";
        path = File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." +
            File.separator + "cache" + File.separator + "test" + File.separator + account + File.separator;
    }

/*
    @Test
    public void checkEmptyNode() {
        assertEquals(mockNodeApiService.getTransactions(account,0,100).size(),0);
    }
*/

    @Test
    public void sendTransactionTest() throws InterruptedException, IOException {
        String directory = System.getProperty("user.dir") + separator + "cache" + separator + account + path;
        File dir = new File(directory);
        File[] files = dir.listFiles((dir1, name) -> name.startsWith("transaction") && name.endsWith(".ser"));
        assertNotEquals(files.length,null);
        for (File f:files) {
            ObjectKeeper objectKeeper = new ObjectKeeper<>(account, path + f.getName());
            Object[] o = (Object[]) objectKeeper.getKeptObject().get();
            TransactionFlowData transactionData = (TransactionFlowData) o[0];
            ApiResponseData apiResponseData = mockNodeApiService.transactionFlow(transactionData);
            assertEquals(apiResponseData.getCode(), ApiResponseCode.SUCCESS);
        }
        sleep(30000);
        assertEquals(mockNodeApiService.getTransactions(account,0,100).size(),2);

    }

    @Test
    public void deploySmartContractTest() throws InterruptedException {
        ObjectKeeper objectKeeper = new ObjectKeeper<>(account, path + "smartTransaction0" + ".ser");
        Object[] o = (Object[]) objectKeeper.getKeptObject().get();
        SmartContractTransactionFlowData transactionData = (SmartContractTransactionFlowData) o[0];
        ApiResponseData apiResponseData = mockNodeApiService.smartContractTransactionFlow(transactionData);
        assertEquals(apiResponseData.getCode(), ApiResponseCode.SUCCESS);
        sleep(30000);
        assertEquals(mockNodeApiService.getTransactions(account,0,100).size(),3);
    }
}
