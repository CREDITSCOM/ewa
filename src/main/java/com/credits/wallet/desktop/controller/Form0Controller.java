package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class Form0Controller extends Controller {
    @FXML
    private void handleExistingAccount() {
        AppState.newAccount=false;
        App.showForm("/fxml/form5.fxml", "Wallet");
    }

    @FXML
    private void handleNewAccount() {
        AppState.newAccount=true;
        App.showForm("/fxml/form1.fxml", "Wallet");
    }
}
