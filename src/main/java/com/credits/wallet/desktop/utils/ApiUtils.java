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
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    public static void callTransactionFlow(long innerId, String source, String target, BigDecimal amount,
        BigDecimal balance, byte currency, BigDecimal fee) throws LevelDbClientException, CreditsNodeException {

        // Формировование параметров основной транзакции
        ByteBuffer signature;
        try {
            // 4 DEBUG
            //innerId=1532529576028l;
            //source="AoRKdBEbozwTKt5sirqx6ERv2DPsrvTk81hyztnndgWC";
            //target="B3EBaHgRU7sd353axMRrZfoL9aL2XjA3oXejDdPrMnHR";
            //amount=new BigDecimal(10.0);
            //fee=new BigDecimal(0.1);
            //currency=1;
            // -------

            TransactionStruct tStruct = new TransactionStruct(innerId, source, target, amount, fee, currency, null);
            byte[] tArr=tStruct.getBytes();

            LOGGER.debug("Transaction structure ^^^^^ ");
            String arrStr="";
            for (int i=0; i<tArr.length; i++)
                arrStr=arrStr+((i==0 ? "" : ", ")+tArr[i]);
            LOGGER.debug(arrStr);
            LOGGER.debug("--------------------- vvvvv ");

            byte[] signatureArr=Ed25519.sign(tArr, AppState.privateKey);

            LOGGER.debug("Signature ^^^^^ ");
            arrStr="";
            for (int i=0; i<signatureArr.length; i++)
                arrStr=arrStr+((i==0 ? "" : ", ")+signatureArr[i]);
            LOGGER.debug(arrStr);
            LOGGER.debug("--------- vvvvv ");

            signature = ByteBuffer.wrap(signatureArr);

        } catch (Exception e) {
            signature = ByteBuffer.wrap(new byte[]{});
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
