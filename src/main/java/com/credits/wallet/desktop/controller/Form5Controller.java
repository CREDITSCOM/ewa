package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

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
        AppState.account=txPublic.getText();

        if (chSavePublicKey.isSelected()) {
            try {
                // Save account
                FileInputStream fis = new FileInputStream("settings.properties");
                Properties property = new Properties();
                property.load(fis);
                FileOutputStream fos = new FileOutputStream("settings.properties");
                property.setProperty("public.key",txPublic.getText());
                property.store(fos, "");
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setVisible(!AppState.newAccount);

        try {
            FileInputStream fis = new FileInputStream("settings.properties");
            Properties property = new Properties();
            property.load(fis);

            String publicKey = property.getProperty("public.key");
            if (publicKey!=null)
                txPublic.setText(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
