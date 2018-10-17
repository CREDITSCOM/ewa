package com.credits.general.util;

import com.credits.general.crypto.Ed25519;
import com.credits.general.exception.GeneralClientException;

import java.math.BigDecimal;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class Validator {
    /**
     * Проверка transaction hash
     * @param hash
     * пример "8iif6oqo"
     * @throws GeneralClientException
     */
    public static void validateTransactionHash(String hash) throws GeneralClientException {

        if (hash.length() != 8) {
            throw new GeneralClientException(String.format("Invalid transaction hash: [%s]. Example of valid hash: [%s]", hash, "8iif6oqo"));
        }
    }

    /**
     * Проверка правильности кошелька, на который выполняется транзакция (перевод)
     * @param toAddress
     */
    public static void validateToAddress(String toAddress) throws GeneralClientException {
        // Проверка: является ли toAddress правильным публичным ключом провайдера ED25519
        try {
            byte[] toAddressBytes = Converter.decodeFromBASE58(toAddress);
            Ed25519.bytesToPublicKey(toAddressBytes);
        } catch (Exception e) {
            throw new GeneralClientException("Invalid To address");
        }
    }

    //TODO move to node general
//    public static void validateCreateTransactionData(CreateTransactionData createTransactionData) throws GeneralClientException {
//
//        if (Utils.isEmpty(createTransactionData.getSource())) {
//            throw new GeneralClientException("account is empty");
//        }
//        if (Utils.isEmpty(createTransactionData.getTarget())) {
//            throw new GeneralClientException("target is empty");
//        }
////        if (Utils.isEmpty(createTransactionData.getSignature())) {
////            throw new NodeClientException("signature is empty");
////        }
//        validateTransactionAmount(createTransactionData.getAmount());
//        validateTransactionBalance(createTransactionData.getBalance());
//    }

    public static void validateTransactionAmount(BigDecimal amount) throws GeneralClientException {

        int numberOfDecimalPlaces = Utils.getNumberOfDecimalPlaces(amount);
        if (numberOfDecimalPlaces > 18) {
            throw new GeneralClientException(String.format("Invalid transaction amount %s, number of decimal places %s larger 18",
                    amount,
                    numberOfDecimalPlaces)
            );
        }
    }

    public static void validateTransactionBalance(BigDecimal balance) throws GeneralClientException {

        int numberOfDecimalPlaces = Utils.getNumberOfDecimalPlaces(balance);
        if (numberOfDecimalPlaces > 18) {
            throw new GeneralClientException(String.format("Invalid transaction balance %s, number of decimal places %s larger 18",
                    balance,
                    numberOfDecimalPlaces)
            );
        }
    }
}
