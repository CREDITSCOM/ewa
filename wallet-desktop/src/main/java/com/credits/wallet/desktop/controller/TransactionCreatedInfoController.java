package com.credits.wallet.desktop.controller;

import com.credits.general.util.Converter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.TransactionTabRow;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class TransactionCreatedInfoController implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionCreatedInfoController.class);

    @FXML
    BorderPane bp;

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
            } catch (URISyntaxException | IOException e) {
                LOGGER.error("failed!", e);
                FormUtils.showError(e.getMessage());
            }
        }
    }

    @FXML
    private void handleOk() {
        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @FXML
    private void handleView() {
        TransactionTabRow transactionTabRow = new TransactionTabRow();

        transactionTabRow.setTarget(AppState.toAddress);
        transactionTabRow.setCurrency((byte)1);
        transactionTabRow.setAmount(Converter.toString(AppState.amount));
        transactionTabRow.setInnerId(Converter.toString(AppState.walletLastTransactionIdCache.get(AppState.account)));
        AppState.selectedTransactionRow = transactionTabRow;
        AppState.detailFromHistory=false;
        VistaNavigator.loadVista(VistaNavigator.TRANSACTION);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);
        this.toAddress.setText(AppState.toAddress);
        this.amountInCs.setText(Converter.toString(AppState.amount) + " " + AppState.coin);

    }
}
