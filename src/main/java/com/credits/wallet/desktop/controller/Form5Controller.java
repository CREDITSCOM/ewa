package com.credits.wallet.desktop.controller;

import javafx.fxml.FXML;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form5Controller extends Controller {
    @FXML
    private void handleBack() {
        app.showForm("/fxml/form0.fxml", "Wallet");
    }
}
