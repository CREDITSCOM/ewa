package com.credits.wallet.desktop.controller;

import com.credits.client.node.crypto.Ed25519;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.Converter;
import com.credits.general.util.ObjectKeeper;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.FormUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
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
public class PutKeysController implements Initializable {
    private static Logger LOGGER = LoggerFactory.getLogger(PutKeysController.class);

    private static final String ERR_KEYS = "Public and private keys pair is not valid";
    private static final String ERROR_EMPTY_PUBLIC  = "Public key is empty";
    private static final String ERROR_EMPTY_PRIVATE = "Private key is empty";


    @FXML
    BorderPane bp;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnUpload;

    @FXML
    private Label lblUpload;

    @FXML
    private TextField txKey;

    @FXML
    private TextField txPublic;

    @FXML
    private Label errorLabelPrivate;

    @FXML
    private Label errorLabelPublic;

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.WELCOME);
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
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);
        clearLabErr();

        btnBack.setVisible(!AppState.newAccount);
        btnUpload.setVisible(!AppState.newAccount);
        lblUpload.setVisible(!AppState.newAccount);
        txPublic.setEditable(!AppState.newAccount);
        txKey.setEditable(!AppState.newAccount);

        if (AppState.newAccount) {
            txKey.setText(Converter.encodeToBASE58(Ed25519.privateKeyToBytes(AppState.privateKey)));
            txPublic.setText(Converter.encodeToBASE58(Ed25519.publicKeyToBytes(AppState.publicKey)));
            try {
                handleSaveKeys();
            } catch (WalletDesktopException e) {
                LOGGER.error("failed!", e);
            }
        }

    }

    private void open(String pubKey, String privKey) {
        clearLabErr();

        boolean empty = false;
        if (privKey.isEmpty()) {
            errorLabelPrivate.setText(ERROR_EMPTY_PRIVATE);
            empty = true;
        }
        else
            errorLabelPrivate.setText("");

        if (pubKey.isEmpty()) {
            errorLabelPublic.setText(ERROR_EMPTY_PUBLIC);
            empty = true;
        }
        else
            errorLabelPublic.setText("");
        if(empty) {
            return;
        }

        initStaticData(pubKey);
        try {
            byte[] privateKeyByteArr = Converter.decodeFromBASE58(privKey);
            AppState.privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
        } catch ( Exception e) {
            if (e.getMessage() != null) {
                errorLabelPrivate.setText(e.getMessage());
            } else {
                errorLabelPrivate.setText("Private key error");
            }
            LOGGER.error("failed!", e);
            //return;
        }
        try {
            byte[] publicKeyByteArr = Converter.decodeFromBASE58(pubKey);
            AppState.publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
        } catch ( Exception e) {
            if (e.getMessage() != null) {
                errorLabelPublic.setText(e.getMessage());
            } else {
                errorLabelPrivate.setText("Public key error");
            }
            LOGGER.error("failed!", e);
            //return;
        }

        if (validateKeys(pubKey, privKey)) {
            VistaNavigator.loadVista(VistaNavigator.WALLET);
        }
    }

    private void initStaticData(String pubKey) {
        AppState.account = pubKey;
        NodeApiServiceImpl.account = pubKey;
        AppState.smartContractsKeeper = new ObjectKeeper<>(AppState.account, "scobj");
    }

    private boolean validateKeys(String publicKey, String privateKey) {
        byte[] publicKeyByteArr;
        byte[] privateKeyByteArr;
        try {
            publicKeyByteArr = Converter.decodeFromBASE58(publicKey);
            privateKeyByteArr = Converter.decodeFromBASE58(privateKey);
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
        errorLabelPublic.setText("");
        errorLabelPrivate.setText("");

        txKey.setStyle(txKey.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        txPublic.setStyle(txPublic.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
    }
}
