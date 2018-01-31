package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import javafx.fxml.FXML;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class Form0Controller extends Controller {
    @FXML
    private void handleExistingAccount() {
        AppState.newAccount = false;
        App.showForm("/fxml/form5.fxml", "Wallet");
    }

    @FXML
    private void handleNewAccount() {
        AppState.newAccount = true;
        App.showForm("/fxml/form1.fxml", "Wallet");
    }
}
