package com.credits.wallet.desktop.controller;

import com.credits.client.node.crypto.Ed25519;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.crypto.sodium.SodiumLibrary;
import com.credits.wallet.desktop.utils.crypto.sodium.SodiumLibraryException;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static com.credits.wallet.desktop.AppState.*;
import static java.nio.charset.StandardCharsets.UTF_8;


public class PutKeysController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(PutKeysController.class);

    private static final String ERROR_EMPTY_PUBLIC = "Public key is empty";
    private static final String ERROR_EMPTY_PRIVATE = "Private key is empty";

    static final String NONCE_KEY = "nonce";
    static final String SALT_KEY = "salt";
    static final String ENCRYPTED_PRIVKEY_KEY = "encryptedPrivKey";
    static final String PUBKEY_KEY = "pubKey";

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
        VistaNavigator.loadVista(VistaNavigator.WELCOME);
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
            SodiumLibrary.initSodium();
            byte[] salt = SodiumLibrary.randomBytes(SodiumLibrary.cryptoPwhashSaltBytes());

            // create salt for derive key from pass phrase
            byte[] key = new byte[0];
            try {
                key = SodiumLibrary.cryptoPwhashArgon2i(AppState.getPwd().getBytes(UTF_8), salt);
            } catch (SodiumLibraryException e) {
                FormUtils.showError(e.getMessage());
            }
            // create nonce for encrypting private key
            byte[] nonce = SodiumLibrary.randomBytes(SodiumLibrary.cryptoSecretBoxNonceBytes().intValue());

            ;
            try(PrintWriter writer = new PrintWriter(file.getAbsolutePath(), UTF_8)){
                byte[] encryptedPrivateKey = SodiumLibrary.cryptoSecretBoxEasy(Ed25519.privateKeyToBytes(getPrivateKey()), nonce, key);

                String json = String.format("{\"key\":{\"public\":\"%s\",\"private\":\"%s\", \"nonce\":\"%s\",\"salt\":\"%s\"}}",
                        GeneralConverter.encodeToBASE58(Ed25519.publicKeyToBytes(getPublicKey())),
                        GeneralConverter.encodeToBASE58(encryptedPrivateKey),
                        GeneralConverter.encodeToBASE58(nonce),
                        GeneralConverter.encodeToBASE58(salt));
                writer.println(json);
                writer.close();
                FormUtils.showInfo(String.format("Keys successfully saved in %n%n%s", file.getAbsolutePath()));
            } catch (SodiumLibraryException | IOException e) {
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

            byte[] privKeyBytes = GeneralConverter.decodeFromBASE58(privKey);

            if (privKeyBytes.length != 64) {
                String nonce = key.get("nonce").getAsString();
                String salt = key.get("salt").getAsString();

                Map<String, Object> params = new HashMap<>();
                params.put(PutKeysController.NONCE_KEY, nonce);
                params.put(PutKeysController.SALT_KEY, salt);
                params.put(PutKeysController.ENCRYPTED_PRIVKEY_KEY, privKey);
                params.put(PutKeysController.PUBKEY_KEY, pubKey);
                VistaNavigator.loadVista(VistaNavigator.CHECK_PRIVATE_KEY, params);
            } else {
                open(pubKey, privKey);
            }
        }
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {
        clearLabErr();
        if (objects != null && objects.get("isNewAccount") != null) {
            hideElementsForUploadExistAccount();

            privateKeyField.setText(GeneralConverter.encodeToBASE58(Ed25519.privateKeyToBytes(getPrivateKey())));
            publicKeyField.setText(GeneralConverter.encodeToBASE58(Ed25519.publicKeyToBytes(getPublicKey())));
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

        setSession(pubKey);
        try {
            byte[] privateKeyByteArr = GeneralConverter.decodeFromBASE58(privKey);
            setPrivateKey(Ed25519.bytesToPrivateKey(privateKeyByteArr));
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
            setPublicKey(Ed25519.bytesToPublicKey(publicKeyByteArr));
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
            VistaNavigator.loadVista(VistaNavigator.WALLET);
        }
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

    @Override
    public void formDeinitialize() {

    }
}
