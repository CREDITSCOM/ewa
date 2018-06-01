package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.thread.GetBalanceUpdater;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class Utils {
    private static final String MSG_RETRIEVE_BALANCE = "Retrieving balance...";
    private static final int FRACTION_MAX_LENGTH = 4;

    private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static final String digits = "0123456789";

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

    public static void displayBalance(String coin, Label label) {
        label.setText(MSG_RETRIEVE_BALANCE);
        Thread getBalanceThread = new Thread(new GetBalanceUpdater(coin, label));
        getBalanceThread.start();
    }
}

