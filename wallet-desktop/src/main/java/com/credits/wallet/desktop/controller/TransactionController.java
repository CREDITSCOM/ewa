package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class TransactionController implements Initializable {
    private static final String ERR_GETTING_TRANSACTION = "Error getting transaction details";
    public static final int MAX_HEIGHT = 300;

    final int ROW_HEIGHT = 24;
    @FXML
    public HBox listContainer;

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
    private TextField labMethod;
    @FXML
    private ListView listParams;

    @FXML
    private void handleBack() {
        if (AppState.detailFromHistory) {
            VistaNavigator.loadVista(VistaNavigator.HISTORY);
        } else {
            VistaNavigator.loadVista(VistaNavigator.FORM_8);
        }
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
        labMethod.setText(AppState.selectedTransactionRow.getMethod());
        ObservableList<String> items = FXCollections.observableArrayList();
        AppState.selectedTransactionRow.getParams().forEach(item -> items.add(item.toString()));
        listParams.setItems(items);
        int value = items.size() * ROW_HEIGHT + 2 > MAX_HEIGHT ? MAX_HEIGHT : items.size() * ROW_HEIGHT + 2;
        listContainer.setPrefHeight(value);

    }
}
