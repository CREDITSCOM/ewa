package com.credits.wallet.desktop.controller;

import com.credits.client.node.crypto.Ed25519;
import com.credits.general.util.Converter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form4Controller extends Controller implements Initializable {
    @FXML
    private TextField txKey;

    @FXML
    BorderPane bp;

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.FORM_1);
    }

    @FXML
    private void handleContinue() {
        VistaNavigator.loadVista(VistaNavigator.FORM_5);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    FormUtils.resizeForm(bp);        txKey.setText(Converter.encodeToBASE58(Ed25519.privateKeyToBytes(AppState.privateKey)));
    }
}
