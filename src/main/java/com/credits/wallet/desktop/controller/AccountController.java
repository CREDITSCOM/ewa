package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.11.2017.
 */
public class AccountController extends Controller implements Initializable {
    private static final String ERR_GETTING_BALANCE = "Error getting balance";

    private final static Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @FXML
    private Label wallet;

    @FXML
    private Label balance;

    @FXML
    private void handleLogout() {
        App.showForm("/fxml/form0.fxml", "Wallet");
    }

    @FXML
    private void handleDetails() {
        AppState.newAccount = false;
        App.showForm("/fxml/history.fxml", "Wallet");
    }

    @FXML
    private void handleSmartContract() {
        AppState.newAccount = false;
        App.showForm("/fxml/smart_contract.fxml", "Wallet");
    }


    @FXML
    private void handleCopy() {
        StringSelection selection = new StringSelection(wallet.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public void initialize(URL location, ResourceBundle resources) {
        this.wallet.setText(AppState.account);

        try {
            Double balance=AppState.apiClient.getBalance(AppState.account, "cs");
            this.balance.setText(Converter.toString(balance));
        } catch (Exception e) {
            this.balance.setText("");
            LOGGER.error(ERR_GETTING_BALANCE, e);
            Utils.showError(ERR_GETTING_BALANCE);
        }
    }
}
