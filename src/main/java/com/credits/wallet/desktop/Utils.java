package com.credits.wallet.desktop;

import javafx.scene.control.Alert;
import javafx.stage.StageStyle;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class Utils {
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
            if (!(c.equals(AppState.decSep) && !wasPoint) && digits.indexOf(c)<0) {
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
        return s;
    }
}
