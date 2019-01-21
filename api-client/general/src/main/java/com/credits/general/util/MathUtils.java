package com.credits.general.util;

import com.credits.general.exception.CreditsException;

public class MathUtils {

    public static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static double calcActualFee(Double value) {
        byte sign; // sign
        int exp;   // exponent
        long frac; // mantissa
        sign = (byte) (value < 0.0 ? 1 : 0);
        value = Math.abs(value);
        double expf = value == 0.0 ? 0.0 : Math.log10(value);
        int expi = GeneralConverter.toInteger(expf >= 0 ? expf + 0.5 : expf - 0.5);
        value /= Math.pow(10, expi);
        if (value >= 1.0) {
            value *= 0.1;
            ++expi;
        }
        exp = expi + 18;
        if (exp < 0 || exp > 28) {//todo добавить обработку exception + convertацию в short
            throw new CreditsException(String.format("exp value %s out of range [0, 28]", exp));
        }
        frac = Math.round(value * 1024);
        final double _1_1024 = 1.0 / 1024;
        return (sign != 0 ? -1.0 : 1.0) * frac * _1_1024 * Math.pow(10.0, exp - 18);
    }

}
