package com.credits.wallet.desktop.controller;

import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

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
    BorderPane bp;

    @FXML
    PasswordField txPassword;

    @FXML
    Label labPassword;

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.WELCOME);
    }

    @FXML
    private void handleGenerate() {
        KeyPair keyPair = Ed25519.generateKeyPair();
        AppState.publicKey = keyPair.getPublic();
        AppState.privateKey = keyPair.getPrivate();

        VistaNavigator.loadVista(VistaNavigator.FORM_4);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);
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

