package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form5Controller extends Controller implements Initializable {
    private static Logger LOGGER = LoggerFactory.getLogger(Form5Controller.class);

    @FXML
    private Button btnBack;

    @FXML
    private Button btnSaveKeys;

    @FXML
    private TextField txKey;

    @FXML
    private TextField txPublic;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form0.fxml", "Wallet");
    }

    @FXML
    private void handleOpen() {
        AppState.account = txPublic.getText();
        if (AppState.newAccount) {
            // Создание системной транзакции
            try {
                ApiUtils.prepareAndCallTransactionFlowSystem(txPublic.getText());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
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
                LOGGER.error(e.getMessage(), e);
                //return;
            }
        }

        if (validateKeys(txPublic.getText(), txKey.getText()))
            App.showForm("/fxml/form6.fxml", "Wallet");
        else
            Utils.showError("Public and private keys pair is not valid");
    }

    @FXML
    private void handleSaveKeys() throws WalletDesktopException {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose folder");
        File defaultDirectory = new File(System.getProperty("user.dir"));
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(null);
        if (selectedDirectory != null) {
            LOGGER.info("Folder = {}", selectedDirectory.getAbsolutePath());

            PrintWriter writer = null;
            try {
                String filePath = String.format("%s\\%s", selectedDirectory.getAbsolutePath(), "wallet-keys.txt");
                writer = new PrintWriter(
                        filePath,
                        "UTF-8"
                );

                writer.println(String.format("Public: %s", Converter.encodeToBASE64(Ed25519.publicKeyToBytes(AppState.publicKey))));
                writer.println(String.format("Private: %s", Converter.encodeToBASE64(Ed25519.privateKeyToBytes(AppState.privateKey))));

                writer.close();
                Utils.showInfo(String.format("Keys successfully saved in \n\n%s", filePath));
            } catch (FileNotFoundException e) {
                throw new WalletDesktopException(e);
            } catch (UnsupportedEncodingException e) {
                throw new WalletDesktopException(e);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setVisible(!AppState.newAccount);
        btnSaveKeys.setVisible(AppState.newAccount);
        txPublic.setDisable(AppState.newAccount);
        txKey.setDisable(AppState.newAccount);

        if (AppState.newAccount) {
            txKey.setText(Converter.encodeToBASE64(Ed25519.privateKeyToBytes(AppState.privateKey)));
            txPublic.setText(Converter.encodeToBASE64(Ed25519.publicKeyToBytes(AppState.publicKey)));
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
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }
}
