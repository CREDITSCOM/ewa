package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import javafx.fxml.FXML;

import java.util.Map;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class WelcomeController implements FormInitializable {

    @FXML
    private void handleExistingAccount() {
        AppState.newAccount = false;
        VistaNavigator.loadVista(VistaNavigator.FORM_5,this);
    }

    @FXML
    private void handleNewAccount() {
        AppState.newAccount = true;
        VistaNavigator.loadVista(VistaNavigator.FORM_1,this);
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {

    }

}
