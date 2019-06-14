package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class NumberUtilsTest {

    private TextField textField;
    private List<Character> notDigits = Arrays.asList('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+',
                                                      '!', '\"', '№', ';', '%', ':', '?', '*', 'a', 'k', 'z', 'а', 'к', 'я');

    @Before
    public void setUp() {
        try {
            JFrame frame = new JFrame("FX");
            final JFXPanel fxPanel = new JFXPanel();
            frame.add(fxPanel);
            frame.setVisible(true);

        } catch (Exception e) {
            Assume.assumeNoException("Unable to open DISPLAY", e);
        }

        textField = new TextField();
    }


    @Test
    public void correctNumTest() {
        //Checking removing non-digit characters
        notDigits.forEach(character -> {
            String text = String.valueOf(character);
            textField.setText(text);
            NumberUtils.correctNum(text, textField);
        });
        Assert.assertEquals("", textField.getText());

        //Checking adding zero if first char is separator
        textField.setText(AppState.decimalSeparator);
        NumberUtils.correctNum(AppState.decimalSeparator, textField);
        Assert.assertEquals("0" + AppState.decimalSeparator, textField.getText());

        //Checking removing second or more separator
        String secSepText = "123" + AppState.decimalSeparator;
        textField.setText(secSepText + AppState.decimalSeparator);
        NumberUtils.correctNum(AppState.decimalSeparator, textField);
        Assert.assertEquals(secSepText, textField.getText());

        //Checking limiting length
        String additionalLengthChar = "1";
        String limitLengthText = "123" + AppState.decimalSeparator + "123456789012345678";
        textField.setText(limitLengthText + additionalLengthChar);
        NumberUtils.correctNum(additionalLengthChar, textField);
        Assert.assertEquals(limitLengthText, textField.getText());
    }
}
