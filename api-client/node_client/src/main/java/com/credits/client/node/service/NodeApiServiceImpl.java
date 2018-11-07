package com.credits.client.node.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.SmartContractTransactionFlowData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.pojo.WalletData;
import com.credits.client.node.thrift.generated.Amount;
import com.credits.client.node.thrift.generated.Pool;
import com.credits.client.node.thrift.generated.PoolInfoGetResult;
import com.credits.client.node.thrift.generated.PoolListGetResult;
import com.credits.client.node.thrift.generated.SealedTransaction;
import com.credits.client.node.thrift.generated.SmartContract;
import com.credits.client.node.thrift.generated.SmartContractAddressesListGetResult;
import com.credits.client.node.thrift.generated.SmartContractGetResult;
import com.credits.client.node.thrift.generated.SmartContractsListGetResult;
import com.credits.client.node.thrift.generated.Transaction;
import com.credits.client.node.thrift.generated.TransactionFlowResult;
import com.credits.client.node.thrift.generated.TransactionGetResult;
import com.credits.client.node.thrift.generated.TransactionsGetResult;
import com.credits.client.node.thrift.generated.TransactionsStateGetResult;
import com.credits.client.node.thrift.generated.WalletBalanceGetResult;
import com.credits.client.node.thrift.generated.WalletDataGetResult;
import com.credits.client.node.thrift.generated.WalletIdGetResult;
import com.credits.client.node.thrift.generated.WalletTransactionsCountGetResult;
import com.credits.client.node.util.NodePojoConverter;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.general.util.Callback;
import com.credits.general.util.Function;
import com.credits.general.util.Utils;
import com.credits.general.util.exception.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.credits.client.node.util.NodeClientUtils.logApiResponse;
import static com.credits.client.node.util.NodeClientUtils.processApiResponse;
import static com.credits.client.node.util.NodePojoConverter.amountToBigDecimal;
import static com.credits.client.node.util.NodePojoConverter.poolToPoolData;
import static com.credits.client.node.util.NodePojoConverter.smartContractToSmartContractData;
import static com.credits.client.node.util.NodePojoConverter.smartContractTransactionFlowDataToTransaction;
import static com.credits.client.node.util.NodePojoConverter.transactionFlowDataToTransaction;
import static com.credits.client.node.util.NodePojoConverter.transactionFlowResultToApiResponseData;
import static com.credits.client.node.util.NodePojoConverter.transactionToTransactionData;
import static com.credits.client.node.util.NodePojoConverter.walletToWalletData;
import static com.credits.general.util.Converter.byteArrayToByteBuffer;
import static com.credits.general.util.Converter.decodeFromBASE58;
import static com.credits.general.util.Converter.encodeToBASE58;

public class NodeApiServiceImpl implements NodeApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeApiServiceImpl.class);
    public static String account;
    private static volatile NodeApiServiceImpl instance;
    private final NodeThriftApiClient nodeClient;

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
    public List<TransactionData> getTransactions(String address, long offset, long limit) throws NodeClientException, ConverterException {
        LOGGER.info(String.format("getTransactions: ---> address = %s, offset = %d, limit = %d", address, offset, limit));
        TransactionsGetResult result = nodeClient.getTransactions(decodeFromBASE58(address), offset, limit);
        processApiResponse(result.getStatus());
        List<TransactionData> transactionDataList = new ArrayList<>();
        for (SealedTransaction sealedTransaction : result.getTransactions()) {
            transactionDataList.add(transactionToTransactionData(sealedTransaction));
        }
        LOGGER.info(String.format("getTransactions: <--- address = %s, transactions count = %d", address, transactionDataList.size()));
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
            throw new NodeClientException(String.format("Pool by hash %s and index %s is not found", Arrays.toString(hash), index));
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
    public ApiResponseData smartContractTransactionFlow(SmartContractTransactionFlowData scTransaction) throws NodeClientException, ConverterException {
        //todo validation
        Transaction transaction = smartContractTransactionFlowDataToTransaction(scTransaction);
        LOGGER.debug("smartContractTransactionFlow -> {}", transaction);
        ApiResponseData response = callTransactionFlow(transaction);
        LOGGER.debug("smartContractTransactionFlow <- {}", response);
        return response;
    }

    @Override
    public ApiResponseData transactionFlow(TransactionFlowData transactionFlowData) {
        //todo validation
        Transaction transaction = transactionFlowDataToTransaction(transactionFlowData);
        LOGGER.debug("transaction flow -> {}", transactionFlowData);
        ApiResponseData response = callTransactionFlow(transaction);
        LOGGER.debug("transaction flow <- {}", response);
        return response;
    }

    private ApiResponseData callTransactionFlow(Transaction transaction) {
        TransactionFlowResult transactionFlowResult = nodeClient.transactionFlow(transaction);
        ApiResponseData response = transactionFlowResultToApiResponseData(transactionFlowResult);
        transactionMapSetRoundNumber(transaction, transactionFlowResult);
        response.setSource(encodeToBASE58(transaction.getSource()));
        response.setTarget(encodeToBASE58(transaction.getTarget()));
        return response;
    }

    private void transactionMapSetRoundNumber(Transaction transaction, TransactionFlowResult transactionFlowResult) {
        ConcurrentHashMap<Long, TransactionRoundData> tempTransactionsData =
            Utils.sourceMap.get(NodeApiServiceImpl.account);
        TransactionRoundData transactionRoundData =
            tempTransactionsData.get(NodePojoConverter.getShortTransactionId(transaction.getId()));
        transactionRoundData.setRoundNumber(transactionFlowResult.getRoundNum());
    }

    @Override
    public SmartContractData getSmartContract(String address) throws NodeClientException, ConverterException {
        LOGGER.info(String.format("---> address = %s", address));
        SmartContractGetResult result = nodeClient.getSmartContract(decodeFromBASE58(address));
        logApiResponse(result.getStatus());
        processApiResponse(result.getStatus());
        SmartContract smartContract = result.getSmartContract();
        SmartContractData smartContractData = smartContractToSmartContractData(smartContract);
        LOGGER.info(String.format("<--- smart contract hashState = %s", smartContractData.getHashState()));
        return smartContractData;
    }

    @Override
    public List<SmartContractData> getSmartContracts(String address) throws NodeClientException, ConverterException {
        LOGGER.info(String.format("---> wallet address = %s", address));
        SmartContractsListGetResult result = nodeClient.getSmartContracts(decodeFromBASE58(address));
        logApiResponse(result.getStatus());
        processApiResponse(result.getStatus());
        LOGGER.info("<--- smart contracts size = {}", result.getSmartContractsList().size());
        return result.getSmartContractsList().stream().map(NodePojoConverter::smartContractToSmartContractData).collect(Collectors.toList());
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
        LOGGER.debug("<---  get wallet id {}",result.getWalletId());
        return result.getWalletId();
    }

    @Override
    public Long getWalletTransactionsCount(String address) throws NodeClientException, ConverterException {
        WalletTransactionsCountGetResult result = nodeClient.getWalletTransactionsCount(decodeFromBASE58(address));
        processApiResponse(result.getStatus());
        return result.getLastTransactionInnerId();
    }

    @Override
    //todo add pojo TransactionsStateGetResult
    public TransactionsStateGetResult getTransactionsState(String address, List<Long> transactionIdList) throws NodeClientException, ConverterException {
        TransactionsStateGetResult result = nodeClient.getTransactionsState(decodeFromBASE58(address), transactionIdList);
        processApiResponse(result.getStatus());
        return result;
    }

    public static <R> void async(Function<R> apiCall, Callback<R> callback) {
        CompletableFuture
            .supplyAsync(apiCall::apply)
            .whenComplete((result, error) -> {
            if (error == null)
                callback.onSuccess(result);
            else{
                LOGGER.error(error.getLocalizedMessage());
                callback.onError(error);
            }
        });
    }

}
