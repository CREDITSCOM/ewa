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
import com.credits.wallet.desktop.utils.struct.TransactionStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    public static void callTransactionFlow(long innerId, String source, String target, BigDecimal amount,
        BigDecimal balance, byte currency, BigDecimal fee) throws LevelDbClientException, CreditsNodeException {

        // Формировование параметров основной транзакции
        String signature = "";
        try {
            TransactionStruct tStruct = new TransactionStruct(innerId, source, target, amount, balance, currency);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(tStruct);
            signature=new String(Ed25519.sign(baos.toByteArray(), AppState.privateKey));
        } catch (Exception e) {
            // do nothing - no signature
        }

        TransactionFlowData transactionFlowData =
            new TransactionFlowData(innerId, source, target, amount, balance, currency, signature, fee);

        AppState.apiClient.transactionFlow(
                transactionFlowData,
                false
        );
    }

    //public static String generateTransactionInnerId() throws CreditsException {
    //    byte[] hashBytes = Blake2S.generateHash(4); // 4 байта
    //    return Converter.bytesToHex(hashBytes);
    //}

    public static long generateTransactionInnerId() {
        return new Date().getTime();
    }

    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return Converter.bytesToHex(hashBytes);
    }
}
