package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form4Controller extends Controller implements Initializable {
    @FXML
    private TextField txKey;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form1.fxml", "Wallet");
    }

    @FXML
    private void handleContinue() {
        App.showForm("/fxml/form5.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txKey.setText(Converter.encodeToBASE58(Ed25519.privateKeyToBytes(AppState.privateKey)));
    }
}
