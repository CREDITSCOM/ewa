package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class TransactionController extends Controller implements Initializable {
    private static final String ERR_GETTING_TRANSACTION = "Error getting transaction details";

    @FXML
    private Label labInnerId;
    @FXML
    private Label labTarget;
    @FXML
    private Label labCurrency;
    @FXML
    private Label labAmount;

    @FXML
    private void handleBack() {
        if (AppState.detailFromHistory)
            App.showForm("/fxml/history.fxml", "Wallet");
        else
            App.showForm("/fxml/form8.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labInnerId.setText(AppState.selectedTransactionRow.getInnerId());
        labTarget.setText(AppState.selectedTransactionRow.getTarget());
        labCurrency.setText(AppState.selectedTransactionRow.getCurrency());
        labAmount.setText(AppState.selectedTransactionRow.getAmount());
    }
}
