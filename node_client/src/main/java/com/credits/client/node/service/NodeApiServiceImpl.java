package com.credits.client.node.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.CreateTransactionData;
import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.SmartContractInvocationData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.pojo.TransactionRoundData;
import com.credits.client.node.pojo.WalletData;
import com.credits.client.node.thrift.Amount;
import com.credits.client.node.thrift.AmountCommission;
import com.credits.client.node.thrift.Pool;
import com.credits.client.node.thrift.PoolInfoGetResult;
import com.credits.client.node.thrift.PoolListGetResult;
import com.credits.client.node.thrift.SealedTransaction;
import com.credits.client.node.thrift.SmartContract;
import com.credits.client.node.thrift.SmartContractAddressesListGetResult;
import com.credits.client.node.thrift.SmartContractGetResult;
import com.credits.client.node.thrift.SmartContractInvocation;
import com.credits.client.node.thrift.SmartContractsListGetResult;
import com.credits.client.node.thrift.Transaction;
import com.credits.client.node.thrift.TransactionFlowResult;
import com.credits.client.node.thrift.TransactionGetResult;
import com.credits.client.node.thrift.TransactionsGetResult;
import com.credits.client.node.thrift.TransactionsStateGetResult;
import com.credits.client.node.thrift.WalletBalanceGetResult;
import com.credits.client.node.thrift.WalletDataGetResult;
import com.credits.client.node.thrift.WalletIdGetResult;
import com.credits.client.node.thrift.WalletTransactionsCountGetResult;
import com.credits.client.node.util.NodePojoConverter;
import com.credits.client.node.util.Validator;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.util.exception.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.credits.client.node.util.NodeClientUtils.logApiResponse;
import static com.credits.client.node.util.NodeClientUtils.processApiResponse;
import static com.credits.client.node.util.NodePojoConverter.amountToBigDecimal;
import static com.credits.client.node.util.NodePojoConverter.apiResponseToApiResponseData;
import static com.credits.client.node.util.NodePojoConverter.bigDecimalToAmount;
import static com.credits.client.node.util.NodePojoConverter.poolToPoolData;
import static com.credits.client.node.util.NodePojoConverter.smartContractInvocationDataToSmartContractInvocation;
import static com.credits.client.node.util.NodePojoConverter.smartContractToSmartContractData;
import static com.credits.client.node.util.NodePojoConverter.transactionToTransactionData;
import static com.credits.client.node.util.NodePojoConverter.walletToWalletData;
import static com.credits.general.util.Converter.byteArrayToByteBuffer;
import static com.credits.general.util.Converter.decodeFromBASE58;

public class NodeApiServiceImpl implements NodeApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeApiServiceImpl.class);
    public static ConcurrentHashMap<String, ConcurrentHashMap<Long, TransactionRoundData>> sourceMap = new ConcurrentHashMap<>();
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


    /**
     * Деплой и исполнение смарт-контракта
     */
    @Override
    public void executeSmartContract(long transactionInnerId, String source, String target, SmartContractInvocationData smartContractInvocationData, byte[] signature,
        TransactionProcessThread.Callback callback) throws NodeClientException, ConverterException {

        if (smartContractInvocationData == null) {
            throw new NodeClientException("Empty smart-contract");
        }

        SmartContractInvocation smartContractInvocation = smartContractInvocationDataToSmartContractInvocation(smartContractInvocationData);

        Transaction transaction =
            new Transaction(transactionInnerId, ByteBuffer.wrap(decodeFromBASE58(source)), ByteBuffer.wrap(decodeFromBASE58(target)), null, null, (byte) 1,
                ByteBuffer.wrap(signature), null);
        transaction.setSmartContract(smartContractInvocation);
        transaction.setSmartContractIsSet(true);

        LOGGER.info("---> transactionInnerId = {}; transactionSource = {}; transactionTarget = {}; smartContract.hashState = {}; " +
                "smartContract.method = {}; smartContract.params = {};", transactionInnerId, source, target, smartContractInvocationData.getHashState(),
            smartContractInvocationData.getMethod(), smartContractInvocationData.getParams() == null ? "" : smartContractInvocationData.getParams().toArray());

        executeAsyncTransactionFlow(callback, transaction);
    }

    private void executeAsyncTransactionFlow(TransactionProcessThread.Callback callback, Transaction transaction) {
        nodeClient.executeAsyncTransactionFlow(transaction, callback);
    }

    /**
     * Генерация транзакции
     */
    @Override
    public ApiResponseData createTransaction(CreateTransactionData createTransactionData, boolean checkBalance) throws NodeClientException, ConverterException {
        Transaction transaction = createTransactionProcess(createTransactionData, checkBalance);
        TransactionFlowResult result = nodeClient.executeSyncTransactionFlow(transaction);
        return apiResponseToApiResponseData(result.getStatus());
    }

    @Override
    public void asyncCreateTransaction(CreateTransactionData createTransactionData, boolean checkBalance, TransactionProcessThread.Callback callback)
        throws NodeClientException, ConverterException {
        Transaction transaction = createTransactionProcess(createTransactionData, checkBalance);
        executeAsyncTransactionFlow(callback, transaction);
    }

    private Transaction createTransactionProcess(CreateTransactionData createTransactionData, boolean checkBalance) throws NodeClientException, ConverterException {
        Validator.validateCreateTransactionData(createTransactionData);

        long innerId = createTransactionData.getInnerId();
        byte[] source = createTransactionData.getSource();
        byte[] target = createTransactionData.getTarget();
        BigDecimal amount = createTransactionData.getAmount();
        BigDecimal balance = createTransactionData.getBalance();
        byte currency = createTransactionData.getCurrency();
        byte[] signature = createTransactionData.getSignature();
        Short offeredMaxFee = createTransactionData.getOfferedMaxFee();

//        if (checkBalance) {
//            /*
//            TODO Необходимо добавить блокировку кошелька.
//            В многопоточной среде баланс может вернуться неактуальный (измененный созданием транзакции в параллельном потоке)
//             */
//            BigDecimal balanceFromNode = getBalance(encodeToBASE58(source));
//
//            if (balanceFromNode.compareTo(amount) < 0) {
//                throw new NodeClientException(
//                    String.format("Wallet %s with balance [%s] is less than transaction amount [%s]", encodeToBASE58(source), Converter.toString(balanceFromNode),
//                        Converter.toString(amount)));
//            }
//        }


        Amount serverAmount = bigDecimalToAmount(amount);
        Amount serverBalance = bigDecimalToAmount(balance);
        AmountCommission serverFee = new AmountCommission(offeredMaxFee);


        //currency = String.format("%s|%s", currency, signature);

        LOGGER.info(
            String.format("---> account = %s; target = %s; amount = %s; balance = %s; currency = %s; signature = %s; innerId = %s",
                Arrays.toString(source),
                Arrays.toString(target),
                serverAmount,
                serverBalance,
                currency,
                Arrays.toString(signature),
                innerId));


        return new Transaction(innerId, ByteBuffer.wrap(source), ByteBuffer.wrap(target), serverAmount, serverBalance, currency, ByteBuffer.wrap(signature), serverFee);

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
        return result.getWalletId();
    }

    @Override
    public Long getWalletTransactionsCount(String address) throws NodeClientException, ConverterException {
        WalletTransactionsCountGetResult result = nodeClient.getWalletTransactionsCount(decodeFromBASE58(address));
        processApiResponse(result.getStatus());
        return result.getLastTransactionInnerId();
    }

    @Override
    public TransactionsStateGetResult getTransactionsState(String address, List<Long> transactionIdList) throws NodeClientException, ConverterException {
        TransactionsStateGetResult result = nodeClient.getTransactionsState(decodeFromBASE58(address), transactionIdList);
        processApiResponse(result.getStatus());
        return result;
    }

}
