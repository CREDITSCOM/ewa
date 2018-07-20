package com.credits.wallet.desktop.controller;


import com.credits.common.utils.Converter;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class Form7Controller extends Controller implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(Form7Controller.class);

    @FXML
    private Label toAddress;

    @FXML
    private Label amountInCs;

    @FXML
    private Label transactionFeeValue;

    @FXML
    private void handleBack() {
        AppState.noClearForm6 = true;
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleGenerate() {
        try {
            //ApiUtils.callTransactionFlow(AppState.innerId, AppState.account, AppState.toAddress, AppState.amount,
            //    AppState.balance, AppState.coin, AppState.transactionFeeValue);
            ApiUtils.callTransactionFlow(AppState.innerId, AppState.account, AppState.toAddress, AppState.amount,
                AppState.balance, (byte)1, AppState.transactionFeeValue);
        } catch (LevelDbClientException e) {
            LOGGER.error(AppState.NODE_ERROR + ": " + e.getMessage(), e);
            FormUtils.showError(AppState.NODE_ERROR);
            return;
        } catch (CreditsNodeException e) {
            LOGGER.error(AppState.NODE_ERROR + ": " + e.getMessage(), e);
            FormUtils.showError(AppState.NODE_ERROR);
            return;
        }

        App.showForm("/fxml/form8.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.toAddress.setText(AppState.toAddress);
        this.amountInCs.setText(Converter.toString(AppState.amount) + " " + AppState.coin);
    }

}
