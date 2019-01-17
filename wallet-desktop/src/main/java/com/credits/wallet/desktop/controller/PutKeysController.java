package com.credits.wallet.desktop.controller;

import com.credits.client.node.crypto.Ed25519;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.client.node.util.ObjectKeeper;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.FormUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
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
import java.nio.file.Files;
import java.util.Map;

import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.coinsKeeper;
import static com.credits.wallet.desktop.AppState.favoriteContractsKeeper;
import static com.credits.wallet.desktop.AppState.privateKey;
import static com.credits.wallet.desktop.AppState.publicKey;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class PutKeysController implements FormInitializable {
    private static Logger LOGGER = LoggerFactory.getLogger(PutKeysController.class);

    private static final String ERROR_EMPTY_PUBLIC = "Public key is empty";
    private static final String ERROR_EMPTY_PRIVATE = "Private key is empty";

    @FXML
    private Button btnBack;

    @FXML
    private Button btnUpload;

    @FXML
    private Label lblUpload;

    @FXML
    private TextField privateKeyField;

    @FXML
    private TextField publicKeyField;

    @FXML
    private Label privateKeyErrorLabel;

    @FXML
    private Label publicKeyErrorLabel;

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.WELCOME, this);
    }

    @FXML
    private void handleOpen() {
        open(publicKeyField.getText(), privateKeyField.getText());
    }

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
                    GeneralConverter.encodeToBASE58(Ed25519.publicKeyToBytes(publicKey)),
                    GeneralConverter.encodeToBASE58(Ed25519.privateKeyToBytes(privateKey)));
                writer.println(json);
                writer.close();
                FormUtils.showInfo(String.format("Keys successfully saved in %n%n%s", file.getAbsolutePath()));
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                throw new WalletDesktopException(e);
            }
        }
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        File defaultDirectory = new File(System.getProperty("user.dir"));
        System.out.println("default directory - " + defaultDirectory);
        if (defaultDirectory.exists()) {
            fileChooser.setInitialDirectory(defaultDirectory);
        }
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String keyFileContent;
            try {
                byte[] keyFileBytes = Files.readAllBytes(file.toPath());
                keyFileContent = new String(keyFileBytes);
            } catch (IOException e) {
                LOGGER.error("failed!", e);
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
    public void initializeForm(Map<String, Object> objects) {
        clearLabErr();
        if (objects != null && objects.get("isNewAccount") != null) {
            hideElementsForUploadExistAccount();

            privateKeyField.setText(GeneralConverter.encodeToBASE58(Ed25519.privateKeyToBytes(privateKey)));
            publicKeyField.setText(GeneralConverter.encodeToBASE58(Ed25519.publicKeyToBytes(publicKey)));
            try {
                handleSaveKeys();
            } catch (WalletDesktopException e) {
                LOGGER.error("failed!", e);
            }
        }

    }

    private void hideElementsForUploadExistAccount() {
        btnBack.setVisible(false);
        btnUpload.setVisible(false);
        lblUpload.setVisible(false);
        publicKeyField.setEditable(false);
        privateKeyField.setEditable(false);
    }

    private void open(String pubKey, String privKey) {
        clearLabErr();

        boolean empty = false;
        if (privKey.isEmpty()) {
            privateKeyErrorLabel.setText(ERROR_EMPTY_PRIVATE);
            empty = true;
        } else {
            privateKeyErrorLabel.setText("");
        }

        if (pubKey.isEmpty()) {
            publicKeyErrorLabel.setText(ERROR_EMPTY_PUBLIC);
            empty = true;
        } else {
            publicKeyErrorLabel.setText("");
        }
        if (empty) {
            return;
        }

        initStaticData(pubKey);
        try {
            byte[] privateKeyByteArr = GeneralConverter.decodeFromBASE58(privKey);
            privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                privateKeyErrorLabel.setText(e.getMessage());
            } else {
                privateKeyErrorLabel.setText("Private key error");
            }
            LOGGER.error("failed!", e);
            //return;
        }
        try {
            byte[] publicKeyByteArr = GeneralConverter.decodeFromBASE58(pubKey);
            publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                publicKeyErrorLabel.setText(e.getMessage());
            } else {
                privateKeyErrorLabel.setText("Public key error");
            }
            LOGGER.error("failed!", e);
            //return;
        }

        if (validateKeys(pubKey, privKey)) {
            VistaNavigator.loadVista(VistaNavigator.WALLET, this);
        }
    }

    private void initStaticData(String pubKey) {
        account = pubKey;
        NodeApiServiceImpl.account = pubKey;
        if (favoriteContractsKeeper != null) {
            favoriteContractsKeeper.flush();
        }
        favoriteContractsKeeper = new ObjectKeeper<>(account, "favorite");
        if (coinsKeeper != null) {
            coinsKeeper.flush();
        }
        coinsKeeper = new ObjectKeeper<>(account, "coins");
    }

    private boolean validateKeys(String publicKey, String privateKey) {
        byte[] publicKeyByteArr;
        byte[] privateKeyByteArr;
        try {
            publicKeyByteArr = GeneralConverter.decodeFromBASE58(publicKey);
            privateKeyByteArr = GeneralConverter.decodeFromBASE58(privateKey);
        } catch (CreditsException e) {
            LOGGER.error("failed!", e);
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
        FormUtils.clearErrorOnField(publicKeyField, publicKeyErrorLabel);
        FormUtils.clearErrorOnField(privateKeyField, privateKeyErrorLabel);
    }

}
