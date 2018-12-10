package com.credits.general.util;

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
        // TODO validate expi or exp between 0 to 28
        exp = expi + 18;
        frac = Math.round(value * 1024);
        final double _1_1024 = 1.0 / 1024;
        return (sign != 0 ? -1.0 : 1.0) * frac * _1_1024 * Math.pow(10.0, exp - 18);
    }

}
