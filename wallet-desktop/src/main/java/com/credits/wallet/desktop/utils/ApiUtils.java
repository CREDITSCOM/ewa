package com.credits.wallet.desktop.utils;

import com.credits.client.node.crypto.Ed25519;
import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.SmartContractInvocationData;
import com.credits.client.node.pojo.SmartContractTransactionFlowData;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.pojo.TransactionRoundData;
import com.credits.client.node.util.NodePojoConverter;
import com.credits.general.crypto.Md5;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.util.Callback;
import com.credits.general.util.exception.ConverterException;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.struct.CalcTransactionIdSourceTargetResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.client.node.service.NodeApiServiceImpl.async;
import static com.credits.client.node.util.NodeClientUtils.serializeByThrift;
import static com.credits.general.util.Converter.byteArrayToHex;
import static com.credits.general.util.Converter.decodeFromBASE58;
import static com.credits.general.util.Converter.encodeToBASE58;
import static com.credits.general.util.Converter.toBitSet;
import static com.credits.general.util.Converter.toByteArray;
import static com.credits.general.util.Converter.toByteArrayLittleEndian;
import static com.credits.general.util.Converter.toLong;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.AppState.toAddress;
import static com.credits.wallet.desktop.AppState.transactionOfferedMaxFeeValue;
import static com.credits.wallet.desktop.AppState.walletLastTransactionIdCache;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    //todo need refactoring
    public static void callCreateTransaction(Callback<ApiResponseData> callback)
        throws NodeClientException, ConverterException {

        String sourceBase58 = account;
        String targetBase58 = toAddress;

        CalcTransactionIdSourceTargetResult transactionData =
            ApiUtils.calcTransactionIdSourceTarget(sourceBase58, targetBase58);

        long id = transactionData.getTransactionId();
        byte[] source = transactionData.getByteSource();
        byte[] target = transactionData.getByteTarget();
        BigDecimal amount = AppState.amount;
        short offeredMaxFee = transactionOfferedMaxFeeValue;
        byte currency = 1;
        byte[] smartContractBytes = null;

        TransactionFlowData transactionFlowData =
            getTransactionFlowData(id, source, target, amount, offeredMaxFee, currency, smartContractBytes);

        async(() -> nodeApiService.transactionFlow(transactionFlowData), callback);
    }

    public static void executeSmartContractProcess(String method, List<Object> params,
        SmartContractData smartContractData, Callback<ApiResponseData> callback)
        throws NodeClientException, ConverterException {

        SmartContractInvocationData smartContractInvocationData =
            new SmartContractInvocationData(smartContractData.getSourceCode(), smartContractData.getByteCode(),
                smartContractData.getHashState(), method, params, false);


        String sourceBase58 = account;
        String targetBase58 = encodeToBASE58(smartContractData.getAddress());
        CalcTransactionIdSourceTargetResult transactionData = calcTransactionIdSourceTarget(sourceBase58, targetBase58);

        long id = transactionData.getTransactionId();
        byte[] source = transactionData.getByteSource();
        byte[] target = transactionData.getByteTarget();
        BigDecimal amount = new BigDecimal(0);
        short offeredMaxFee = 0;
        byte currency = 0x01;
        byte[] smartContractBytes = serializeByThrift(smartContractInvocationData);

        TransactionFlowData transactionFlowData =
            getTransactionFlowData(id, source, target, amount, offeredMaxFee, currency, smartContractBytes);


        SmartContractTransactionFlowData scData =
            new SmartContractTransactionFlowData(transactionFlowData, smartContractInvocationData);

        async(() -> nodeApiService.smartContractTransactionFlow(scData), callback);
    }

    private static TransactionFlowData getTransactionFlowData(long id, byte[] source, byte[] target, BigDecimal amount,
        short offeredMaxFee, byte currency, byte[] smartContractBytes) {
        TransactionFlowData transactionFlowData =
            new TransactionFlowData(id, source, target, amount, offeredMaxFee, currency, smartContractBytes);
        SignUtils.signTransaction(transactionFlowData);
        saveTransactionIntoMap(id, transactionFlowData);
        return transactionFlowData;
    }

    private static void saveTransactionIntoMap(long id, TransactionFlowData transactionFlowData) {
        AppState.sourceMap.computeIfAbsent(AppState.account, key -> new ConcurrentHashMap<>());
        Map<Long, TransactionRoundData> sourceMap = AppState.sourceMap.get(AppState.account);
        TransactionRoundData transactionRoundData = new TransactionRoundData(transactionFlowData);
        long shortTransactionId = NodePojoConverter.getShortTransactionId(id);
        sourceMap.put(shortTransactionId, transactionRoundData);
    }

    public static long createTransactionId(boolean senderIndexExists, boolean receiverIndexExists, long transactionId)
        throws ConverterException {

        byte[] transactionIdBytes = toByteArray(transactionId);
        BitSet transactionIdBitSet = toBitSet(transactionIdBytes);
        for (int i = 63; i > 45; i--) {
            transactionIdBitSet.set(i, false);
        }
        transactionIdBitSet.set(47, senderIndexExists);
        transactionIdBitSet.set(46, receiverIndexExists);
        return toLong(transactionIdBitSet);
    }

    public static CalcTransactionIdSourceTargetResult calcTransactionIdSourceTarget(String sourceBase58,
        String targetBase58) throws NodeClientException, ConverterException {

        // get transactions count from Node and increment it
        Long transactionId = nodeApiService.getWalletTransactionsCount(sourceBase58) + 1;
        // get last transaction id from cache
        Long walletLastTransactionIdInCache = walletLastTransactionIdCache.get(sourceBase58);

        if (walletLastTransactionIdInCache != null && transactionId < walletLastTransactionIdInCache) {
            transactionId = walletLastTransactionIdInCache + 1;
        }
        walletLastTransactionIdCache.put(sourceBase58, transactionId);

        boolean sourceIndexExists = false;
        boolean targetIndexExists = false;

        Integer sourceWalletId = nodeApiService.getWalletId(sourceBase58);
        if (sourceWalletId != 0) {
            sourceIndexExists = true;
            sourceBase58 = encodeToBASE58(toByteArrayLittleEndian(sourceWalletId, 4));
        }
        Integer targetWalletId = nodeApiService.getWalletId(targetBase58);
        if (targetWalletId != 0) {
            targetIndexExists = true;
            targetBase58 = encodeToBASE58(toByteArrayLittleEndian(targetWalletId, 4));
        }

        return new CalcTransactionIdSourceTargetResult(
            ApiUtils.createTransactionId(sourceIndexExists, targetIndexExists, transactionId), sourceBase58,
            targetBase58);
    }

    //todo need refactoring
    public static void deploySmartContractProcess(String javaCode, byte[] byteCode, Callback<ApiResponseData> callback)
        throws NodeClientException, ConverterException {
        SmartContractInvocationData smartContractInvocationData =
            new SmartContractInvocationData(javaCode, byteCode, "", "", new ArrayList<>(), false);

        String transactionTarget = generatePublicKeyBase58();
        LOGGER.info("transactionTarget = {}", transactionTarget);

        LOGGER.debug("SmartContractData structure ^^^^^");
        LOGGER.debug("sourceCode = " + smartContractInvocationData.getSourceCode());
        if (smartContractInvocationData.getByteCode() != null) {
            LOGGER.debug("byteCode.length = " + smartContractInvocationData.getByteCode().length);
        } else {
            LOGGER.debug("byteCode.length = 0");
        }
        LOGGER.debug("hashState = " + smartContractInvocationData.getHashState());
        LOGGER.debug("method = " + smartContractInvocationData.getMethod());
        if (smartContractInvocationData.getParams() != null) {
            LOGGER.debug("params.length = " + smartContractInvocationData.getParams().size());
            for (int i = 0; i < smartContractInvocationData.getParams().size(); i++) {
                LOGGER.debug("params." + i + " = " + smartContractInvocationData.getParams().get(i));
            }
        } else {
            LOGGER.debug("params.length = 0");
        }
        LOGGER.debug("SmartContractData structure vvvvv");

        SmartContractData scData =
            new SmartContractData(decodeFromBASE58(transactionTarget), decodeFromBASE58(account), javaCode, byteCode,
                "", null); //todo unused hashState
        executeSmartContractProcess("", new ArrayList<>(), scData, callback);
    }

    private static String generatePublicKeyBase58() {
        KeyPair keyPair = Ed25519.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        return encodeToBASE58(Ed25519.publicKeyToBytes(publicKey));
    }

    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return byteArrayToHex(hashBytes);
    }

}