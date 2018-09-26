package com.credits.wallet.desktop.utils;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.crypto.Md5;
import com.credits.leveldb.client.data.CreateTransactionData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.util.TransactionType;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.struct.CalcTransactionIdSourceTargetResult;
import com.credits.wallet.desktop.utils.struct.TransactionStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Date;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    public static void callCreateTransaction(TransactionType transactionType) throws LevelDbClientException,
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
        BigDecimal fee = AppState.transactionFeeValue;

        TransactionStruct tStruct = new TransactionStruct(
                calcTransactionIdSourceTargetResult.getTransactionId(),
                calcTransactionIdSourceTargetResult.getSource(),
                calcTransactionIdSourceTargetResult.getTarget(),
                amount,
                fee,
                currency,
                null
        );

        ByteBuffer signature = Utils.signTransactionStruct(tStruct);

        CreateTransactionData createTransactionData = new CreateTransactionData(
                calcTransactionIdSourceTargetResult.getTransactionId(),
                calcTransactionIdSourceTargetResult.getSource(),
                calcTransactionIdSourceTargetResult.getTarget(),
                amount,
                balance,
                currency,
                fee,
                signature.array()
        );

        AppState.apiClient.asyncCreateTransaction(
                createTransactionData,
                false,
                transactionType
        );

        // add or update transactionId in cache
        AppState.walletLastTransactionIdCache.put(sourceBase58, calcTransactionIdSourceTargetResult.getTransactionId());
        AppState.transactionId = calcTransactionIdSourceTargetResult.getTransactionId();
    }

    public static long generateTransactionInnerId() {
        return new Date().getTime();
    }

    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return Converter.bytesToHex(hashBytes);
    }

    public static long createTransactionId(boolean senderIndexExists, boolean receiverIndexExists, long transactionId) {

        BitSet transactionIdBitSet = Converter.longToBitSet(transactionId);
        for (int i = 63; i > 45; i--) {
            transactionIdBitSet.set(i, false);
        }
        transactionIdBitSet.set(47, senderIndexExists);
        transactionIdBitSet.set(46, receiverIndexExists);

        return Converter.bitSetToLong(transactionIdBitSet);
    }

    public static CalcTransactionIdSourceTargetResult calcTransactionIdSourceTarget(
            String sourceBase58,
            String targetBase58
    ) throws CreditsCommonException, LevelDbClientException, CreditsNodeException {

        byte[] source = Converter.decodeFromBASE58(sourceBase58);
        byte[] target = Converter.decodeFromBASE58(targetBase58);

        // get transactions count from Node and increment it
        Long transactionId = AppState.apiClient.getWalletTransactionsCount(source) + 1;
        // get last transaction id from cache
        Long walletLastTransactionIdInCache = AppState.walletLastTransactionIdCache.get(sourceBase58);

        if (transactionId < walletLastTransactionIdInCache) {
            transactionId = walletLastTransactionIdInCache;
        }

        boolean sourceIndexExists = false;
        boolean targetIndexExists = false;

        Integer sourceWalletId = AppState.apiClient.getWalletId(source);
        if (sourceWalletId != 0) {
            sourceIndexExists = true;
            source = Converter.toByteArrayLittleEndian(sourceWalletId, 4);
        }
        Integer targetWalletId = AppState.apiClient.getWalletId(target);
        if (targetWalletId != 0) {
            targetIndexExists = true;
            target = Converter.toByteArrayLittleEndian(targetWalletId, 4);
        }

        return new CalcTransactionIdSourceTargetResult(
                ApiUtils.createTransactionId(sourceIndexExists, targetIndexExists, transactionId),
                source,
                target
        );
    }
}