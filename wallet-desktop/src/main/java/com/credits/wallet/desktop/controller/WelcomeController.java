package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class WelcomeController implements Initializable {

    @FXML
    BorderPane bp;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    FormUtils.resizeForm(bp);    }

}
