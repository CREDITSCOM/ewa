package com.credits.wallet.desktop.utils;

import com.credits.common.utils.Converter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.thread.GetBalanceThread;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class Utils {
    private static final String MSG_RETRIEVE_BALANCE = "Retrieving balance...";
    private static final int FRACTION_MAX_LENGTH=18;

    private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static final String digits="0123456789";

    public static void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Error");
        alert.setHeaderText("Error!");
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static void showInfo(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Information");
        alert.setHeaderText("Information");
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static String correctNum(String s) {
        int i=0;
        boolean wasPoint=false;
        while (i<s.length()) {
            String c=s.substring(i,i+1);
            if (!(c.equals(AppState.decSep) && !wasPoint) && !digits.contains(c)) {
                if (i==0 && s.length()==1)
                    s="";
                else if (i==0)
                    s=s.substring(1);
                else if (i==s.length()-1)
                    s=s.substring(0,i);
                else
                    s=s.substring(0,i)+s.substring(i+1);
            } else
                i++;

            if (c.equals(AppState.decSep))
                wasPoint=true;
        }

        // limit 18 positions after point
        int indPoint=s.indexOf(AppState.decSep);
        if (indPoint==0) {
            s = "0" + s;
            indPoint=1;
        }
        if (indPoint>0) {
            String fract=s.substring(indPoint+1);
            if (fract.length()>FRACTION_MAX_LENGTH)
                s=s.substring(0,s.length()-fract.length()+FRACTION_MAX_LENGTH);
        }

        return s;
    }

    public static void displayBalance(String coin, Label label) {
        label.setText(MSG_RETRIEVE_BALANCE);
        Thread getBalanceThread = new Thread(new GetBalanceThread(coin, label));
        getBalanceThread.start();
    }
}

