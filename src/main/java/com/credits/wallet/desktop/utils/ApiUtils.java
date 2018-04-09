package com.credits.wallet.desktop.utils;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.TcpClient;
import com.credits.crypto.Blake2S;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.data.TransactionFlowData;
import com.credits.wallet.desktop.AppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    public static void callTransactionFlowWithFee(
            String hash,
            String innerId,
            String source,
            String target,
            Double amount,
            String currency,
            String hashFee,
            String innerIdFee,
            String sourceFee,
            String targetFee,
            Double amountFee,
            String currencyFee
    )
        throws Exception {

        // Формировование параметров основной транзакции
        String signature =
            Ed25519.generateSignOfTransaction(hash, innerId, source, target, amount, currency, AppState.privateKey);

        TransactionFlowData transactionFlowData = new TransactionFlowData(
                hash,
                innerId,
                source,
                target,
                amount,
                currency,
                signature
        );

        // Формировование параметров транзакции для списания комиссии
        String signatureFee =
                Ed25519.generateSignOfTransaction(hashFee, innerIdFee, sourceFee, targetFee, amountFee, currencyFee, AppState.privateKey);

        TransactionFlowData transactionFlowDataFee = new TransactionFlowData(
                hashFee,
                innerIdFee,
                sourceFee,
                targetFee,
                amountFee,
                currencyFee,
                signatureFee
        );

        AppState.apiClient.transactionFlowWithFee(
                transactionFlowData,
                transactionFlowDataFee,
                true
        );
    }

    /**
     * Выполнение системной транзакции
     *
     * @param target
     * @throws Exception
     */
    public static void execSystemTransaction(String target) throws Exception {

        TcpClient.sendRequest(AppState.csSenderBotHost, AppState.csSenderBotPort, target);
    }


    public static String generateTransactionHash() throws CreditsException {
        byte[] hashBytes = Blake2S.generateHash(4); // 4 байта
        return com.credits.leveldb.client.util.Converter.bytesToHex(hashBytes);
    }

}
