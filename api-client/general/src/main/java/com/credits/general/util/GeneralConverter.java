package com.credits.general.util;

import com.credits.general.pojo.AnnotationData;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.Annotation;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.MethodArgument;
import com.credits.general.thrift.generated.MethodDescription;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.exception.ConverterException;
import com.github.drapostolos.typeparser.TypeParser;
import org.apache.commons.beanutils.converters.BigDecimalConverter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.math.NumberUtils.createBigDecimal;

/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
public class GeneralConverter {

    public static final String DOUBLE_FORMAT = "#.##################";

    public static String toString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Double) {
            NumberFormat nf = NumberFormat.getNumberInstance(Constants.LOCALE);
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern(DOUBLE_FORMAT);
            return df.format(value);
        }
        if (value instanceof Float) {
            return String.valueOf(value);
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
        if (value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Byte) {
            return String.valueOf(value);
        }
        if (value instanceof Short) {
            return String.valueOf(value);
        }
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.equalsIgnoreCase("true")) {
                return true;
            } else if (stringValue.equalsIgnoreCase("false")) {
                return false;
            } else {
                throw new IllegalArgumentException(String.format("Invalid string value: %s", stringValue));
            }
        }
        if (value instanceof Integer) {
            Integer intValue = (Integer) value;
            if (intValue == 1) {
                return true;
            } else if (intValue == 0) {
                return false;
            } else {
                throw new IllegalArgumentException(String.format("Invalid int value: %s", intValue));
            }
        }
        if (value instanceof Byte) {
            Byte byteValue = (Byte) value;
            if (byteValue == 1) {
                return true;
            } else if (byteValue == 0) {
                return false;
            } else {
                throw new IllegalArgumentException(String.format("Invalid byte value: %s", byteValue));
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        if (value instanceof Double) {
            final Double doubleValue = (Double) value;
            if (Math.abs(doubleValue) < (double) Integer.MIN_VALUE || Math.abs(doubleValue) > (double) Integer.MAX_VALUE) {
                throw new IllegalArgumentException(String.format("Math.abs(value) %s is out of range Integer [%s %s]",
                                                                 Math.abs(doubleValue),
                                                                 Integer.MIN_VALUE,
                                                                 Integer.MAX_VALUE));
            }
            return ((Double) value).intValue();
        }
        if (value instanceof Byte) {
            return Integer.valueOf((Byte) value);
        }
        if (value instanceof Short) {
            return Integer.valueOf((Short) value);
        }
        if (value instanceof Long) {
            final Long longValue = (Long) value;
            if (Math.abs(longValue) < (long) Integer.MIN_VALUE || Math.abs(longValue) > (long) Integer.MAX_VALUE) {
                throw new IllegalArgumentException(String.format("Math.abs(value) %s is out of range Integer [%s %s]",
                                                                 Math.abs(longValue),
                                                                 Integer.MIN_VALUE,
                                                                 Integer.MAX_VALUE));
            }
            return ((Long) value).intValue();
        }
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Float toFloat(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return Float.parseFloat((String) value);
        }
        if (value instanceof Double) {
            Double doubleValue = (Double) value;
            if (Math.abs(doubleValue) < (double) Float.MIN_NORMAL || Math.abs(doubleValue) > (double) Float.MAX_VALUE) {
                throw new IllegalArgumentException(String.format("Math.abs(value) %s is out of range Float [%s %s]",
                                                                 Math.abs(doubleValue),
                                                                 Float.MIN_NORMAL,
                                                                 Float.MAX_VALUE));
            }
            return (float) (double) value;
        }
        if (value instanceof Byte) {
            return (float) (byte) value;
        }
        if (value instanceof Short) {
            return (float) (short) value;
        }
        if (value instanceof Integer) {
            return (float) (int) value;
        }
        if (value instanceof Integer) {
            return (float) (int) value;
        }

        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Byte toByte(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return Byte.parseByte((String) value);
        }
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Short toShort(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return Short.parseShort((String) value);
        } else if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) value;
            return bigDecimal.shortValueExact();
        }
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return Long.parseLong((String) value);
        } else if (value instanceof BitSet) {
            long longValue = 0L;
            BitSet bits = (BitSet) value;
            for (int i = 0; i < bits.length(); ++i) {
                longValue += bits.get(i) ? (1L << i) : 0L;
            }
            return longValue;
        } else if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.put(bytes);
            buffer.flip();//need flip
            return buffer.getLong();
        } else if (value instanceof Double) {
            return ((Double) value).longValue();
        }
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static List<ByteCodeObjectData> compilationPackageToByteCodeObjects(
            CompilationPackage compilationPackage) {
        List<ByteCodeObjectData> compilationUnits = new ArrayList<>();
        compilationPackage.getUnits().forEach(unit -> compilationUnits.add(new ByteCodeObjectData(unit.getName(), unit.getByteCode())));
        return compilationUnits;
    }

    public static List<ByteCodeObject> byteCodeObjectsDataToByteCodeObjects(List<ByteCodeObjectData> byteCodeObjects) {
        if (byteCodeObjects == null) {
            return null;
        }
        List<ByteCodeObject> compilationUnits = new ArrayList<>();
        byteCodeObjects.forEach(unit -> compilationUnits.add(new ByteCodeObject(unit.getName(), ByteBuffer.wrap(unit.getByteCode()))));
        return compilationUnits;
    }


    public static List<ByteCodeObjectData> byteCodeObjectsToByteCodeObjectsData(
            List<ByteCodeObject> thriftByteCodeObjects) {
        List<ByteCodeObjectData> compilationUnits = new ArrayList<>();
        thriftByteCodeObjects.forEach(unit -> compilationUnits.add(new ByteCodeObjectData(unit.getName(), unit.getByteCode())));
        return compilationUnits;
    }

    public static List<ByteCodeObjectData> byteCodeObjectToByteCodeObjectData(List<ByteCodeObject> thriftByteCodeObjects) {
        List<ByteCodeObjectData> compilationUnits = new ArrayList<>();
        thriftByteCodeObjects.forEach(unit -> compilationUnits.add(new ByteCodeObjectData(unit.getName(), unit.getByteCode())));
        return compilationUnits;
    }


    public static Character toCharacter(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.length() > 1) {
                throw new IllegalArgumentException(String.format("Unsupported string length: %s, string: %s", stringValue.length(), stringValue));
            }
            return stringValue.charAt(0);
        }
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }

    public static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return Double.parseDouble((String) value);
        } else if (value instanceof Float) {
            String stringValue = GeneralConverter.toString(value);
            return GeneralConverter.toDouble(stringValue);
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return null;
    }

    public static Double toDouble(Object value, Locale locale, String doubleFormat) throws ConverterException {
        if (value == null) {
            return null;
        }
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
        return null;
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Constants.LOCALE);

        if (value instanceof String) {
            return createBigDecimal((String) value);
        }

        if (value instanceof Double) {
            Double doubleValue = (Double) value;
            String text = GeneralConverter.toString(Math.abs(doubleValue));
            int integerPlaces = text.indexOf(symbols.getDecimalSeparator());
            int decimalPlaces = text.length() - integerPlaces - 1;
            return new BigDecimal(doubleValue, new MathContext(decimalPlaces));
        }

        if (value instanceof Long) {
            return new BigDecimal((Long) value);
        }
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
        for (byte b : bytes) {
            stringBuilder.append(b).append(delimiter);
        }
        return stringBuilder.toString();
    }

    public static byte[] toByteArrayLittleEndian(Object object, int arraySize) {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }
        if (object.getClass().equals(Integer.class)) {
            return ByteBuffer.allocate(arraySize).order(ByteOrder.LITTLE_ENDIAN).putInt((Integer) object).array();
        } else if (object.getClass().equals(Long.class)) {
            return ByteBuffer.allocate(arraySize).order(ByteOrder.LITTLE_ENDIAN).putLong((Long) object).array();
        } else if (object.getClass().equals(Short.class)) {
            return ByteBuffer.allocate(arraySize).order(ByteOrder.LITTLE_ENDIAN).putShort((Short) object).array();
        } else if (object.getClass().equals(Byte.class)) {
            return ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN).put((Byte) object).array();
        } else if (object instanceof byte[]) {
            return ByteBuffer.allocate(arraySize).order(ByteOrder.LITTLE_ENDIAN).put((byte[]) object).array();
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
            buffer.putLong((Long) value);
            return buffer.array();
        }
        throw new ConverterException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));

    }

    public static BitSet toBitSet(Object value) {
        if (value instanceof byte[]) {
            BitSet bits = new BitSet();
            byte[] bytes = (byte[]) value;
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
        } else if (value instanceof Integer) {
            BitSet bits = new BitSet();
            long intValue = (int) value;
            int index = 0;
            while (intValue != 0) {
                if (intValue % 2 != 0) {
                    bits.set(index);
                }
                ++index;
                intValue = intValue >>> 1;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported type of value: %s", value.getClass().getSimpleName()));
    }


    public static MethodDescription convertMethodDataToMethodDescription(MethodDescriptionData data) {
        List<MethodArgument> methodArgumentList = new ArrayList<>();
        for (MethodArgumentData arg : data.args) {
            List<Annotation> annotationList = getAnnotations(arg.annotations);
            methodArgumentList.add(new MethodArgument(arg.returnType, arg.name, annotationList));

        }
        return new MethodDescription(data.returnType, data.name, methodArgumentList, getAnnotations(data.annotations));
    }

    public static List<Annotation> getAnnotations(List<AnnotationData> annotations) {
        List<Annotation> annotationList = new ArrayList<>();
        for (AnnotationData annotation : annotations) {
            annotationList.add(new Annotation(annotation.name, annotation.arguments));
        }
        return annotationList;
    }

    private static TypeParser typeParser = TypeParser.newBuilder().build();

    public static Object createObjectFromString(String value, Class<?> classType) {
        return typeParser.parse(value, classType);
    }
}
