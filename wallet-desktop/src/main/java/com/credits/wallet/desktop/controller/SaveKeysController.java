package com.credits.wallet.desktop.controller;

import com.credits.client.node.crypto.Ed25519;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class SaveKeysController implements FormInitializable {
    @FXML
    private TextField txKey;

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.FORM_1,this);
    }

    @FXML
    private void handleContinue() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("isNewAccount","true");
        VistaNavigator.loadVista(VistaNavigator.FORM_5, params, this);
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {
        txKey.setText(GeneralConverter.encodeToBASE58(Ed25519.privateKeyToBytes(AppState.privateKey)));
    }
}
