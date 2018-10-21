package com.credits.client.node.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.CreateTransactionData;
import com.credits.client.node.pojo.SmartContractInvocationData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.thrift.API;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.thrift.ThriftClientPool;
import com.credits.general.util.exception.ConverterException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.utils.ThreadUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.fail;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
@SuppressWarnings("SpellCheckingInspection")
public class NodeClientTestIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeClientTestIT.class);
    private static final String API_HOST = "localhost";
    private static final Integer API_PORT = 9090;
    private NodeThriftApiClient nodeApiClient = NodeThriftApiClient.getInstance(API_HOST, API_PORT);
    private NodeApiService nodeService = NodeApiServiceImpl.getInstance(API_HOST, API_PORT);

    @Test
    public void getBalanceTest() {
        String walletBASE58 = "8J7oZawJadpRFoMpQb42ffV3HN5zxtLjUJfNN8jpxUgp";
        BigDecimal balance = null;
        try {
            balance = nodeService.getBalance(walletBASE58);
        } catch (NodeClientException | ConverterException e) {
            e.printStackTrace();
        }
        LOGGER.info("Balance = {}", balance);
    }

    @Test
    @Ignore
    public void getBalanceTestInMultipleThreads() {
        ThriftClientPool<API.Client> pool = nodeApiClient.getPool();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    LOGGER.info("Balance = {}", nodeApiClient.getBalance("1_GJnW172n4CdN0".getBytes()));
                    LOGGER.info("Active client num in pool: {}, Idle client num in pool: {}", pool.getNumActive(), pool.getNumIdle());
                } catch (Throwable e) {
                    LOGGER.info(e.getMessage());
                }
            });
        }
        ThreadUtil.shutdownAndAwaitTermination(executorService, 1, MINUTES);
    }

    @Test
    public void getTransactionsTest() {
        List<TransactionData> transactionDataList = null;
        try {
            transactionDataList = nodeService.getTransactions("bxG+hrz1KuOLZHNpTz69Vij4vXAYIB26c25zFH7EJ6g=", 0, 10);
        } catch (NodeClientException | ConverterException e) {
            e.printStackTrace();
            assert false;
        }
        LOGGER.info("transactionDataList = {}", transactionDataList);
    }

    @Test
    public void getTransactionTest() throws NodeClientException {
        TransactionData transactionData = nodeService.getTransaction(new TransactionIdData("qwerty".getBytes(), 1));
        LOGGER.info("transactionData = {}", transactionData);
    }

    @Test
    public void transactionFlowTest() throws NodeClientException, ConverterException {
        CreateTransactionData createTransactionData =
            new CreateTransactionData(12327, "qwerty".getBytes(), "qwerty".getBytes(), new BigDecimal("0.123"), new BigDecimal("0.123"), (byte) 1,
                (short) 10, "signature".getBytes());
        ApiResponseData responseData = nodeService.createTransaction(createTransactionData, false);
        Assert.assertEquals(0, responseData.getCode());
    }

    @Test
    public void getSmartContractTest() throws NodeClientException, ConverterException {
        String walletBASE58 = "AoRKdBEbozwTKt5sirqx6ERv2DPsrvTk81hyztnndgWC";
        SmartContractData smartContractData;
        smartContractData = nodeService.getSmartContract(walletBASE58);
        LOGGER.info("Smart contract hashState = {}", smartContractData.getHashState());
    }

    @Test
    public void getSmartContractsTest() {
        String walletBASE58 = "8J7oZawJadpRFoMpQb42ffV3HN5zxtLjUJfNN8jpxUgp";
        List<SmartContractData> smartContractDataList = null;
        try {
            smartContractDataList = nodeService.getSmartContracts(walletBASE58);
        } catch (CreditsException e) {
            e.printStackTrace();
            fail();
        }
        smartContractDataList.forEach(smartContractData -> LOGGER.info("sourceCode = {}", smartContractData.getSourceCode()));
    }

    @Test
    public void deploySmartContractTest() throws NodeClientException, ConverterException {
        String address = "address";
        long transactionInnerId = 12327;
        String transactionSource = "4ESD7KpGzJCfDL8pZKhMfcfekqdoBdjSBUF5FiJdkBAC";
        String transactionTarget = "transactionTarget";
        SmartContractInvocationData smartContractInvocationData =
            new SmartContractInvocationData("sourceCode", address.getBytes(), "hashState", "method", null, true);

        nodeService.executeSmartContract(transactionInnerId, transactionSource, transactionTarget, smartContractInvocationData,
            "signature01".getBytes(), new TransactionProcessThread.Callback() {
                @Override
                public void onSuccess(ApiResponseData resultData) {
                }

                @Override
                public void onError(Exception e) {
                }
            });
    }
}
