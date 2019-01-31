package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.SmartContractTransactionTabRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.Map;

public class SmartContractTransactionController extends AbstractController{
    private static final String ERR_GETTING_TRANSACTION = "Error getting transaction details";
    public static final int MAX_HEIGHT = 300;

    final int ROW_HEIGHT = 24;
    @FXML
    public HBox listContainer;

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
    private TextField labReturnedValue;

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT);
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {
        SmartContractTransactionTabRow selectedTransactionRow = (SmartContractTransactionTabRow) objects.get("selectedTransactionRow");

        labInnerId.setText(selectedTransactionRow.getBlockId());
        labSource.setText(selectedTransactionRow.getSource());
        labTarget.setText(selectedTransactionRow.getTarget());
        labAmount.setText(selectedTransactionRow.getAmount());
        labState.setText(selectedTransactionRow.getState());
        labMethod.setText(selectedTransactionRow.getMethod());
        ObservableList<String> items = FXCollections.observableArrayList();
        if (selectedTransactionRow.getParams() != null) {
            selectedTransactionRow.getParams().forEach(item -> items.add(item.getBoxedValue().toString()));
        }
        listParams.setItems(items);
        labReturnedValue.setText(selectedTransactionRow.getReturnedValue() == null ? "" : selectedTransactionRow.getReturnedValue().toString());
        int value = items.size() * ROW_HEIGHT + 2 > MAX_HEIGHT ? MAX_HEIGHT : items.size() * ROW_HEIGHT + 2;
        listContainer.setPrefHeight(value);

    }

    @Override
    public void formDeinitialize() {

    }
}
