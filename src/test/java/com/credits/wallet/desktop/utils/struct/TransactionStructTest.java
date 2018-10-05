package com.credits.wallet.desktop.utils.struct;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.crypto.exception.CreditsCryptoException;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.service.LevelDbService;
import com.credits.leveldb.client.service.LevelDbServiceImpl;
import com.credits.leveldb.client.util.ApiClientUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class TransactionStructTest {

    private static Logger LOGGER = LoggerFactory.getLogger(TransactionStructTest.class);

    @Test
    public void test01() {
        try {
            byte currencyByte = 1;

            // -125,-89,123,-73,116,78,22,103,79,80,47,-61,-37,-115,106,68,93,-45,-102,93,21,-82,112,-116,-2,54,-87,53,-67,65,101,-97,79,-34,-54,-45,-65,77,11,-43,38,25,97,-93,-79,-38,39,60,89,-69,-1,52,-68,-28,-84,-92,-85,69,-113,33,-100,-109,-39,13,
//             -125,-89,123,-73,116,78,22,103,79,80,47,-61,-37,-115,106,68,93,-45,-102,93,21,-82,112,-116,-2,54,-87,53,-67,65,101,-97,79,-34,-54,-45,-65,77,11,-43,38,25,97,-93,-79,-38,39,60,89,-69,-1,52,-68,-28,-84,-92,-85,69,-113,33,-100,-109,-39,13,
            TransactionStruct transactionStruct = new TransactionStruct(
                    1532529576028L,
                    Converter.decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"),
                    Converter.decodeFromBASE58("B3EBaHgRU7sd353axMRrZfoL9aL2XjA3oXejDdPrMnHR"),
                    new BigDecimal("10.0"),
                    (short)1,
                    currencyByte,
                    null
            );

            byte[] transactionStructBytes = transactionStruct.getBytes();

            StringBuilder builder = new StringBuilder();
            for(byte b : transactionStructBytes) {
                builder.append(b + ",");
            }
            LOGGER.info(builder.toString());


            try {
                byte[] sign = Ed25519.sign(
                        transactionStructBytes,
                        Ed25519.bytesToPrivateKey(
                                Converter.decodeFromBASE58("3rUevsW5xfob6qDxWMDFwwTQCq39SYhzstuyfUGSDvF2QHBRyPD8fSk49wFXaPk3GztfxtuU85QHfMV3ozfqa7rN")
                        )
                );

                builder = new StringBuilder();

                for(byte b : sign) {
                    builder.append(b + ",");
                }
                LOGGER.info(builder.toString());

            } catch (CreditsCryptoException e) {
                e.printStackTrace();
            }

        } catch (CreditsCommonException e) {
            e.printStackTrace();
        } catch (LevelDbClientException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void test02() {
        try {
            byte currencyByte = 1;

            TransactionStruct transactionStruct = new TransactionStruct(
                    1532529576028L,
                    Converter.decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"),
                    Converter.decodeFromBASE58("B3EBaHgRU7sd353axMRrZfoL9aL2XjA3oXejDdPrMnHR"),
                    new BigDecimal("10.0"),
                    (short)1,
                    currencyByte,
                    null
            );

            for(byte b : transactionStruct.getBytes()) {
                LOGGER.info(b + "");
            }

        } catch (CreditsCommonException e) {
            e.printStackTrace();
        } catch (LevelDbClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore                     // integration test
    public void test03() {
        try {
            byte currencyByte = 1;

            SmartContractData smartContractData = null;
            try {
                LevelDbService levelDbService = LevelDbServiceImpl.getInstance("127.0.0.1", 9090);
                smartContractData = levelDbService.getSmartContract("6RNiVco3yb6jsQPaJ7GzjjRFc9aH1Rnyp21HTAh64FRn");
            } catch (CreditsNodeException e) {
                e.printStackTrace();
            }

            byte[] smartContractBytes = ApiClientUtils.serializeByThrift(smartContractData);

            TransactionStruct transactionStruct = new TransactionStruct(
                    1533280630882L,
                    Converter.decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe"),
                    Converter.decodeFromBASE58("B3EBaHgRU7sd353axMRrZfoL9aL2XjA3oXejDdPrMnHR"),
                    new BigDecimal(0),
                    (short) 1,
                    currencyByte,
                    smartContractBytes
            );

            byte[] transactionStructBytes = transactionStruct.getBytes();

            StringBuilder builder = new StringBuilder();
            for(byte b : transactionStructBytes) {
                builder.append(b).append(" ");
            }
            LOGGER.info(builder.toString());

            try {
                byte[] sign = Ed25519.sign(
                        transactionStructBytes,
                        Ed25519.bytesToPrivateKey(
                                Converter.decodeFromBASE58("5LCVyPsn5XAhNDfP4CcSo1pJEWdsCbgrXzhpauVoNZKtRWygEaZ3LrvF6T5EmPYJGRx7pfZXiSZP8tpYn5X6qLvU")
                        )
                );

                builder = new StringBuilder();

                for(byte b : sign) {
                    builder.append(b).append(" ");
                }
                LOGGER.info(builder.toString());

            } catch (CreditsCryptoException e) {
                e.printStackTrace();
            }

        } catch (CreditsCommonException | LevelDbClientException e) {
            e.printStackTrace();
        }
    }


}
