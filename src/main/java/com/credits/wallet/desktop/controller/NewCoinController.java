package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 07.02.2018.
 */
public class NewCoinController extends Controller implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(NewCoinController.class);

    private static final String ERR_COIN = "You must enter coin mnemonic";
    private static final String ERR_TOKEN = "You must enter token";
    private static final String ERR_COIN_DUPLICATE = "Coin already exists";

    @FXML
    private TextField txToken;
    @FXML
    private TextField txCoin;

    @FXML
    private Label labelErrorToken;
    @FXML
    private Label labelErrorCoin;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleSave() {
        clearLabErr();

        String coin = txCoin.getText().replace(";", "");
        String token = txToken.getText().replace(";", "");

        // VALIDATE
        boolean isValidationSuccessful = true;
        if (coin.isEmpty()) {
            labelErrorCoin.setText(ERR_COIN);
            txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (token.isEmpty()) {
            labelErrorToken.setText(ERR_TOKEN);
            txToken.setStyle(txToken.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (AppState.coins.contains(coin)) {
            labelErrorCoin.setText(ERR_COIN_DUPLICATE);
            txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (!isValidationSuccessful) {
            return;
        }

        String strToWrite = coin + ";" + token;

        // Save to csv
        try {
            Path coinsPath = Paths.get("coins.csv");
            List<String> strings = Collections.singletonList(strToWrite + "\n");
            Files.write(coinsPath, strings, StandardCharsets.UTF_8, StandardOpenOption.APPEND,
                StandardOpenOption.CREATE);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clearLabErr();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            String token = (String) clipboard.getData(DataFlavor.stringFlavor);
            txToken.setText(token);
        } catch (UnsupportedFlavorException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void clearLabErr() {
        labelErrorToken.setText("");
        labelErrorCoin.setText("");

        txToken.setStyle(txToken.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
    }
}
