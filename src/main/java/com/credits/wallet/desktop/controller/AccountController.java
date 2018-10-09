package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.CoinsUtils;
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
        VistaNavigator.loadVista("/fxml/welcome.fxml");
    }

    @FXML
    private void handleDetails() {
        AppState.newAccount = false;
        VistaNavigator.loadVista("/fxml/history.fxml");
    }

    @FXML
    private void handleAddCoin() {
        VistaNavigator.loadVista(VistaNavigator.NEW_COIN);
    }

    @FXML
    private void handleSmartContract() {
        AppState.newAccount = false;
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT);
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
