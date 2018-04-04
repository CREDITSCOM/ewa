package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
    private Button btnUpload;

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
        open(txPublic.getText(), txKey.getText());
    }

    @FXML
    private void handleSaveKeys() throws WalletDesktopException {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose folder");
        File defaultDirectory = new File(System.getProperty("user.dir"));
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(null);
        if (selectedDirectory != null) {

            PrintWriter writer = null;
            try {
                String filePath = String.format("%s\\%s", selectedDirectory.getAbsolutePath(), "wallet-keys.txt");
                writer = new PrintWriter(
                        filePath,
                        "UTF-8"
                );

                //writer.println(String.format("Public: %s", Converter.encodeToBASE64(Ed25519.publicKeyToBytes(AppState.publicKey))));
                //writer.println(String.format("Private: %s", Converter.encodeToBASE64(Ed25519.privateKeyToBytes(AppState.privateKey))));
                Map<String, String> content = new HashMap<>();
                content.put("public", Converter.encodeToBASE64(Ed25519.publicKeyToBytes(AppState.publicKey)));
                content.put("private", Converter.encodeToBASE64(Ed25519.privateKeyToBytes(AppState.privateKey)));
                writer.println(new Gson().toJson(content));

                writer.close();
                Utils.showInfo(String.format("Keys successfully saved in \n\n%s", filePath));
            } catch (FileNotFoundException e) {
                throw new WalletDesktopException(e);
            } catch (UnsupportedEncodingException e) {
                throw new WalletDesktopException(e);
            }
        }
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        File defaultDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(defaultDirectory);
        fileChooser.setInitialFileName("wallet-keys.txt");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                InputStream is = new FileInputStream(file);
                BufferedReader buf = new BufferedReader(new InputStreamReader(is));
                String line = buf.readLine();
                StringBuilder sb = new StringBuilder();
                while (line != null) {
                    sb.append(line);
                    line = buf.readLine();
                }
                JsonObject jObject = new JsonParser().parse(sb.toString()).getAsJsonObject();
                String pubKey=jObject.get("public").getAsString();
                String privKey=jObject.get("private").getAsString();

                open(pubKey, privKey);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                Utils.showError("Error reading keys from file " + file.getAbsolutePath() + " " + e.toString());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setVisible(!AppState.newAccount);
        btnSaveKeys.setVisible(AppState.newAccount);
        btnUpload.setVisible(!AppState.newAccount);
        txPublic.setDisable(AppState.newAccount);
        txKey.setDisable(AppState.newAccount);

        if (AppState.newAccount) {
            txKey.setText(Converter.encodeToBASE64(Ed25519.privateKeyToBytes(AppState.privateKey)));
            txPublic.setText(Converter.encodeToBASE64(Ed25519.publicKeyToBytes(AppState.publicKey)));
        }
    }

    private void open (String pubKey, String privKey) {
        AppState.account = pubKey;
        if (AppState.newAccount) {
            // Создание системной транзакции
            try {
                ApiUtils.execSystemTransaction(pubKey);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                Utils.showError("Error creating transaction " + e.toString());
                //return;
            }
        } else {
            try {
                byte[] publicKeyByteArr = Converter.decodeFromBASE64(pubKey);
                byte[] privateKeyByteArr = Converter.decodeFromBASE64(privKey);
                AppState.publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
                AppState.privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
            } catch (Exception e) {
                if (e.getMessage() != null)
                    Utils.showError(e.getMessage());
                LOGGER.error(e.getMessage(), e);
                //return;
            }
        }

        if (validateKeys(pubKey, privKey))
            App.showForm("/fxml/form6.fxml", "Wallet");
        else
            Utils.showError("Public and private keys pair is not valid");
    }

    private boolean validateKeys(String publicKey, String privateKey) {
        try {
            byte[] publicKeyByteArr = Converter.decodeFromBASE64(publicKey);
            byte[] privateKeyByteArr = Converter.decodeFromBASE64(privateKey);

            if (privateKeyByteArr.length <= 32)
                return false;

            for (int i = 0; i < publicKeyByteArr.length && i < privateKeyByteArr.length - 32; i++) {
                if (publicKeyByteArr[i] != privateKeyByteArr[i + 32])
                    return false;
            }

            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }
}
