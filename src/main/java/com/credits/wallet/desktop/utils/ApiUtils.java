package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.controller.Const;

import java.security.PrivateKey;
import java.util.UUID;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    public static String prepareAndCallTransactionFlow (
            String source,
            String target,
            Double amount,
            String currency
    ) throws Exception {

        String hash = ApiUtils.generateTransactionHash();
        String innerId = UUID.randomUUID().toString();

        String signatureBASE64 = Ed25519.generateSignOfTransaction(hash, innerId, source, target, amount, currency,
                AppState.privateKey);

        AppState.apiClient.transactionFlow(hash, innerId, source, target, amount, currency, signatureBASE64);

        return innerId;
    }

    /**
     * Создание системной транзакции
     * @param target
     * @throws Exception
     */
    public static void prepareAndCallTransactionFlowSystem(String target) throws Exception {

        String hash = ApiUtils.generateTransactionHash();
        String innerId = UUID.randomUUID().toString();

        byte[] privateKeyByteArr = Converter.decodeFromBASE64(Const.SYS_TRAN_PRIVATE_KEY_BASE64);
        PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);

        String source = Const.SYS_TRAN_PUBLIC_KEY_BASE64;
        Double amount = Const.SYS_TRAN_AMOUNT;
        String currency = Const.SYS_TRAN_CURRENCY;

        String signatureBASE64 = Ed25519.generateSignOfTransaction(
                hash,
                innerId,
                source,
                target,
                amount,
                currency,
                privateKey
        );

        AppState.apiClient.transactionFlow(hash, innerId, source, target, amount, currency, signatureBASE64);
    }


    public static String generateTransactionHash() {
        return Utils.randomAlphaNumeric(8);
    }

}
