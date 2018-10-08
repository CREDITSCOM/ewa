package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import javafx.fxml.FXML;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class Form0Controller extends Controller {
    @FXML
    private void handleExistingAccount() {
        AppState.newAccount = false;
        VistaNavigator.loadVista(VistaNavigator.FORM_5);
    }

    @FXML
    private void handleNewAccount() {
        AppState.newAccount = true;
        VistaNavigator.loadVista(VistaNavigator.FORM_1);
    }
}
