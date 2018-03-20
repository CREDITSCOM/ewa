package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;

import java.util.Random;
import java.util.UUID;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    public static void prepareAndCallTransactionFlow (
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
    }

    public static String generateTransactionHash() {
        byte[] hashBytes = new byte[8];
        new Random().nextBytes(hashBytes);
        StringBuilder hashBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            int i = ((Byte)b).intValue();
            hashBuilder.append(Integer.toString(Math.abs(i)));
        }
        return hashBuilder.toString();
    }

}
