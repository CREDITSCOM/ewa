package com.credits.wallet.desktop.controller;

import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.security.KeyPair;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form1Controller extends Controller implements Initializable {
    @FXML
    Button btnShowPassword;

    @FXML
    PasswordField txPassword;

    @FXML
    Label labPassword;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txPassword.setVisible(true);
        labPassword.setVisible(false);

        btnShowPassword.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                labPassword.setText(txPassword.getText());
                txPassword.setVisible(false);
                labPassword.setVisible(true);
            }
        });

        btnShowPassword.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                txPassword.setVisible(true);
                labPassword.setVisible(false);
            }
        });

        btnShowPassword.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                txPassword.setVisible(true);
                labPassword.setVisible(false);
            }
        });
    }
}

