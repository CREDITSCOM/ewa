package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Utils;
import com.credits.wallet.desktop.utils.Converter;
import com.credits.wallet.desktop.utils.Ed25519;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form5Controller extends Controller implements Initializable {
    @FXML
    private Button btnBack;

    @FXML
    private TextField txKey;

    @FXML
    private TextField txPublic;

    @FXML
    private CheckBox chSavePublicKey;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form0.fxml", "Wallet");
    }

    @FXML
    private void handleOpen() {
        AppState.account = txPublic.getText();

        if (chSavePublicKey.isSelected()) {
            try {
                // Save account
                FileInputStream fis = new FileInputStream("settings.properties");
                Properties property = new Properties();
                property.load(fis);
                FileOutputStream fos = new FileOutputStream("settings.properties");
                property.setProperty("public.key", txPublic.getText());
                property.store(fos, "");
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (AppState.newAccount) {
            // Transaction from credits
            try {
                Utils.prepareAndCallTransactionFlow(
                        Const.CREDIT_ACCOUNT,
                        txPublic.getText(),
                        Const.NEW_ACCOUNT_TRANSACTION_AMOUNT,
                        Const.NEW_ACCOUNT_TRANSACTION_COIN
                );
            } catch (Exception e) {
                e.printStackTrace();
                Utils.showError("Error creating transaction " + e.toString());
                return;
            }
        } else {
            try {
                byte[] publicKeyByteArr = Converter.decodeFromBASE64(txPublic.getText());
                byte[] privateKeyByteArr = Converter.decodeFromBASE64(txKey.getText());
                AppState.publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
                AppState.privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
            } catch (Exception e) {
                Utils.showError(e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setVisible(!AppState.newAccount);
        txPublic.setDisable(AppState.newAccount);

        if (AppState.newAccount) {
            txKey.setText(Converter.encodeToBASE64(AppState.privateKey.getEncoded()));
            txPublic.setText(Converter.encodeToBASE64(AppState.publicKey.getEncoded()));
        } else {
            try {
                FileInputStream fis = new FileInputStream("settings.properties");
                Properties property = new Properties();
                property.load(fis);

                String publicKey = property.getProperty("public.key");
                if (publicKey != null) {
                    txPublic.setText(publicKey);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
