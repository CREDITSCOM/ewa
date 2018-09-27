package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.thread.GetBalanceUpdater;
import com.credits.wallet.desktop.utils.CoinsUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
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
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @FXML
    private Label wallet;

    @FXML
    private ComboBox<String> cbCoinBalance;

    @FXML
    private Label lCoinBalance;

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
    private void handleAddCoin() {
        App.showForm("/fxml/new_coin.fxml", "Wallet");
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

    @FXML
    private void handleRefreshBalance() {
        CoinsUtils.displayBalance(cbCoinBalance,lCoinBalance);
    }

    public void initialize(URL location, ResourceBundle resources) {
        CoinsUtils.fillBalanceCombobox(cbCoinBalance,lCoinBalance);
        cbCoinBalance.getSelectionModel().select(0);
        this.wallet.setText(AppState.account);

    }

}
