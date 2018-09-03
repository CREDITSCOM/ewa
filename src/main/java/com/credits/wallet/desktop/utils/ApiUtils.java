package com.credits.wallet.desktop.utils;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.crypto.Md5;
import com.credits.leveldb.client.data.TransactionFlowData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.struct.TransactionStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    public static void callTransactionFlow(long innerId, String source, String target, BigDecimal amount,
        BigDecimal balance, byte currency, BigDecimal fee) throws LevelDbClientException,
            CreditsNodeException, CreditsCommonException {

        // Формировование параметров основной транзакции

        // 4 DEBUG
        //innerId=1532529576028l;
        //source="AoRKdBEbozwTKt5sirqx6ERv2DPsrvTk81hyztnndgWC";
        //target="B3EBaHgRU7sd353axMRrZfoL9aL2XjA3oXejDdPrMnHR";
        //amount=new BigDecimal(10.0);
        //fee=new BigDecimal(0.1);
        //currency=1;
        // -------

        TransactionStruct tStruct = new TransactionStruct(innerId, source, target, amount, fee, currency, null);

        ByteBuffer signature=Utils.signTransactionStruct(tStruct);

        TransactionFlowData transactionFlowData =
            new TransactionFlowData(innerId, Converter.decodeFromBASE58(source), Converter.decodeFromBASE58(target), amount, balance, currency,
                    signature.array(), fee);

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
