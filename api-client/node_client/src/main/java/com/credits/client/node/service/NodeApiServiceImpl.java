package com.credits.client.node.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.*;
import com.credits.client.node.pojo.WalletData;
import com.credits.client.node.thrift.generated.*;
import com.credits.client.node.util.NodePojoConverter;
import com.credits.client.node.util.Validator;
import com.credits.general.util.Callback;
import com.credits.general.util.Function;
import com.credits.general.util.exception.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.credits.client.node.util.NodeClientUtils.logApiResponse;
import static com.credits.client.node.util.NodeClientUtils.processApiResponse;
import static com.credits.client.node.util.NodePojoConverter.*;
import static com.credits.general.util.GeneralConverter.byteArrayToByteBuffer;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.Utils.threadPool;

public class NodeApiServiceImpl implements NodeApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeApiServiceImpl.class);
    public static String account;
    private static volatile NodeApiServiceImpl instance;
    public NodeThriftApiClient nodeClient;

    private NodeApiServiceImpl(String host, int port) {
        nodeClient = NodeThriftApiClient.getInstance(host, port);
    }

    public static NodeApiServiceImpl getInstance(String host, int port) {
        NodeApiServiceImpl localInstance = NodeApiServiceImpl.instance;
        if (localInstance == null) {
            synchronized (NodeApiServiceImpl.class) {
                localInstance = NodeApiServiceImpl.instance;
                if (localInstance == null) {
                    NodeApiServiceImpl.instance = localInstance = new NodeApiServiceImpl(host, port);
                }
            }
        }
        return localInstance;
    }

    @Override
    public BigDecimal getBalance(String address) throws NodeClientException, ConverterException {
        LOGGER.info(String.format("getBalance: ---> address = %s", address));
        WalletBalanceGetResult result = nodeClient.getBalance(decodeFromBASE58(address));
        processApiResponse(result.getStatus());
        Amount amount = result.getBalance();
        BigDecimal balance = amountToBigDecimal(amount);
        LOGGER.info(String.format("getBalance: <--- balance = %s", balance));
        return balance;
    }

    @Override
    public List<TransactionData> getTransactions(String address, long offset, long limit)
        throws NodeClientException, ConverterException {
        LOGGER.info(
            String.format("getTransactions: ---> address = %s, offset = %d, limit = %d", address, offset, limit));
        TransactionsGetResult result = nodeClient.getTransactions(decodeFromBASE58(address), offset, limit);
        processApiResponse(result.getStatus());
        List<TransactionData> transactionDataList = new ArrayList<>();
        for (SealedTransaction sealedTransaction : result.getTransactions()) {
            transactionDataList.add(transactionToTransactionData(sealedTransaction));
        }
        LOGGER.info(String.format("getTransactions: <--- address = %s, transactions count = %d", address,
            transactionDataList.size()));
        return transactionDataList;
    }

    @Override
    public TransactionData getTransaction(TransactionIdData transactionIdData) throws NodeClientException {
        LOGGER.info(String.format("getTransaction: ---> transIndex = %s", transactionIdData.getIndex()));
        TransactionGetResult result = nodeClient.getTransaction(transactionIdData);
        processApiResponse(result.getStatus());
        LOGGER.info(String.format("getTransaction: <--- transIndex = %s", transactionIdData.getIndex()));
        return transactionToTransactionData(result.getTransaction());
    }

    @Override
    public PoolData getPoolInfo(byte[] hash, long index) throws NodeClientException {
        LOGGER.info(String.format("getPoolInfo: ---> index = %d", index));
        ByteBuffer hashByteBuffer = byteArrayToByteBuffer(hash);

        PoolInfoGetResult result = nodeClient.getPoolInfo(hashByteBuffer, index);
        processApiResponse(result.getStatus());

        if (!result.isIsFound()) {
            throw new NodeClientException(
                String.format("Pool by hash %s and index %s is not found", Arrays.toString(hash), index));
        }
        PoolData poolData = poolToPoolData(result.getPool());
        LOGGER.info(String.format("getPoolInfo: <--- index = %d", index));
        return poolData;
    }

    @Override
    public List<PoolData> getPoolList(Long offset, Long limit) throws NodeClientException {
        LOGGER.info(String.format("getPoolList: ---> offset = %d,limit=%d", offset, limit));
        PoolListGetResult result = nodeClient.getPoolList(offset, limit);
        processApiResponse(result.getStatus());
        List<PoolData> poolDataList = new ArrayList<>();
        for (Pool pool : result.getPools()) {
            poolDataList.add(poolToPoolData(pool));
        }
        LOGGER.info(String.format("getPoolList: <--- poolListSize = %d", poolDataList.size()));
        return poolDataList;
    }

    @Override
    public TransactionFlowResultData smartContractTransactionFlow(SmartContractTransactionFlowData scTransaction)
        throws NodeClientException, ConverterException {
        Validator.validate(scTransaction);
        Transaction transaction = smartContractTransactionFlowDataToTransaction(scTransaction);
        LOGGER.debug("smartContractTransactionFlow -> {}", transaction);
        TransactionFlowResultData response = callTransactionFlow(transaction);
        LOGGER.debug("smartContractTransactionFlow <- {}", response);
        return response;
    }

    @Override
    public TransactionFlowResultData transactionFlow(TransactionFlowData transactionFlowData) {
        Validator.validate(transactionFlowData);
        Transaction transaction = transactionFlowDataToTransaction(transactionFlowData);
        LOGGER.debug("transaction flow -> {}", transactionFlowData);
        TransactionFlowResultData response = callTransactionFlow(transaction);
        LOGGER.debug("transaction flow <- {}", response);
        return response;
    }

    private TransactionFlowResultData callTransactionFlow(Transaction transaction) {
        TransactionFlowResult result = nodeClient.transactionFlow(transaction);
        logApiResponse(result.getStatus());
        processApiResponse(result.getStatus());
        return transactionFlowResultToTransactionFlowResultData(result, transaction.getSource(), transaction.getTarget());
    }


    @Override
    public SmartContractData getSmartContract(String address) throws NodeClientException, ConverterException {
        LOGGER.info(String.format("---> address = %s", address));
        SmartContractGetResult result = nodeClient.getSmartContract(decodeFromBASE58(address));
        logApiResponse(result.getStatus());
        processApiResponse(result.getStatus());
        SmartContract smartContract = result.getSmartContract();
        SmartContractData smartContractData = smartContractToSmartContractData(smartContract);
        LOGGER.info(String.format("<--- smart contract hashState = %s", smartContractData.getSmartContractDeployData().getHashState()));
        return smartContractData;
    }

    @Override
    public List<SmartContractData> getSmartContracts(String address) throws NodeClientException, ConverterException {
        LOGGER.info(String.format("---> wallet address = %s", address));
        SmartContractsListGetResult result = nodeClient.getSmartContracts(decodeFromBASE58(address));
        logApiResponse(result.getStatus());
        processApiResponse(result.getStatus());
        LOGGER.info("<--- smart contracts size = {}", result.getSmartContractsList().size());
        return result.getSmartContractsList()
            .stream()
            .map(NodePojoConverter::smartContractToSmartContractData)
            .collect(Collectors.toList());
    }

    @Override
    public List<ByteBuffer> getSmartContractAddresses(String address) throws NodeClientException, ConverterException {
        SmartContractAddressesListGetResult result = nodeClient.getSmartContractAddresses(decodeFromBASE58(address));
        logApiResponse(result.getStatus());
        processApiResponse(result.getStatus());
        return result.getAddressesList();
    }

    @Override
    public WalletData getWalletData(String address) throws NodeClientException, ConverterException {
        WalletDataGetResult result = nodeClient.getWalletData(decodeFromBASE58(address));
        processApiResponse(result.getStatus());
        return walletToWalletData(result.getWalletData());
    }

    @Override
    public Integer getWalletId(String address) throws NodeClientException, ConverterException {
        WalletIdGetResult result = nodeClient.getWalletId(decodeFromBASE58(address));
        processApiResponse(result.getStatus());
        LOGGER.debug("<---  get wallet id {}", result.getWalletId());
        return result.getWalletId();
    }

    @Override
    public Long getWalletTransactionsCount(String address) throws NodeClientException, ConverterException {
        WalletTransactionsCountGetResult result = nodeClient.getWalletTransactionsCount(decodeFromBASE58(address));
        processApiResponse(result.getStatus());
        return result.getLastTransactionInnerId();
    }

    @Override
    public TransactionsStateGetResultData getTransactionsState(String address, List<Long> transactionIdList)
        throws NodeClientException, ConverterException {
        TransactionsStateGetResult transactionsStateGetResult =
            nodeClient.getTransactionsState(decodeFromBASE58(address), transactionIdList);
        processApiResponse(transactionsStateGetResult.getStatus());
        return NodePojoConverter.createTransactionsStateGetResultData(transactionsStateGetResult);
    }

    public static <R> void async(Function<R> apiCall, Callback<R> callback) {
        CompletableFuture.supplyAsync(apiCall::apply,threadPool).whenComplete(handleCallback(callback));
    }

    public static <R> BiConsumer<R, Throwable> handleCallback(Callback<R> callback) {
        return (result, error) -> {
            if (error == null) {
                callback.onSuccess(result);
            } else {
                LOGGER.error(error.getLocalizedMessage());
                callback.onError(error);
            }
        };
    }

}
