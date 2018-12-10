package com.credits.general.util;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class Utils {
    private static final String ALPHA_LOWER_CASE_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
    private static final String COLLECTION_VALUES_DELIMITER = "\\|";
    public static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static final String STRING_TYPE = "String";

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
            String objectString = (String)object;
            return (objectString == null || objectString.trim().isEmpty());
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

    //todo change String className type to enum type
    public static Object createVariantObject(String className, String value) {

        int openingBracketPosition = className.indexOf("<");
        boolean genericExists = (openingBracketPosition > -1);

        String classNameWOGeneric = className; // class name without generic, example: List<Integer> -> List
        String genericName = null;
        if (genericExists) {
            classNameWOGeneric = className.substring(0, openingBracketPosition);
            int closingBracketPosition = className.indexOf(">ValidationException ", openingBracketPosition);
            genericName = className.substring(openingBracketPosition + 1, closingBracketPosition);
        }

        switch (classNameWOGeneric) {
            case "Object": return value;
            case STRING_TYPE: return value;
            case "Byte": return Converter.toByte(value);
            case "byte": return Converter.toByte(value);
            case "Short": return Converter.toShort(value);
            case "short": return Converter.toShort(value);
            case "Integer": return Converter.toInteger(value);
            case "int": return Converter.toInteger(value);
            case "Long": return Converter.toLong(value);
            case "long": return Converter.toLong(value);
            case "Double": return Converter.toDouble(value);
            case "double": return Converter.toDouble(value);
            case "Boolean": return Converter.toBoolean(value);
            case "boolean": return Converter.toBoolean(value);
            case "List":
                List<Object> variantObjectList = new ArrayList<>();
                String[] objectArr = value.split(COLLECTION_VALUES_DELIMITER);
                for (String object : objectArr) {
                    Object variantObject;
                    if (genericExists) {
                        variantObject = createVariantObject(genericName, object.trim());
                    } else {
                        variantObject = createVariantObject("Object", object.trim());
                    }
                    variantObjectList.add(variantObject);
                }
                return variantObjectList;
            default: throw new IllegalArgumentException(String.format("Unsupported class: %s", className));
        }
    }
}