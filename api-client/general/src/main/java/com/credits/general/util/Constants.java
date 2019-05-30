package com.credits.general.util;

import java.text.DecimalFormatSymbols;
import java.util.Locale;


public class Constants {
    public static final Locale LOCALE = Locale.getDefault();
    public static final String DECIMAL_SEPARATOR = Character.toString(new DecimalFormatSymbols(LOCALE).getDecimalSeparator());
}
