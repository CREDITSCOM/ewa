package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class TransactionController implements Initializable {
    private static final String ERR_GETTING_TRANSACTION = "Error getting transaction details";

    @FXML
    BorderPane bp;

    @FXML
    private TextField labInnerId;
    @FXML
    private TextField labSource;
    @FXML
    private TextField labTarget;
    @FXML
    private TextField labAmount;
    @FXML
    private TextField labState;

    @FXML
    private void handleBack() {
        if (AppState.detailFromHistory)
            VistaNavigator.loadVista(VistaNavigator.HISTORY);
        else
            VistaNavigator.loadVista(VistaNavigator.FORM_8);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);
        labInnerId.setText(String.valueOf(AppState.selectedTransactionRow.getInnerId()));
        labSource.setText(AppState.selectedTransactionRow.getSource());
        //labTarget.setText(AppState.selectedTransactionRow.getCurrency());
        labTarget.setText(AppState.selectedTransactionRow.getTarget());
        labAmount.setText(AppState.selectedTransactionRow.getAmount());
        labState.setText(AppState.selectedTransactionRow.getState());
    }
}
