package com.credits.wallet.desktop.utils;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.*;
import com.credits.client.node.util.NodePojoConverter;
import com.credits.client.node.util.SignUtils;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.general.util.exception.ConverterException;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Session;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.client.node.util.NodeClientUtils.serializeByThrift;
import static com.credits.client.node.util.TransactionIdCalculateUtils.CalcTransactionIdSourceTargetResult;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.generateSmartContractAddress;
import static java.math.BigDecimal.ZERO;


public class ApiUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);


    public static Pair<Long, TransactionFlowResultData> createTransaction(
        CalcTransactionIdSourceTargetResult transactionData, BigDecimal amount, short offeredMaxFee16Bits, String text, Session session)
        throws NodeClientException, ConverterException {
        return Pair.of(
            transactionData.getTransactionId(),
            AppState.getNodeApiService().transactionFlow(getTransactionFlowData(transactionData, amount, offeredMaxFee16Bits, null, text, session)));
    }

    public static Pair<Long, TransactionFlowResultData> createSmartContractTransaction(
        CalcTransactionIdSourceTargetResult transactionData,
        short offeredMaxFee,
        SmartContractData smartContractData,
        List<ByteBuffer> usedSmartContracts,
        Session session)
        throws NodeClientException, ConverterException {

        smartContractData.setAddress(
            generateSmartContractAddress(transactionData.getByteSource(), transactionData.getTransactionId(),
                                         smartContractData.getSmartContractDeployData().getByteCodeObjects()));

        SmartContractInvocationData smartContractInvocationData =
            new SmartContractInvocationData(smartContractData.getSmartContractDeployData(),
                                            smartContractData.getMethod(),
                                            smartContractData.getParams(),
                                            usedSmartContracts,
                                            smartContractData.isGetterMethod());

        SmartContractTransactionFlowData scData = new SmartContractTransactionFlowData(
            getTransactionFlowData(transactionData, ZERO, offeredMaxFee, serializeByThrift(smartContractInvocationData), null,
                                   session), smartContractInvocationData);

        return Pair.of(transactionData.getTransactionId(), AppState.getNodeApiService().smartContractTransactionFlow(scData));
    }

    private static TransactionFlowData getTransactionFlowData(
        CalcTransactionIdSourceTargetResult transactionData,
        BigDecimal amount, short offeredMaxFee16Bits, byte[] smartContractBytes, String text, Session session) {
        long id = transactionData.getTransactionId();
        byte[] source = transactionData.getByteSource();
        byte[] target = transactionData.getByteTarget();
        byte currency = 1;
        byte[] textBytes = null;
        if (text != null) {
            textBytes = text.getBytes(StandardCharsets.UTF_8);
        }

        saveTransactionIntoMap(transactionData, amount.toString(), String.valueOf(currency), session);

        TransactionFlowData transactionFlowData =
            new TransactionFlowData(id, source, target, amount, offeredMaxFee16Bits, smartContractBytes, textBytes);
        SignUtils.signTransaction(transactionFlowData, AppState.getPrivateKey());
        return transactionFlowData;
    }


    private static void saveTransactionIntoMap(
        CalcTransactionIdSourceTargetResult transactionData, String amount,
        String currency, Session session) {
        if (session.sourceMap == null) {
            session.sourceMap = new ConcurrentHashMap<>();
        }
        long shortTransactionId = NodePojoConverter.getShortTransactionId(transactionData.getTransactionId());
        TransactionRoundData transactionRoundData =
            new TransactionRoundData(String.valueOf(shortTransactionId), transactionData.getWideSource(),
                                     transactionData.getWideTarget(), amount, currency);
        session.sourceMap.put(shortTransactionId, transactionRoundData);
    }

    public static void saveTransactionRoundNumberIntoMap(int roundNumber, long transactionId, Session session) {
        TransactionRoundData transactionRoundData =
            session.sourceMap.get(NodePojoConverter.getShortTransactionId(transactionId));
        transactionRoundData.setRoundNumber(roundNumber);
    }

}