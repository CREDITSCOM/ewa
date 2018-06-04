package com.credits.wallet.desktop.controller;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller implements Initializable {

    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);

    @FXML
    Label address;

    @FXML
    private TextField txAddress;

    @FXML
    private TextArea taCode;

    @FXML
    private TreeView<Label> contractsTree;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleCreate() {
        App.showForm("/fxml/smart_contract_deploy.fxml", "Wallet");
    }

    @FXML
    private void handleSearch() {
        String address = txAddress.getText();
        try {
            SmartContractData smartContractData = AppState.apiClient.getSmartContract(address);

            this.address.setText(address);
            this.taCode.setText(smartContractData.getSourceCode());

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            Utils.showError(String.format("Error %s", e.getMessage()));
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        this.contractsTree.setRoot(null);

        //2DO
        //AppState.apiClient.
    }

    @FXML
    private void panelCodeKeyReleased() {
        refreshClassMembersTree();
    }

    private void refreshClassMembersTree() {
    }
}