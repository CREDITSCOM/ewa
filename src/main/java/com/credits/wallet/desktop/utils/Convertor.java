package com.credits.wallet.desktop.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
public class Convertor {

    public static String DOUBLE_FORMAT = "#############0.#######################";

    public static String toString(Object value) {
        if (value instanceof Double) {

            if (value == null) {
                return "0.00";
            }
            Locale locale = new Locale("en", "UK");
            NumberFormat nf = NumberFormat.getNumberInstance(locale);
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern(DOUBLE_FORMAT);
            return df.format(value);
        }
        // TODO Добавить Integer, Date и т.д.
        return null;
    }
}
