package com.credits.wallet.desktop.utils;

import com.credits.client.node.crypto.Ed25519;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.thrift.generated.Amount;
import com.credits.general.util.Converter;
import com.credits.wallet.desktop.AppState;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.credits.client.node.util.NodePojoConverter.bigDecimalToAmount;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class SignUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(SignUtils.class);

    private static int smartContractLen = 0;

    public static void signTransaction(TransactionFlowData tStruct) {
        ByteBuffer signature;
        try {
            byte[] tArr = getBytes(tStruct);

            LOGGER.debug("Smart contract length = " + smartContractLen);
            LOGGER.debug("Transaction structure length = " + tArr.length);
            LOGGER.debug("Transaction structure ^^^^^ ");
            StringBuilder arrStr = new StringBuilder();
            for (int i = 0; i < tArr.length; i++) {
                arrStr.append(i == 0 ? "" : " ").append(tArr[i]);
            }
            LOGGER.debug(arrStr.toString());
            LOGGER.debug("--------------------- vvvvv ");

            byte[] signatureArr = Ed25519.sign(tArr, AppState.privateKey);

            LOGGER.debug("Signature ^^^^^ ");
            arrStr = new StringBuilder();
            for (int i = 0; i < signatureArr.length; i++) {
                arrStr.append(i == 0 ? "" : ", ").append(signatureArr[i]);
            }
            LOGGER.debug(arrStr.toString());
            LOGGER.debug("--------- vvvvv ");

            signature = ByteBuffer.wrap(signatureArr);

        } catch (Exception e) {
            LOGGER.error("Sign transaction is failed");
            throw e;
        }
        tStruct.setSignature(signature.array());
    }

    private static byte[] getBytes(TransactionFlowData tStruct) {
        int amountInt;
        long amountFrac;
        byte ufNum = 0;

        Amount aAmount = bigDecimalToAmount(tStruct.getAmount());
        amountInt = aAmount.integral;
        amountFrac = aAmount.fraction;


        if (tStruct.getSmartContractBytes() != null) {
            ufNum++;
            smartContractLen = tStruct.getSmartContractBytes().length;
        }

        byte[] commentBytes = tStruct.getCommentBytes();
        boolean isCommentBytesExists = (commentBytes != null && commentBytes.length > 0);
        if (isCommentBytesExists) {
            ufNum++;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            byte[] idBytes = Converter.toByteArrayLittleEndian(tStruct.getId(), 8);
            idBytes = ArrayUtils.remove(idBytes, 7); // delete two last bytes
            idBytes = ArrayUtils.remove(idBytes, 6);
            os.write(idBytes);
            os.write(tStruct.getSource());
            os.write(tStruct.getTarget());
            os.write(Converter.toByteArrayLittleEndian(amountInt, 4));
            os.write(Converter.toByteArrayLittleEndian(amountFrac, 8));
            os.write(Converter.toByteArrayLittleEndian(tStruct.getOfferedMaxFee(), 2));
            os.write(Converter.toByteArrayLittleEndian(tStruct.getCurrency(), 1));
            os.write(Converter.toByteArrayLittleEndian(ufNum, 1));
            if (tStruct.getSmartContractBytes()!= null) {
                os.write(Converter.toByteArrayLittleEndian(smartContractLen, 4));
                os.write(Converter.toByteArrayLittleEndian(tStruct.getSmartContractBytes(), smartContractLen));
            }
            if (isCommentBytesExists) {
                os.write(Converter.toByteArrayLittleEndian(commentBytes.length, 4));
                os.write(Converter.toByteArrayLittleEndian(commentBytes, commentBytes.length));
            }
        } catch (IOException e) {
            // do nothing - never happen
        }
        return os.toByteArray();
    }

    public int getSmartContractLen() {
        return smartContractLen;
    }
}

