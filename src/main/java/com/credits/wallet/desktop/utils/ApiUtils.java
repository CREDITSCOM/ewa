package com.credits.wallet.desktop.utils;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.common.utils.TcpClient;
import com.credits.crypto.Blake2S;
import com.credits.crypto.Ed25519;
import com.credits.crypto.Md5;
import com.credits.leveldb.client.data.TransactionFlowData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.wallet.desktop.AppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    public static void callTransactionFlow(String innerId, String source, String target, BigDecimal amount,
        BigDecimal balance, String currency) throws LevelDbClientException, CreditsNodeException {

        // Формировование параметров основной транзакции
        String signature =
            Ed25519.generateSignOfTransaction(innerId, source, target, amount, balance, currency, AppState.privateKey);

        TransactionFlowData transactionFlowData =
            new TransactionFlowData(innerId, source, target, amount, balance, currency, signature);

        AppState.apiClient.transactionFlow(
                transactionFlowData,
                false
        );
    }

    public static String generateTransactionInnerId() throws CreditsException {
        byte[] hashBytes = Blake2S.generateHash(4); // 4 байта
        return Converter.bytesToHex(hashBytes);
    }

    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return Converter.bytesToHex(hashBytes);
    }
}
