package com.credits.general.util;

import com.credits.general.exception.CreditsException;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.credits.general.util.GeneralConverter.toInteger;
import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;


public class Utils {
    private static final String ALPHA_LOWER_CASE_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
    public static final ExecutorService threadPool = Executors.newCachedThreadPool();

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
        for (int i = 0; i < bytes.length; i++) {
            buf[i] = bytes[bytes.length - 1 - i];
        }
        return buf;
    }

    public static String getClassType(Object object) {
        return object == null ? "" : object.getClass().getTypeName();
    }

    public static String randomAlphaLowerCaseNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_LOWER_CASE_NUMERIC_STRING.length());
            builder.append(ALPHA_LOWER_CASE_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static boolean isEmpty(final Object object) {
        if (object instanceof String) {
            String objectString = (String) object;
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

    /**
     * Вычисляет Актуальное значение (которое будет вычислено в Ядре) предлагаемой пользователем максимальной комиссии
     * Возвращает пару:
     * - Отображаемое значение в десятичной системе счисления
     * - Значение, помещенное в 16 бит в объекте Short
     *
     * @param value
     * @return
     */
    public static Pair<Double, Short> calculateActualFee(Double value) {
        byte sign = (byte) (value < 0.0 ? 1 : 0); // sign
        int exp;   // exponent
        long frac; // mantissa
        value = Math.abs(value);
        double expf = value == 0.0 ? 0.0 : Math.log10(value);
        int expi = Objects.requireNonNull(toInteger(expf >= 0 ? expf + 0.5 : expf - 0.5), "can't convert expi to int");
        value /= Math.pow(10, expi);
        if (value >= 1.0) {
            value *= 0.1;
            ++expi;
        }
        exp = expi + 18;
        if (exp < 0 || exp > 28) {
            throw new CreditsException(String.format("exponent value %s out of range [0, 28]", exp));
        }
        frac = Math.round(value * 1024);
        double actualValue = (sign != 0 ? -1.0 : 1.0) * frac / 1024D * Math.pow(10.0, exp - 18.0);
        short actualValue16Bit = (short) (sign * 32768 + exp * 1024 + frac);
        return Pair.of(actualValue, actualValue16Bit);
    }


    public static void rethrowUnchecked(CheckedRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            rethrow(e);
        }
    }

    public interface CheckedRunnable {
        void run() throws Exception;
    }

    public static <R> R rethrowUnchecked(Callable<R> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            rethrow(e);
        }
        return null; //unreachable code
    }
}
