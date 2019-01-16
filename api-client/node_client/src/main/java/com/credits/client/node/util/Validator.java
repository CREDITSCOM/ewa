package com.credits.client.node.util;

import com.credits.client.node.crypto.Ed25519;
import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.SmartContractTransactionFlowData;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.general.util.Utils;

import java.math.BigDecimal;

import static com.credits.general.util.GeneralConverter.decodeFromBASE58;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class Validator {
    /**
     * Проверка transaction hash
     * @param hash
     * пример "8iif6oqo"
     */
    public static void validateTransactionHash(String hash) {
        if (hash.length() != 8) {
            throw new NodeClientException(String.format("Invalid transaction hash: [%s]. Example of valid hash: [%s]", hash, "8iif6oqo"));
        }
    }

    /**
     * Проверка правильности кошелька, на который выполняется транзакция (перевод)
     */
    public static void validateToAddress(String toAddress) {
        // Проверка: является ли toAddress правильным публичным ключом провайдера ED25519
        try {
            byte[] toAddressBytes = decodeFromBASE58(toAddress);
            Ed25519.bytesToPublicKey(toAddressBytes);
        } catch (Exception e) {
            throw new NodeClientException("Invalid To address");
        }
    }

    public static void validate(SmartContractTransactionFlowData data) {
        if (data == null) {
            throw new NodeClientException("smartContractTransactionFlowData is null");
        }
    }

    public static void validate(TransactionFlowData data) {

        if (data == null) {
            throw new NodeClientException("transactionFlowData is null");
        }
        if (Utils.isEmpty(data.getSource())) {
            throw new NodeClientException("account is empty");
        }
        if (Utils.isEmpty(data.getTarget())) {
            throw new NodeClientException("target is empty");
        }
//        if (Utils.isEmpty(transactionFlowData.getSignature())) {
//            throw new NodeClientException("signature is empty");
//        }
        Validator.validateTransactionAmount(data.getAmount());
        Validator.validateTransactionBalance(data.getBalance());
    }

    private static void validateTransactionAmount(BigDecimal amount) {

        int numberOfDecimalPlaces = Utils.getNumberOfDecimalPlaces(amount);
        if (numberOfDecimalPlaces > 18) {
            throw new NodeClientException(String.format("Invalid transaction amount %s, number of decimal places %s larger 18",
                    amount,
                    numberOfDecimalPlaces)
            );
        }
    }

    private static void validateTransactionBalance(BigDecimal balance) {

        int numberOfDecimalPlaces = Utils.getNumberOfDecimalPlaces(balance);
        if (numberOfDecimalPlaces > 18) {
            throw new NodeClientException(String.format("Invalid transaction balance %s, number of decimal places %s larger 18",
                    balance,
                    numberOfDecimalPlaces)
            );
        }
    }
}
