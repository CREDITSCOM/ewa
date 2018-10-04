package com.credits.wallet.desktop.utils;

import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.struct.TransactionStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class Utils {
    private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static ByteBuffer signTransactionStruct(TransactionStruct tStruct) {
        ByteBuffer signature;
        try {
            byte[] tArr=tStruct.getBytes();

            LOGGER.debug("Smart contract length = "+tStruct.getScLen());
            LOGGER.debug("Transaction structure length = "+tArr.length);
            LOGGER.debug("Transaction structure ^^^^^ ");
            String arrStr="";
            for (int i=0; i<tArr.length; i++)
                arrStr=arrStr+((i==0 ? "" : " ")+tArr[i]);
            LOGGER.debug(arrStr);
            LOGGER.debug("--------------------- vvvvv ");

            byte[] signatureArr= Ed25519.sign(tArr, AppState.privateKey);

            LOGGER.debug("Signature ^^^^^ ");
            arrStr="";
            for (int i=0; i<signatureArr.length; i++)
                arrStr=arrStr+((i==0 ? "" : ", ")+signatureArr[i]);
            LOGGER.debug(arrStr);
            LOGGER.debug("--------- vvvvv ");

            signature = ByteBuffer.wrap(signatureArr);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            signature = ByteBuffer.wrap(new byte[]{});
        }
        return signature;
    }
}

