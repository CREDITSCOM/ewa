package com.credits.wallet.desktop.controller;

import com.credits.client.node.crypto.Ed25519;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.security.KeyPair;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class GenerateKeysController implements Initializable {
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
    public void initialize(URL location, ResourceBundle resources) {  //fixme jumping fields
        FormUtils.resizeForm(bp);

        txPassword.setVisible(true);
        labPassword.setVisible(false);

        labPassword.setPrefWidth(txPassword.getPrefWidth());
        labPassword.setPrefHeight(txPassword.getPrefHeight());
        labPassword.setLayoutX(txPassword.getLayoutX());
        labPassword.setLayoutY(txPassword.getLayoutY());

        btnShowPassword.setOnMousePressed(event -> {
            labPassword.setText(txPassword.getText());
            txPassword.setVisible(false);
            labPassword.setVisible(true);
        });

        btnShowPassword.setOnMouseReleased(event -> {
            txPassword.setVisible(true);
            labPassword.setVisible(false);
        });

        btnShowPassword.setOnMouseExited(event -> {
            txPassword.setVisible(true);
            labPassword.setVisible(false);
        });
    }
}

