package com.credits.general.util;

import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.generate.APIResponse;
import com.credits.general.thrift.generate.Variant;
import com.credits.general.util.exception.ConverterException;
import org.apache.commons.beanutils.converters.BigDecimalConverter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Base64;
import java.util.BitSet;
import java.util.Locale;
import java.util.Objects;

import static java.util.Arrays.stream;

/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
public class Converter {

    public static final String DOUBLE_FORMAT = "#.##################";

    public static String toString(Object value) {
        if (value instanceof Double) {
            NumberFormat nf = NumberFormat.getNumberInstance(Constants.LOCALE);
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern(DOUBLE_FORMAT);
            return df.format(value);
        }
        if (value instanceof Integer) {
            return String.valueOf(value);
        }
        if (value instanceof Long) {
            return String.valueOf(value);
        }
        if (value instanceof BigDecimal) {
            BigDecimalConverter bigDecimalConverter = new BigDecimalConverter();
            bigDecimalConverter.setLocale(Constants.LOCALE);
            bigDecimalConverter.setPattern("#.##################");
            return (String) bigDecimalConverter.convert(String.class, value);
        }
        // TODO Добавить Date и т.д.
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Boolean toBoolean(Object value) {
        if (value instanceof String) {
            String stringValue = (String)value;
            if (stringValue.equalsIgnoreCase("true")) {
                return true;
            } else if (stringValue.equalsIgnoreCase("false")) {
                return false;
            } else {
                throw new IllegalArgumentException(String.format("Invalid string value: %s", stringValue));
            }
        }
        if (value instanceof Integer) {
            Integer intValue = (Integer)value;
            if (intValue == 1) {
                return true;
            } else if (intValue == 0) {
                return false;
            } else {
                throw new IllegalArgumentException(String.format("Invalid int value: %s", intValue));
            }
        }
        // TODO Добавить Byte, Short, Float, Long, Character, Double, BigDecimal, Date и т.д.
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Integer toInteger(Object value) {
        if (value instanceof String) {
            return Integer.parseInt((String)value);
        }
        // TODO Добавить Byte, Short, Long, Character, Float, Double, Float, BigDecimal, Boolean, Date и т.д.
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Float toFloat(Object value) {
        if (value instanceof String) {
            return Float.parseFloat((String)value);
        }
        // TODO Добавить Byte, Short, Integer, Long, Character, Double, BigDecimal, Boolean, Date и т.д.
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Byte toByte(Object value) {
        if (value instanceof String) {
            return Byte.parseByte((String)value);
        }
        // TODO Добавить Short, Integer, Long, Character, Float, Double, BigDecimal, Boolean, Date и т.д.
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Short toShort(Object value) {
        if (value instanceof String) {
            return Short.parseShort((String)value);
        } else if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal)value;
            return bigDecimal.shortValueExact();
        }
        // TODO Добавить Byte, Integer, Long, Character, Float, Double, Boolean, Date и т.д.
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Long toLong(Object value) {
        if (value instanceof String) {
            return Long.parseLong((String)value);
        } else if (value instanceof BitSet) {
            long longValue = 0L;
            BitSet bits = (BitSet)value;
            for (int i = 0; i < bits.length(); ++i) {
                longValue += bits.get(i) ? (1L << i) : 0L;
            }
            return longValue;
        } else if (value instanceof byte[]) {
            byte[] bytes = (byte[])value;
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.put(bytes);
            buffer.flip();//need flip
            return buffer.getLong();
        }
        // TODO Добавить Byte, Short, Integer, Float, Double, Character, BigDecimal, Boolean, Date и т.д.
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Character toCharacter(Object value) {
        if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.length() > 1) {
                throw new IllegalArgumentException(String.format("Unsupported string length: %s, string: %s", stringValue.length(), stringValue));
            }
            return stringValue.charAt(0);
        }
        // TODO Добавить Byte, Short, Integer, Float, Long, Double, BigDecimal, Date и т.д.
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Double toDouble(Object value) {
        if (value instanceof String) {
            return Double.parseDouble((String)value);
        }
        // TODO Добавить Byte, Short, Integer, Long, Float, BigDecimal, Boolean, Date и т.д.
        return null;
    }

    public static Double toDouble(Object value, Locale locale, String doubleFormat) throws ConverterException {
        if (value instanceof String) {
            NumberFormat nf = NumberFormat.getNumberInstance(locale);
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern(doubleFormat);
            try {
                return (Double) df.parse((String) value);
            } catch (ParseException e) {
                throw new ConverterException(e);
            }
        }
        // TODO Добавить Byte, Short, Integer, Long, Float, BigDecimal, Boolean, Date и т.д.
        return null;
    }

    public static BigDecimal toBigDecimal(Object value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Constants.LOCALE);

        if (value instanceof String) {
            String valueAsString = (String) value;

            if (Utils.isEmpty(valueAsString)) {
                return BigDecimal.ZERO;
            }

            String pattern = "#,##0.0#";
            DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
            decimalFormat.setParseBigDecimal(true);

            try {
                return (BigDecimal) decimalFormat.parse(valueAsString);
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }

        if (value instanceof Double) {
            Double doubleValue = (Double) value;
            String text = Converter.toString(Math.abs(doubleValue));
            int integerPlaces = text.indexOf(symbols.getDecimalSeparator());
            int decimalPlaces = text.length() - integerPlaces - 1;
            return new BigDecimal(doubleValue, new MathContext(decimalPlaces));
        }

        if (value instanceof Long) {
            return new BigDecimal((Long) value);
        }


        // TODO Добавить Integer и т.д.
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static String encodeToBASE64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] decodeFromBASE64(String string) {
        return Base64.getDecoder().decode(string);
    }

    public static String encodeToBASE58(byte[] bytes) {
        return Base58.encode(bytes);
    }

    public static byte[] decodeFromBASE58(String string) throws ConverterException {
        return Base58.decode(string);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String byteArrayToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static ByteBuffer byteArrayToByteBuffer(byte[] bytes) {
        return ByteBuffer.wrap(bytes);
    }

    static String byteArrayToString(byte[] bytes, String delimiter) {
        if (delimiter == null || Objects.equals(delimiter, "")) {
            delimiter = " ";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b : bytes) {
            stringBuilder.append(b).append(delimiter);
        }
        return stringBuilder.toString();
    }

    public static byte[] toByteArrayLittleEndian(Object object, int arraySize) {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }
        if (object.getClass().equals(Integer.class)) {
            return ByteBuffer.allocate(arraySize).order(ByteOrder.LITTLE_ENDIAN).putInt((Integer)object).array();
        } else if (object.getClass().equals(Long.class)) {
            return ByteBuffer.allocate(arraySize).order(ByteOrder.LITTLE_ENDIAN).putLong((Long)object).array();
        } else if (object.getClass().equals(Short.class)) {
            return ByteBuffer.allocate(arraySize).order(ByteOrder.LITTLE_ENDIAN).putShort((Short)object).array();
        } else if (object.getClass().equals(Byte.class)) {
            return ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN).put((Byte)object).array();
        } else if (object instanceof byte[]) {
            return ByteBuffer.allocate(arraySize).order(ByteOrder.LITTLE_ENDIAN).put((byte)object).array();
        } else if (object instanceof Byte[]) {
            Byte[] bigBytes = (Byte[]) object;
            ByteBuffer byteBuffer = ByteBuffer.allocate(bigBytes.length);
            stream(bigBytes).forEachOrdered(byteBuffer::put);
            return byteBuffer.order(ByteOrder.LITTLE_ENDIAN).array();
        }
        throw new IllegalArgumentException(String.format("Unsupported object class: %s", object.getClass().getSimpleName()));
    }

    public static byte[] toByteArray(Object value) throws ConverterException {
        if (value instanceof Long) {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong((Long)value);
            return buffer.array();
        }
        // TODO Добавить String, Byte, Short, Integer, Float, Double, BigDecimal, Date и т.д.
        throw new ConverterException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));

    }

    public static BitSet toBitSet(Object value) {
        if (value instanceof byte[]) {
            BitSet bits = new BitSet();
            byte[] bytes = (byte[])value;
            for (int i = 0; i < bytes.length * 8; i++) {
                if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                    bits.set(i);
                }
            }
            return bits;
        } else if (value instanceof Long) {
            BitSet bits = new BitSet();
            long longValue = (long) value;
            int index = 0;
            while (longValue != 0L) {
                if (longValue % 2L != 0) {
                    bits.set(index);
                }
                ++index;
                longValue = longValue >>> 1;
            }
            return bits;
        }
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static ApiResponseData apiResponseToApiResponseData(APIResponse apiResponse) {
        return new ApiResponseData(
            apiResponse.getCode(),
            apiResponse.getMessage(),
            null
        );
    }

    public static ApiResponseData apiResponseToApiResponseData(APIResponse apiResponse, Variant smartContractResult) {
        return new ApiResponseData(
            apiResponse.getCode(),
            apiResponse.getMessage(),
            smartContractResult
        );
    }
}
