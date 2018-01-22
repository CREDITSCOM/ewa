package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form5Controller extends Controller implements Initializable {
    @FXML
    private Button btnBack;

    @FXML
    private void handleBack() {
        app.showForm("/fxml/form0.fxml", "Wallet");
    }

    @FXML
    private void handleOpen() {
        app.showForm("/fxml/form6.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setVisible(!AppState.newAccount);
    }
}
