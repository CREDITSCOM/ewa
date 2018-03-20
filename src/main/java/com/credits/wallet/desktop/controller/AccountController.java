package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.Utils;
import com.credits.wallet.desktop.utils.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.11.2017.
 */
public class AccountController extends Controller implements Initializable {
    private static final String ERR_GETTING_BALANCE = "Error getting balance";

    @FXML
    private Label wallet;

    @FXML
    private Label balance;

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

    public void initialize(URL location, ResourceBundle resources) {
        this.wallet.setText(AppState.account);

        try {
            Double balance=AppState.apiClient.getBalance(AppState.account, "cs");
            this.balance.setText(Converter.toString(balance));
        } catch (Exception e) {
            this.balance.setText("");
            e.printStackTrace();
            Utils.showError(ERR_GETTING_BALANCE);
        }
    }
}
