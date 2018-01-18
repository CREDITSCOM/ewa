package com.credits.service.contract.method;

import org.apache.commons.lang3.math.NumberUtils;

public class MethodParamValueRecognizerHelper {
    public static boolean isNumberLiteralOrCastableNumber(String str) {
        return NumberUtils.isCreatable(str) || isCastedNumber(str);
    }

    public static boolean isCastedNumber(String str) {
        String pattern = "\\((byte|short|int|long|float|double)\\)\\s*.+";
        return str.matches(pattern);
    }

    public static boolean isDoubleLiteral(String str) {
        String pattern1 = "\\d*\\.\\d+(([eE])\\d+)*d?";
        String pattern2 = "\\d+\\.\\d*(([eE])\\d+)*d?";
        return str.matches(pattern1) || str.matches(pattern2);
    }

    public static boolean isNullLiteral(String str) {
        String pattern = "null";
        return str.matches(pattern);
    }

    public static boolean isStringLiteral(String str) {
        char literalMarker = '"';
        int firstQuotePos = str.indexOf(literalMarker);
        int lastQuotePos = str.lastIndexOf(literalMarker);

        return firstQuotePos == 0 && lastQuotePos > firstQuotePos;
    }

    public static boolean isCharLiteral(String str) {
        char literalMarker = '\'';
        int firstQuotePos = str.indexOf(literalMarker);
        int lastQuotePos = str.lastIndexOf(literalMarker);

        return firstQuotePos == 0 && lastQuotePos > firstQuotePos;
    }

    public static boolean isBooleanLiteral(String str) {
        return Boolean.toString(true).equals(str) || Boolean.toString(false).equals(str);
    }

    public static boolean isArray(String str) {
        int firstBrakePos = str.indexOf('{');
        int lastBrakePos = str.lastIndexOf('}');
        return firstBrakePos == 0 && lastBrakePos > firstBrakePos;
    }

    public static Number createCastedNumber(String param) {
        String pattern = "(\\((byte|short|int|long|float|double)\\)\\s*)(.+)";
        String number = param.replaceFirst(pattern, "$3").trim();
        String type = param.replaceFirst(pattern, "$2").trim();
        Number retVal = null;
        if (Long.TYPE.getName().equals(type)) {
            retVal = NumberUtils.createLong(number);
        } else if (Integer.TYPE.getName().equals(type)) {
            retVal = NumberUtils.createInteger(number);
        } else if (Double.TYPE.getName().equals(type)) {
            retVal = NumberUtils.createDouble(number);
        } else if (Float.TYPE.getName().equals(type)) {
            retVal = NumberUtils.createFloat(number);
        } else if (Short.TYPE.getName().equals(type)) {
            retVal = Short.valueOf(number);
        } else if (Byte.TYPE.getName().equals(type)) {
            retVal = Byte.valueOf(number);
        }
        return retVal;
    }

    public static String createFromStringOrCharLiteral(String param, char literalMarker) {
        int firstQuotePos = param.indexOf(literalMarker);
        int lastQuotePos = param.lastIndexOf(literalMarker);
        String retVal = null;
        if (firstQuotePos > -1 && lastQuotePos > firstQuotePos) {
            retVal = param.substring(firstQuotePos + 1, lastQuotePos);
        }
        return retVal;
    }
}
