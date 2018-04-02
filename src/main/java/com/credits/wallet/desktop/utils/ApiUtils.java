package com.credits.wallet.desktop.utils;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.TcpClient;
import com.credits.crypto.Blake2S;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.data.TransactionFlowData;
import com.credits.wallet.desktop.AppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    public static String prepareAndCallTransactionFlow(
            String source,
            String target,
            Double amount,
            String currency,
            String sourceFee,
            String targetFee,
            Double amountFee,
            String currencyFee
    )
        throws Exception {

        // Формировование параметров основной транзакции
        String hash = ApiUtils.generateTransactionHash();
        String innerId = UUID.randomUUID().toString();

        String signatureBASE64 =
            Ed25519.generateSignOfTransaction(hash, innerId, source, target, amount, currency, AppState.privateKey);

        LOGGER.debug("Signature got: {}", signatureBASE64);

        TransactionFlowData transactionFlowData = new TransactionFlowData(
                hash,
                innerId,
                source,
                target,
                amount,
                currency,
                signatureBASE64
        );

        // Формировование параметров транзакции для списания комиссии
        String hashFee = ApiUtils.generateTransactionHash();
        String innerIdFee = UUID.randomUUID().toString();

        String signatureBASE64Fee =
                Ed25519.generateSignOfTransaction(hashFee, innerIdFee, sourceFee, targetFee, amountFee, currencyFee, AppState.privateKey);

        LOGGER.debug("Signature got: {}", signatureBASE64Fee);

        TransactionFlowData transactionFlowDataFee = new TransactionFlowData(
                hashFee,
                innerIdFee,
                sourceFee,
                targetFee,
                amountFee,
                currencyFee,
                signatureBASE64Fee
        );

        AppState.apiClient.transactionFlowWithFee(
                transactionFlowData,
                transactionFlowDataFee,
                true
        );

        return innerId;
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
