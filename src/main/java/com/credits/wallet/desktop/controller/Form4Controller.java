package com.credits.wallet.desktop.controller;

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
        app.showForm("/fxml/form1.fxml", "Wallet");
    }

    @FXML
    private void handleContinue() {
        app.showForm("/fxml/form5.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txKey.setText("2862190ef04ece53dc725e3b8bd6baaed98f00b204e9800998ecf8427ee866fb");
    }
}
