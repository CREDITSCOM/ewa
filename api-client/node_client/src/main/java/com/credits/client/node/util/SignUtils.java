package com.credits.client.node.util;

import com.credits.client.node.crypto.Ed25519;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.thrift.generated.Amount;
import com.credits.general.util.GeneralConverter;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;

import static com.credits.client.node.util.NodePojoConverter.bigDecimalToAmount;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class SignUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(SignUtils.class);

    private static int smartContractLen = 0;

    public static void signTransaction(TransactionFlowData tStruct, PrivateKey privateKey) {
        ByteBuffer signature;
        try {
            byte[] tArr = printBytes("Transaction structure", getBytes(tStruct));
            byte[] signatureArr = Ed25519.sign(tArr, privateKey);
            signature = ByteBuffer.wrap(signatureArr);

        } catch (Exception e) {
            LOGGER.error("Sign transaction is failed");
            throw e;
        }
        tStruct.setSignature(signature.array());
    }

    private static byte[] printBytes(String message, byte[] bytes) {
        if(bytes != null) {
            StringBuilder arrStr = new StringBuilder();
            LOGGER.debug("");
            LOGGER.debug("----- {} -----", message);
            LOGGER.debug("{} bytes", bytes.length);

            for (byte b : bytes) {
                arrStr.append(String.format("%02X", b));
            }
            LOGGER.debug(arrStr.toString());
            LOGGER.debug("--------------------------");
        }
        return bytes;
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
            byte[] idBytes = GeneralConverter.toByteArrayLittleEndian(tStruct.getId(), 8);
            idBytes = ArrayUtils.remove(idBytes, 7); // delete two last bytes
            idBytes = ArrayUtils.remove(idBytes, 6);
            os.write(printBytes("id", idBytes));
            os.write(printBytes("source", tStruct.getSource()));
            os.write(printBytes("target", tStruct.getTarget()));
            os.write(printBytes("amountInt", GeneralConverter.toByteArrayLittleEndian(amountInt, 4)));
            os.write(printBytes("amountFrac", GeneralConverter.toByteArrayLittleEndian(amountFrac, 8)));
            os.write(printBytes("fee", GeneralConverter.toByteArrayLittleEndian(tStruct.getOfferedMaxFee16Bits(), 2)));
            os.write(printBytes("currency", GeneralConverter.toByteArrayLittleEndian(tStruct.getCurrency(), 1)));
            os.write(printBytes("ufNum", GeneralConverter.toByteArrayLittleEndian(ufNum, 1)));
            if (tStruct.getSmartContractBytes() != null) {
                os.write(printBytes("smartContractLen", GeneralConverter.toByteArrayLittleEndian(smartContractLen, 4)));
                os.write(printBytes("smartContract", GeneralConverter.toByteArrayLittleEndian(tStruct.getSmartContractBytes(), smartContractLen)));
            }
            if (isCommentBytesExists) {
                os.write(printBytes("commentLen", GeneralConverter.toByteArrayLittleEndian(commentBytes.length, 4)));
                os.write(printBytes("comment", GeneralConverter.toByteArrayLittleEndian(commentBytes, commentBytes.length)));
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

