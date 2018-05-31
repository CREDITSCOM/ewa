package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;

public class NumberUtils {
    private static final int FRACTION_MAX_LENGTH = 18;

    private static final String digits = "0123456789";

    //TODO: this function is ugly and has to be rewritten ASAP!
    public static String correctNum(String s) {
        int i = 0;
        boolean wasPoint = false;
        while (i < s.length()) {
            String c = s.substring(i, i + 1);
            if (!(c.equals(AppState.decimalSeparator) && !wasPoint) && !digits.contains(c)) {
                if (i == 0 && s.length() == 1)
                    s = "";
                else if (i == 0)
                    s = s.substring(1);
                else if (i == s.length()-1)
                    s = s.substring(0, i);
                else
                    s = s.substring(0, i) + s.substring(i + 1);
            } else
                i++;

            if (c.equals(AppState.decimalSeparator))
                wasPoint = true;
        }

        // limit 18 positions after point
        int indPoint = s.indexOf(AppState.decimalSeparator);
        if (indPoint == 0) {
            s = "0" + s;
            indPoint = 1;
        }
        if (indPoint > 0) {
            String fract = s.substring(indPoint + 1);
            if (fract.length() > FRACTION_MAX_LENGTH)
                s = s.substring(0, s.length() - fract.length() + FRACTION_MAX_LENGTH);
        }

        return s;
    }
}
