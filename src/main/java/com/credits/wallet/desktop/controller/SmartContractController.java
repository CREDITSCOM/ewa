package com.credits.wallet.desktop.controller;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.thrift.executor.APIResponse;
import com.credits.wallet.desktop.thrift.executor.ContractExecutor;
import com.credits.wallet.desktop.thrift.executor.ContractFile;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
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
    private TextField txSearchAddress;

    @FXML
    private TextArea taCode;

    @FXML
    private TextArea taABI;

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
        String address = txSearchAddress.getText();
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
    public void initialize(URL location, ResourceBundle resources) {
        TreeItem<Label> rootItem= new TreeItem<>(new Label("Smart contracts"));

        try {
            List<SmartContractData> smartContracts = AppState.apiClient.getSmartContracts(AppState.account);
            for (SmartContractData smartContractData : smartContracts) {
                rootItem.getChildren().add(new TreeItem<>(new Label(smartContractData.getHashState())));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            FormUtils.showError("Error getting Smart Contract List " + e.toString());
        }

        this.contractsTree.setRoot(rootItem);

        taCode.setDisable(true);
        taABI.setDisable(true);
    }

    @FXML
    private void panelCodeKeyReleased() {
        refreshClassMembersTree();
    }

    @FXML
    private void handleExecute() {

        // Call contract executor
        if (AppState.contractExecutorHost != null &&
                AppState.contractExecutorPort != null &&
                AppState.contractExecutorDir != null ) {
            try {
                TTransport transport;

                transport = new TSocket(AppState.contractExecutorHost, AppState.contractExecutorPort);
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                ContractExecutor.Client client = new ContractExecutor.Client(protocol);

                String address = this.address.getText();
                String method = "Method"; // TODO
                List<String> params = new ArrayList<>();
                params.add("par01");
                params.add("par02");

                LOGGER.info("Contract executor request: address = {}; method = {}; params = {}", address, method, params.toArray());

                APIResponse apiResponse = client.execute(address, method, params);

                LOGGER.info("Contract executor response: code = {}; message = {}", apiResponse.getCode(), apiResponse.getMessage());

                transport.close();
            } catch (Exception e) {
                e.printStackTrace();
                Utils.showError("Error executing smart contract " + e.toString());
            }
        }

    }

    private void refreshClassMembersTree() {
    }
}