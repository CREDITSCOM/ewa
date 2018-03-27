package com.credits.wallet.desktop.controller;

import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import javafx.fxml.FXML;

import java.security.KeyPair;

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

        KeyPair keyPair = Ed25519.generateKeyPair();
        AppState.publicKey = keyPair.getPublic();
        AppState.privateKey = keyPair.getPrivate();

        App.showForm("/fxml/form4.fxml", "Wallet");
    }
}
