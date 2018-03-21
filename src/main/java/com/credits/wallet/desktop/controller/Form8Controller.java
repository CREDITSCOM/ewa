package com.credits.wallet.desktop.controller;

import com.credits.leveldb.client.TransactionData;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.struct.TransactionTabRow;
import com.credits.wallet.desktop.utils.Converter;
import com.credits.wallet.desktop.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class Form8Controller extends Controller implements Initializable {

    @FXML
    private Label toAddress;

    @FXML
    private Label amountInCs;

    @FXML
    private Label transactionFeeValue;

    @FXML
    private Label transactionFee;

    @FXML
    private void handleSeeOnMonitor() {
        App.showForm("/fxml/form0.fxml", "Wallet");
    }

    @FXML
    private void handleView() {
        if (AppState.transactionId==null) {
            Utils.showError("Transaction does not exists");
        } else {
            try {
                TransactionData transactionData = AppState.apiClient.getTransaction(AppState.transactionId);
                AppState.selectedTransactionRow=new TransactionTabRow();
                AppState.selectedTransactionRow.setAmount(transactionData.getAmount().toString());
                AppState.selectedTransactionRow.setCurrency(transactionData.getCurrency());
                AppState.selectedTransactionRow.setHash(transactionData.getHash());
                AppState.selectedTransactionRow.setId(transactionData.getInnerId());
                AppState.selectedTransactionRow.setTarget(transactionData.getTarget());
                App.showForm("/fxml/transaction.fxml", "Wallet");
            } catch (Exception e) {
                Utils.showError("Error "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.toAddress.setText(AppState.toAddress);
        this.amountInCs.setText(Converter.toString(AppState.amount) + " "+AppState.coin);
        this.transactionFeeValue.setText(Converter.toString(AppState.transactionFeeValue) + " "+AppState.coin);
        this.transactionFee.setText(Converter.toString(AppState.transactionFeePercent) + " %");
    }
}
