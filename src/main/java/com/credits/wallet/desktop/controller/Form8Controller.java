package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.struct.TransactionTabRow;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class Form8Controller extends Controller implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(Form8Controller.class);

    @FXML
    private Label toAddress;

    @FXML
    private Label amountInCs;

    @FXML
    private Label transactionFeeValue;

    @FXML
    private void handleSeeOnMonitor() {
        if (AppState.creditMonitorURL==null) {
            FormUtils.showError("URL for credit monitor not defined");
        } else {
            try {
                Desktop.getDesktop().browse(new URL(AppState.creditMonitorURL+AppState.account).toURI());
            } catch (URISyntaxException e) {
                LOGGER.error(e.getMessage(), e);
                FormUtils.showError(e.getMessage());
            } catch (MalformedURLException e) {
                LOGGER.error(e.getMessage(), e);
                FormUtils.showError(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                FormUtils.showError(e.getMessage());
            }
        }
    }

    @FXML
    private void handleOk() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleView() {
        TransactionTabRow transactionTabRow = new TransactionTabRow();

        transactionTabRow.setTarget(AppState.toAddress);
        transactionTabRow.setCurrency(AppState.coin);
        transactionTabRow.setAmount(Converter.toString(AppState.amount));
        transactionTabRow.setInnerId(AppState.innerId);
        AppState.selectedTransactionRow = transactionTabRow;
        AppState.detailFromHistory=false;
        App.showForm("/fxml/transaction.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.toAddress.setText(AppState.toAddress);
        this.amountInCs.setText(Converter.toString(AppState.amount) + " "+AppState.coin);
    }
}
