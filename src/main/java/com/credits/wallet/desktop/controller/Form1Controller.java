package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import javafx.fxml.FXML;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form1Controller extends Controller {
    @FXML
    private void handleBack() {
        App.showForm("/fxml/form0.fxml", "Wallet");
    }

    @FXML
    private void handleGenerate() {
        App.showForm("/fxml/form4.fxml", "Wallet");
    }
}
