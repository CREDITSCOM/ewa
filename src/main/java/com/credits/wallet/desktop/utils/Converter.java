package com.credits.wallet.desktop.utils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Base64;
import java.util.Locale;

/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
public class Converter {

    public static final String DOUBLE_FORMAT = "0.##";

    public static String toString(Object value) {
        if (value instanceof Double) {
            //Locale locale = new Locale("en", "UK");
            Locale locale=Locale.getDefault();
            NumberFormat nf = NumberFormat.getNumberInstance(locale);
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
        // TODO Добавить Date и т.д.
        return null;
    }

    public static Double toDouble(String value) throws ParseException {
        //Locale locale = new Locale("en", "UK");
        Locale locale=Locale.getDefault();
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern(DOUBLE_FORMAT);
        return (Double)df.parse(value);
    }

    public static String encodeToBASE64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] decodeFromBASE64(String string) throws IOException {
        return Base64.getDecoder().decode(string);
    }

}
