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
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.client.node.util.NodeClientUtils.serializeByThrift;
import static com.credits.general.util.Converter.byteArrayToHex;
import static com.credits.general.util.Converter.decodeFromBASE58;
import static com.credits.general.util.Converter.encodeToBASE58;
import static com.credits.general.util.Converter.toBitSet;
import static com.credits.general.util.Converter.toByteArray;
import static com.credits.general.util.Converter.toByteArrayLittleEndian;
import static com.credits.general.util.Converter.toLong;
import static com.credits.wallet.desktop.AppState.*;
import static java.math.BigDecimal.*;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static byte[] accountBytesAddress;

    public static ApiResponseData createTransaction(String targetBase58, BigDecimal amount) throws NodeClientException, ConverterException {
        return nodeApiService.transactionFlow(getTransactionFlowData(targetBase58, amount, null));
    }

    public static ApiResponseData createSmartContractTransaction(SmartContractData smartContractData)
        throws NodeClientException, ConverterException {

        SmartContractInvocationData smartContractInvocationData =
            new SmartContractInvocationData(smartContractData.getSourceCode(), smartContractData.getByteCode(),
                smartContractData.getHashState(), smartContractData.getMethod(), smartContractData.getParams(), false);

        SmartContractTransactionFlowData scData =
            new SmartContractTransactionFlowData(
                    getTransactionFlowData(smartContractData.getBase58Address(), ZERO, serializeByThrift(smartContractInvocationData)),
                    smartContractInvocationData);

        return nodeApiService.smartContractTransactionFlow(scData);
    }

    private static TransactionFlowData getTransactionFlowData(String targetBase58, BigDecimal amount, byte[] smartContractBytes) {
        CalcTransactionIdSourceTargetResult transactionData = ApiUtils.calcTransactionIdSourceTarget(account, targetBase58);

        long id = transactionData.getTransactionId();
        byte[] source = transactionData.getByteSource();
        byte[] target = transactionData.getByteTarget();
        short offeredMaxFee = transactionOfferedMaxFeeValue;
        byte currency = 1;

        TransactionFlowData transactionFlowData = new TransactionFlowData(id, source, target, amount, offeredMaxFee, currency, smartContractBytes);
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

    private static long createTransactionId(boolean senderIndexExists, boolean receiverIndexExists, long transactionId)
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

    private static CalcTransactionIdSourceTargetResult calcTransactionIdSourceTarget(String sourceBase58,
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

    public static ApiResponseData deploySmartContractProcess(String javaCode, byte[] byteCode) throws NodeClientException, ConverterException {
        SmartContractData scData = new SmartContractData(generateAddress(), accountBytesAddress, javaCode, byteCode, "", null);
        return createSmartContractTransaction(scData);
    }

    private static byte[] generateAddress() {
        KeyPair keyPair = Ed25519.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        return Ed25519.publicKeyToBytes(publicKey);
    }

    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return byteArrayToHex(hashBytes);
    }

}