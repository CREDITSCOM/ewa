package com.credits.wallet.desktop.controller;

import com.credits.client.node.pojo.*;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.variant.VariantConverter;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.SmartContractTransactionTabRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;

public class SmartContractTransactionController extends AbstractController{
    private static final String ERR_GETTING_TRANSACTION = "Error getting transaction details";
    public static final int MAX_HEIGHT = 300;

    final int ROW_HEIGHT = 24;

    private SmartContractData selectedContract;
    private String selectedContractsTab;
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
    private ListView listSmartInfo;

    @FXML
    private void handleBack() {
        Map<String, Object> params = new HashMap<>();
        params.put(SmartContractController.SELECTED_CONTRACT_KEY, selectedContract);
        params.put(SmartContractController.SELECTED_CONTRACTS_TAB_KEY, selectedContractsTab);
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT, params);
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {
        selectedContract = (SmartContractData)objects.get(SmartContractController.SELECTED_CONTRACT_KEY);
        selectedContractsTab = (String)objects.get(SmartContractController.SELECTED_CONTRACTS_TAB_KEY);
        SmartContractTransactionTabRow selectedTransactionRow = (SmartContractTransactionTabRow) objects.get("selectedTransactionRow");

        labInnerId.setText(selectedTransactionRow.getBlockId());
        labSource.setText(selectedTransactionRow.getSource());
        labTarget.setText(selectedTransactionRow.getTarget());
        labAmount.setText(selectedTransactionRow.getAmount());
        labState.setText(selectedTransactionRow.getState());
        labMethod.setText(selectedTransactionRow.getMethod());
        ObservableList<String> items = FXCollections.observableArrayList();
        if (selectedTransactionRow.getParams() != null) {
            selectedTransactionRow.getParams().forEach(item -> items.add(item.getV_string()));
        }
        listParams.setItems(items);

        SmartTransInfoData smartInfo = selectedTransactionRow.getSmartInfo();

        if (smartInfo != null) {
            ObservableList<String> smartInfoItems = FXCollections.observableArrayList();
            if (smartInfo.isSmartDeploy()) {
                SmartDeployTransInfoData data = smartInfo.getSmartDeployTransInfoData();
                smartInfoItems.add(String.format("State: %s", data.getState().toString()));
            } else if (smartInfo.isSmartExecution()) {
                SmartExecutionTransInfoData data = smartInfo.getSmartExecutionTransInfoData();
                smartInfoItems.add(String.format("State: %s", data.getState().toString()));
                smartInfoItems.add(String.format("Method: %s", data.getMethod()));
                StringBuilder params = new StringBuilder();
                data.getParams().forEach(variant -> params.append(variant.getV_string() + "\n"));
                smartInfoItems.add(String.format("Params: %s", params.toString()));
            } else if (smartInfo.isSmartState()) {
                SmartStateTransInfoData data = smartInfo.getSmartStateTransInfoData();
                smartInfoItems.add(String.format("Is success: %s", data.isSuccess()));
                smartInfoItems.add(String.format("Execution fee: %s", data.getExecutionFee()));
                Variant returnedValue = data.getReturnValue();
                if (returnedValue != null && !returnedValue.isSetV_void()) {
                    smartInfoItems.add(String.format("Return value: %s", VariantConverter.toObject(returnedValue).toString()));
                }
            }
            listSmartInfo.setItems(smartInfoItems);
        }
        int value = items.size() * ROW_HEIGHT + 2 > MAX_HEIGHT ? MAX_HEIGHT : items.size() * ROW_HEIGHT + 2;
        listContainer.setPrefHeight(value);

    }

    @Override
    public void formDeinitialize() {

    }
}
