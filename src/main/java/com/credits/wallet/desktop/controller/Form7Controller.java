package com.credits.wallet.desktop.controller;


import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.Utils;
import com.credits.wallet.desktop.utils.Converter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class Form7Controller extends Controller implements Initializable {

    @FXML
    private Label toAddress;

    @FXML
    private Label amountInCs;

    @FXML
    private Label transactionFeeValue;

    @FXML
    private Label transactionFee;

    @FXML
    private void handleBack() {
        AppState.noClearForm6=true;
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleGenerate() {
        try {
            AppState.transactionId=null;
            AppState.transactionId=ApiUtils.prepareAndCallTransactionFlow(
                    AppState.account,
                    AppState.toAddress,
                    AppState.amount,
                    AppState.coin
            );

            // create fee transaction
            ApiUtils.prepareAndCallTransactionFlow(
                    AppState.account,
                    Const.FEE_TRAN_TARGET,
                    Const.FEE_TRAN_AMOUNT,
                    Const.FEE_TRAN_CURRENCY
            );
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showError("Error creating transaction " + e.toString());
            return;
        }

        App.showForm("/fxml/form8.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.toAddress.setText(AppState.toAddress);
        this.amountInCs.setText(Converter.toString(AppState.amount) + " CS");
        this.transactionFeeValue.setText(Converter.toString(AppState.transactionFeeValue) + " CS");
        this.transactionFee.setText(Converter.toString(AppState.transactionFeePercent) + " %");
    }

}
