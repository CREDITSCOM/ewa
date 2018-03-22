package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.Utils;
import com.credits.wallet.desktop.utils.Converter;
import com.credits.wallet.desktop.utils.Ed25519;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form5Controller extends Controller implements Initializable {
    private static Logger LOGGER = LoggerFactory.getLogger(Form5Controller.class);

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
            // Создание системной транзакции
            try {
                ApiUtils.prepareAndCallTransactionFlowSystem(txPublic.getText());
            } catch (Exception e) {
                e.printStackTrace();
                Utils.showError("Error creating transaction " + e.toString());
                //return;
            }
        } else {
            try {
                byte[] publicKeyByteArr = Converter.decodeFromBASE64(txPublic.getText());
                byte[] privateKeyByteArr = Converter.decodeFromBASE64(txKey.getText());
                AppState.publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
                AppState.privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
            } catch (Exception e) {
                if (e.getMessage()!=null)
                    Utils.showError(e.getMessage());
                e.printStackTrace();
                //return;
            }
        }

        if (validateKeys(txPublic.getText(), txKey.getText()))
            App.showForm("/fxml/form6.fxml", "Wallet");
        else
            Utils.showError("Public and private keys pair is not valid");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setVisible(!AppState.newAccount);
        txPublic.setDisable(AppState.newAccount);
        txKey.setDisable(AppState.newAccount);

        if (AppState.newAccount) {
            txKey.setText(Converter.encodeToBASE64(Ed25519.privateKeyToBytes(AppState.privateKey)));
            txPublic.setText(Converter.encodeToBASE64(Ed25519.publicKeyToBytes(AppState.publicKey)));
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

    private boolean validateKeys(String publicKey, String privateKey) {
        try {
            byte[] publicKeyByteArr = Converter.decodeFromBASE64(publicKey);
            byte[] privateKeyByteArr = Converter.decodeFromBASE64(privateKey);

            if (privateKeyByteArr.length<=32)
                return false;

            for (int i=0; i<publicKeyByteArr.length && i<privateKeyByteArr.length-32; i++) {
                if (publicKeyByteArr[i]!=privateKeyByteArr[i+32])
                    return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
