package com.credits.wallet.desktop.utils;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.exception.CreditsException;
import com.credits.crypto.Ed25519;
import com.credits.crypto.Md5;
import com.credits.leveldb.client.ApiTransactionThreadRunnable;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.data.CreateTransactionData;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.data.SmartContractInvocationData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.util.ApiClientUtils;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.struct.CalcTransactionIdSourceTargetResult;
import com.credits.wallet.desktop.utils.struct.TransactionStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import static com.credits.common.utils.Converter.byteArrayToHex;
import static com.credits.common.utils.Converter.encodeToBASE58;
import static com.credits.common.utils.Converter.toBitSet;
import static com.credits.common.utils.Converter.toByteArray;
import static com.credits.common.utils.Converter.toByteArrayLittleEndian;
import static com.credits.common.utils.Converter.toLong;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    public static void callCreateTransaction() throws LevelDbClientException,
            CreditsNodeException, CreditsCommonException {

        String sourceBase58 = AppState.account;
        String targetBase58 = AppState.toAddress;

        CalcTransactionIdSourceTargetResult calcTransactionIdSourceTargetResult = ApiUtils.calcTransactionIdSourceTarget(
                sourceBase58,
                targetBase58
        );

        BigDecimal amount = AppState.amount;
        BigDecimal balance = AppState.balance;
        byte currency = 1;
        Short offeredMaxFee = AppState.transactionOfferedMaxFeeValue;

        TransactionStruct tStruct = new TransactionStruct(
                calcTransactionIdSourceTargetResult.getTransactionId(),
                calcTransactionIdSourceTargetResult.getByteSource(),
                calcTransactionIdSourceTargetResult.getByteTarget(),
                amount,
                offeredMaxFee,
                currency,
                null
        );

        ByteBuffer signature = Utils.signTransactionStruct(tStruct);

        CreateTransactionData createTransactionData = new CreateTransactionData(
                calcTransactionIdSourceTargetResult.getTransactionId(),
                calcTransactionIdSourceTargetResult.getByteSource(),
                calcTransactionIdSourceTargetResult.getByteTarget(),
                amount,
                balance,
                currency,
                offeredMaxFee,
                signature.array()
        );

        AppState.levelDbService.asyncCreateTransaction(createTransactionData, false, new ApiTransactionThreadRunnable.Callback() {
            @Override
            public void onSuccess(ApiResponseData resultData) {
                FormUtils.showPlatformInfo("Execute transaction was success");
            }

            @Override
            public void onError(Exception e) {
                FormUtils.showPlatformError(e.getMessage());
            }
        });

        // add or update transactionId in cache
    }

    public static long generateTransactionInnerId() {
        return new Date().getTime();
    }

    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return byteArrayToHex(hashBytes);
    }

    public static long createTransactionId(boolean senderIndexExists, boolean receiverIndexExists, long transactionId) {

        byte[] transactionIdBytes = toByteArray(transactionId);
        BitSet transactionIdBitSet = toBitSet(transactionIdBytes);
        for (int i = 63; i > 45; i--) {
            transactionIdBitSet.set(i, false);
        }
        transactionIdBitSet.set(47, senderIndexExists);
        transactionIdBitSet.set(46, receiverIndexExists);
        return toLong(transactionIdBitSet);
    }

    public static CalcTransactionIdSourceTargetResult calcTransactionIdSourceTarget(
            String sourceBase58,
            String targetBase58
    ) throws CreditsCommonException, LevelDbClientException, CreditsNodeException {

        // get transactions count from Node and increment it
        Long transactionId = AppState.levelDbService.getWalletTransactionsCount(sourceBase58) + 1;
        // get last transaction id from cache
        Long walletLastTransactionIdInCache = AppState.walletLastTransactionIdCache.get(sourceBase58);

        if (walletLastTransactionIdInCache!=null && transactionId < walletLastTransactionIdInCache) {
            transactionId = walletLastTransactionIdInCache+1;
        }
        AppState.walletLastTransactionIdCache.put(sourceBase58, transactionId);

        boolean sourceIndexExists = false;
        boolean targetIndexExists = false;

        Integer sourceWalletId = AppState.levelDbService.getWalletId(sourceBase58);
        if (sourceWalletId != 0) {
            sourceIndexExists = true;
            sourceBase58 = encodeToBASE58(toByteArrayLittleEndian(sourceWalletId, 4));
        }
        Integer targetWalletId = AppState.levelDbService.getWalletId(targetBase58);
        if (targetWalletId != 0) {
            targetIndexExists = true;
            targetBase58 = encodeToBASE58(toByteArrayLittleEndian(targetWalletId, 4));
        }

        return new CalcTransactionIdSourceTargetResult(
                ApiUtils.createTransactionId(sourceIndexExists, targetIndexExists, transactionId),
                sourceBase58,
                targetBase58
        );
    }

    public static void deploySmartContractProcess(String javaCode, byte[] byteCode, String hashState)
        throws LevelDbClientException, CreditsCommonException, CreditsNodeException {
        SmartContractInvocationData smartContractInvocationData =
            new SmartContractInvocationData(javaCode, byteCode, hashState, "", new ArrayList<>(), false);

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

        byte[] scBytes = ApiClientUtils.serializeByThrift(smartContractInvocationData);
        CalcTransactionIdSourceTargetResult calcTransactionIdSourceTargetResult =
            ApiUtils.calcTransactionIdSourceTarget(AppState.account, transactionTarget);

        TransactionStruct tStruct = new TransactionStruct(calcTransactionIdSourceTargetResult.getTransactionId(),
            calcTransactionIdSourceTargetResult.getByteSource(), calcTransactionIdSourceTargetResult.getByteTarget(),
            new BigDecimal(0), (short)0, (byte) 1, scBytes);
        ByteBuffer signature = Utils.signTransactionStruct(tStruct);

        AppState.levelDbService.executeSmartContract(calcTransactionIdSourceTargetResult.getTransactionId(),
            calcTransactionIdSourceTargetResult.getSource(), calcTransactionIdSourceTargetResult.getTarget(),
            smartContractInvocationData, signature.array(), new ApiTransactionThreadRunnable.Callback() {
                @Override
                public void onSuccess(ApiResponseData resultData) {
                    String target = resultData.getTarget();
                    StringSelection selection = new StringSelection(target);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    FormUtils.showPlatformInfo(
                        String.format("Smart-contract address\n\n%s\n\nhas generated and copied to clipboard", target));
                }
                @Override
                public void onError(Exception e) {
                    FormUtils.showPlatformError(e.getMessage());
                }
            });
    }

    public static void executeSmartContractProcess(String method, List<Object> params,
        SmartContractData smartContractData, ApiTransactionThreadRunnable.Callback callback)
        throws LevelDbClientException, CreditsCommonException, CreditsNodeException {
        SmartContractInvocationData smartContractInvocationData =
            new SmartContractInvocationData("", new byte[0], smartContractData.getHashState(), method, params, false);

        byte[] scBytes = ApiClientUtils.serializeByThrift(smartContractInvocationData);
        CalcTransactionIdSourceTargetResult calcTransactionIdSourceTargetResult =
            ApiUtils.calcTransactionIdSourceTarget(AppState.account,
                encodeToBASE58(smartContractData.getAddress()));

        TransactionStruct tStruct = new TransactionStruct(calcTransactionIdSourceTargetResult.getTransactionId(),
            calcTransactionIdSourceTargetResult.getByteSource(), calcTransactionIdSourceTargetResult.getByteTarget(),
            new BigDecimal(0), (short)0, (byte) 1, scBytes);

        ByteBuffer signature = Utils.signTransactionStruct(tStruct);

        AppState.levelDbService.executeSmartContract(calcTransactionIdSourceTargetResult.getTransactionId(),
            calcTransactionIdSourceTargetResult.getSource(), calcTransactionIdSourceTargetResult.getTarget(),
            smartContractInvocationData, signature.array(), callback);
    }
    private static String generatePublicKeyBase58() {
        KeyPair keyPair = Ed25519.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        return encodeToBASE58(Ed25519.publicKeyToBytes(publicKey));
    }

}