package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 07.02.2018.
 */
public class NewCoinController extends Controller implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(NewCoinController.class);

    @FXML
    private TextField txToken;
    @FXML
    private TextField txCoin;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleSave() {
        String coin=txCoin.getText().replace(";","");
        String token=txToken.getText().replace(";", "");

        if (coin.isEmpty()) {
            Utils.showError("You must enter coin mnemonic");
            return;
        }

        if (token.isEmpty()) {
            Utils.showError("You must enter token");
            return;
        }

        if (AppState.coins.contains(coin)) {
            Utils.showError("Coin already exists");
            return;
        }

        String strToWrite=coin+";"+token;

        // Save to csv
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File("coins.csv"), true);
            bw = new BufferedWriter(fw);
            bw.write(strToWrite+"\n");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }

        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            String token = (String)clipboard.getData(DataFlavor.stringFlavor);
            txToken.setText(token);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
