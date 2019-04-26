package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;
import javafx.scene.control.TextField;

public class NumberUtils {
    private static final int FRACTION_MAX_LENGTH = 18;

    private static final String digits = "0123456789";

    public static void correctNum(String c, TextField tf) {
        String s = tf.getText();
        // I. remove non-digits character
        if (!digits.contains(c) && !c.equals(AppState.decimalSeparator) && s.contains(c)) {
            int ind = s.indexOf(c);
            s = s.substring(0, ind) + s.substring(ind + 1);
            tf.setText(s);
            tf.positionCaret(s.length());
        }

        // II. Add 0 if first char is separator
        if (s.length() > 0 && s.substring(0, 1).equals(AppState.decimalSeparator)) {
            s = "0" + s;
            tf.setText(s);
            tf.positionCaret(s.length());
        }

        // III. remove second or more separator and limit length
        int ind1 = s.indexOf(AppState.decimalSeparator);
        if (ind1 >= 0) {
            int ind2 = s.indexOf(AppState.decimalSeparator, ind1 + 1);
            if (ind2 > 0) {
                s = s.substring(0, ind2) + s.substring(ind2 + 1);
                tf.setText(s);
                tf.positionCaret(s.length());
            }
            if (s.length() - ind1 > FRACTION_MAX_LENGTH) {
                s = s.substring(0, ind1 + FRACTION_MAX_LENGTH + 1);
                tf.setText(s);
                tf.positionCaret(s.length());
            }
        }
    }

    public static String getCorrectNum(String s) {
        if(s.isEmpty()) {
            return s;
        }
        // 0. Remove non digits characters
        s = s.replaceAll("[^\\d.,]", "");
        // I. Add 0 if first char is separator
        if (AppState.decimalSeparator.equals(".")) {
            s = s.replace(',', '.');
        } else if (AppState.decimalSeparator.equals(",")) {
            s = s.replace('.', ',');
        }
        // II. Add 0 if first char is separator
        if (s.length() > 0 && s.substring(0, 1).equals(AppState.decimalSeparator)) {
            s = "0" + s;
        }
        // III. remove second or more separator and limit length
        int ind1 = s.indexOf(AppState.decimalSeparator);
        if (ind1 >= 0) {
            int ind2 = s.indexOf(AppState.decimalSeparator, ind1 + 1);
            if (ind2 > 0) {
                s = s.substring(0, ind2) + s.substring(ind2 + 1);
            }
            if (s.length() - ind1 > FRACTION_MAX_LENGTH) {
                s = s.substring(0, ind1 + FRACTION_MAX_LENGTH + 1);
            }
        }
        return s;
    }
}
