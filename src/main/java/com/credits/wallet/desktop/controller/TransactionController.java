package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
    private Label labTarget;
    @FXML
    private Label labCurrency;
    @FXML
    private Label labAmount;
    @FXML
    private Label labFee;
    @FXML
    private Label labTime;
    @FXML
    private Label labStatus;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/history.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labTarget.setText(AppState.selectedTransactionRow.getTarget());
        labCurrency.setText(AppState.selectedTransactionRow.getCurrency());
        labAmount.setText(AppState.selectedTransactionRow.getAmount());
        labFee.setText(AppState.selectedTransactionRow.getFee());
        labTime.setText(AppState.selectedTransactionRow.getTime());

        String transactionDetails = Utils.callAPI("gettransactioninfo?source=" + AppState.account +
            "&destination=" + AppState.selectedTransactionRow.getTarget() +
            "&amount=" + AppState.selectedTransactionRow.getAmount() +
            "&timestamp=" + AppState.selectedTransactionRow.getTimeN() +
            "&currency=" + AppState.selectedTransactionRow.getCurrency(), ERR_GETTING_TRANSACTION);
        if (transactionDetails != null) {
            JsonElement jelement = new JsonParser().parse(transactionDetails);
            String status = jelement.getAsJsonObject().get("response").getAsJsonObject().get("status").getAsString();
            labStatus.setText(status);
        }

    }
}
