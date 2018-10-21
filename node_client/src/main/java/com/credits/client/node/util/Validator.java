package com.credits.client.node.util;

import com.credits.client.node.crypto.Ed25519;
import com.credits.client.node.pojo.CreateTransactionData;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.Converter;
import com.credits.general.util.Utils;
import com.credits.general.util.exception.ConverterException;

import java.math.BigDecimal;

import static com.credits.general.util.Converter.decodeFromBASE58;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class Validator {
    /**
     * Проверка transaction hash
     * @param hash
     * пример "8iif6oqo"
     * @throws CreditsException
     */
    public static void validateTransactionHash(String hash) throws ConverterException {
        if (hash.length() != 8) {
            throw new ConverterException(String.format("Invalid transaction hash: [%s]. Example of valid hash: [%s]", hash, "8iif6oqo"));
        }
    }

    /**
     * Проверка правильности кошелька, на который выполняется транзакция (перевод)
     * @param toAddress
     */
    public static void validateToAddress(String toAddress) throws ConverterException {
        // Проверка: является ли toAddress правильным публичным ключом провайдера ED25519
        try {
            byte[] toAddressBytes = decodeFromBASE58(toAddress);
            Ed25519.bytesToPublicKey(toAddressBytes);
        } catch (Exception e) {
            throw new ConverterException("Invalid To address");
        }
    }

    public static void validateCreateTransactionData(CreateTransactionData createTransactionData) throws ConverterException {

        if (Utils.isEmpty(createTransactionData.getSource())) {
            throw new ConverterException("account is empty");
        }
        if (Utils.isEmpty(createTransactionData.getTarget())) {
            throw new ConverterException("target is empty");
        }
//        if (Utils.isEmpty(createTransactionData.getSignature())) {
//            throw new NodeClientException("signature is empty");
//        }
        validateTransactionAmount(createTransactionData.getAmount());
        validateTransactionBalance(createTransactionData.getBalance());
    }

    public static void validateTransactionAmount(BigDecimal amount) throws ConverterException {

        int numberOfDecimalPlaces = Utils.getNumberOfDecimalPlaces(amount);
        if (numberOfDecimalPlaces > 18) {
            throw new ConverterException(String.format("Invalid transaction amount %s, number of decimal places %s larger 18",
                    amount,
                    numberOfDecimalPlaces)
            );
        }
    }

    public static void validateTransactionBalance(BigDecimal balance) throws ConverterException {

        int numberOfDecimalPlaces = Utils.getNumberOfDecimalPlaces(balance);
        if (numberOfDecimalPlaces > 18) {
            throw new ConverterException(String.format("Invalid transaction balance %s, number of decimal places %s larger 18",
                    balance,
                    numberOfDecimalPlaces)
            );
        }
    }
}
