package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.FormUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form5Controller extends Controller implements Initializable {
    private static Logger LOGGER = LoggerFactory.getLogger(Form5Controller.class);

    private static final String ERR_KEYS="Public and private keys pair is not valid";

    @FXML
    private Button btnBack;

    @FXML
    private Button btnUpload;

    @FXML
    private TextField txKey;

    @FXML
    private TextField txPublic;

    @FXML
    private Label labelError;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form0.fxml", "Wallet");
    }

    @FXML
    private void handleOpen() {
        open(txPublic.getText(), txKey.getText());
    }

    @FXML
    private void handleSaveKeys() throws WalletDesktopException {
        FileChooser fileChooser = new FileChooser();
        File defaultDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(defaultDirectory);
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            PrintWriter writer;
            try {
                writer = new PrintWriter(file.getAbsolutePath(), "UTF-8");
                String json = String.format("{\"key\":{\"public\":\"%s\",\"private\":\"%s\"}}",
                    Converter.encodeToBASE58(Ed25519.publicKeyToBytes(AppState.publicKey)),
                    Converter.encodeToBASE58(Ed25519.privateKeyToBytes(AppState.privateKey)));
                writer.println(json);
                writer.close();
                FormUtils.showInfo(String.format("Keys successfully saved in \n\n%s", file.getAbsolutePath()));
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                throw new WalletDesktopException(e);
            }
        }
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        File defaultDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(defaultDirectory);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String keyFileContent;
            try {
                byte[] keyFileBytes = Files.readAllBytes(file.toPath());
                keyFileContent = new String(keyFileBytes);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                FormUtils.showError("Error reading keys from file " + file.getAbsolutePath() + " " + e.toString());
                return;
            }
            JsonParser jsonParser = new JsonParser();
            JsonObject jObject = jsonParser.parse(keyFileContent).getAsJsonObject();
            JsonObject key = jObject.getAsJsonObject("key");
            String pubKey = key.get("public").getAsString();
            String privKey = key.get("private").getAsString();

            open(pubKey, privKey);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clearLabErr();

        btnBack.setVisible(!AppState.newAccount);
        btnUpload.setVisible(!AppState.newAccount);
        txPublic.setDisable(AppState.newAccount);
        txKey.setDisable(AppState.newAccount);

        if (AppState.newAccount) {
            txKey.setText(Converter.encodeToBASE58(Ed25519.privateKeyToBytes(AppState.privateKey)));
            txPublic.setText(Converter.encodeToBASE58(Ed25519.publicKeyToBytes(AppState.publicKey)));
        }

        try {
            handleSaveKeys();
        } catch (WalletDesktopException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void open(String pubKey, String privKey) {
        clearLabErr();

        AppState.account = pubKey;
        if (AppState.newAccount) {
            //TODO: if there is no need to make a system transaction separately we should remove this code and refactor the method
            //            try {
            //                ApiUtils.execSystemTransaction(pubKey);
            //            } catch (Exception e) {
            //                LOGGER.error(e.getMessage(), e);
            //                FormUtils.showError("Error creating transaction " + e.toString());
            //                //return;
            //            }
        } else {
            try {
                byte[] publicKeyByteArr = Converter.decodeFromBASE58(pubKey);
                byte[] privateKeyByteArr = Converter.decodeFromBASE58(privKey);
                AppState.publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
                AppState.privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
            } catch (CreditsCommonException e) {
                if (e.getMessage() != null) {
                    labelError.setText(e.getMessage());
                    txKey.setStyle(txKey.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
                    txPublic.setStyle(txPublic.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
                }
                LOGGER.error(e.getMessage(), e);
                //return;
            }
        }

        if (validateKeys(pubKey, privKey)) {
            App.showForm("/fxml/form6.fxml", "Wallet");
        } else {
            if (labelError.getText().isEmpty())
                labelError.setText(ERR_KEYS);
            else
                labelError.setText(ERR_KEYS + "; " + labelError.getText());
            txKey.setStyle(txKey.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            txPublic.setStyle(txPublic.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
        }
    }

    private boolean validateKeys(String publicKey, String privateKey) {
        byte[] publicKeyByteArr;
        byte[] privateKeyByteArr;
        try {
            publicKeyByteArr = Converter.decodeFromBASE58(publicKey);
            privateKeyByteArr = Converter.decodeFromBASE58(privateKey);
        } catch (CreditsException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }

        if (privateKeyByteArr.length <= 32) {
            return false;
        }

        for (int i = 0; i < publicKeyByteArr.length && i < privateKeyByteArr.length - 32; i++) {
            if (publicKeyByteArr[i] != privateKeyByteArr[i + 32]) {
                return false;
            }
        }

        return true;
    }

    private void clearLabErr() {
        labelError.setText("");

        txKey.setStyle(txKey.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        txPublic.setStyle(txPublic.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
    }
}
