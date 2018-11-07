package com.credits.general.util;

import com.credits.general.pojo.TransactionRoundData;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class Utils {
    private static final String ALPHA_LOWER_CASE_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";

    public static ConcurrentHashMap<String, ConcurrentHashMap<Long, TransactionRoundData>> sourceMap = new ConcurrentHashMap<>();


    public static byte[] parseSubArray(byte[] array, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, offset, result, 0, length);
        return result;
    }

    public static byte[] concatenateArrays(byte[] firstArr, byte[] secondArr) {
        byte[] resultArr = new byte[firstArr.length + secondArr.length];
        System.arraycopy(firstArr, 0, resultArr, 0, firstArr.length);
        System.arraycopy(secondArr, 0, resultArr, firstArr.length, secondArr.length);
        return resultArr;
    }

    /**
     * Returns a copy of the given byte array in reverse order.
     */
    public static byte[] reverseBytes(byte[] bytes) {
        // We could use the XOR trick here but it's easier to understand if we don't. If we find this is really a
        // performance issue the matter can be revisited.
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = bytes[bytes.length - 1 - i];
        return buf;
    }

    public static String randomAlphaLowerCaseNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()* ALPHA_LOWER_CASE_NUMERIC_STRING.length());
            builder.append(ALPHA_LOWER_CASE_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static boolean isEmpty(final Object object) {
        if (object instanceof String) {
            return ((String) object).trim().isEmpty();
        } else if (object instanceof ByteBuffer) {
            return ((ByteBuffer) object).position() == 0;
        } else if (object instanceof byte[]) {
            return ((byte[]) object).length == 0;
        } else {
            return object == null;
        }

    }

    public static int getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
        String string = bigDecimal.stripTrailingZeros().toPlainString();
        int index = string.indexOf(".");
        return index < 0 ? 0 : string.length() - index - 1;
    }
}