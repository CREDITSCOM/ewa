package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.VistaNavigator;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import java.util.Map;


public class WelcomeController extends AbstractController {

    public BorderPane bp;

    @FXML
    private void handleExistingAccount() {
        VistaNavigator.loadVista(VistaNavigator.FORM_5);
    }

    @FXML
    private void handleNewAccount() {
        VistaNavigator.loadVista(VistaNavigator.FORM_1);
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {
    }

    @Override
    public void formDeinitialize() {

    }
}
