package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.common.utils.sourcecode.SourceCodeUtils;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.data.SmartContractInvocationData;
import com.credits.leveldb.client.util.ApiClientUtils;
import com.credits.leveldb.client.util.TransactionTypeEnum;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.ContactSaver;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SmartContractUtils;
import com.credits.wallet.desktop.utils.Utils;
import com.credits.wallet.desktop.utils.struct.TransactionStruct;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller implements Initializable {

    private static final String PERSONAL_CONTRACTS = "Personal contracts";
    private static final String FOUND_CONTRACTS = "Found contracts";
    private static final String SMART_CONTRACTS = "Smart contracts";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);


    @FXML
    private Pane pControls;

    @FXML
    private TextField tfAddress;

    @FXML
    private ComboBox<MethodDeclaration> cbMethods;

    @FXML
    private TextField tfSearchAddress;

    @FXML
    private StackPane pCodePanel;

    @FXML
    private ScrollPane spCodePanel;

    @FXML
    private TreeView<Label> tvContracts;

    @FXML
    private AnchorPane pParams;

    @FXML
    private AnchorPane pParamsContainer;

    private CodeArea codeArea;

    private SmartContractData currentSmartContract;

    private MethodDeclaration currentMethod;

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
        String address = tfSearchAddress.getText();
        try {
            SmartContractData smartContractData =
                AppState.apiClient.getSmartContract(Converter.decodeFromBASE58(address));
            saveInSmartContractTree(smartContractData);
            this.refreshFormState(smartContractData);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            FormUtils.showError(e.getMessage());
        }
    }

    private void saveInSmartContractTree(SmartContractData smartContractData) {
        String contractAddress = Converter.encodeToBASE58(smartContractData.getAddress());

        Map<String, TreeItem<Label>> rootItemMap = new HashMap<>();
        this.tvContracts.getRoot().getChildren().forEach((p) -> rootItemMap.put(p.getValue().getText(), p));

        TreeItem<Label> foundContractsList = rootItemMap.get(FOUND_CONTRACTS);
        boolean newRoot = false;
        if (foundContractsList == null) {
            foundContractsList = new TreeItem<>(new Label(FOUND_CONTRACTS));
            newRoot = true;
        }
        TreeItem<Label> personalCoontractsList = rootItemMap.get(PERSONAL_CONTRACTS);

        if (notElementInList(contractAddress, personalCoontractsList)) {
            if (notElementInList(contractAddress, foundContractsList)) {
                Label label = new Label(contractAddress);
                setSmartContractLabelEventOnClick(smartContractData, label);
                foundContractsList.getChildren().add(new TreeItem<>(label));
                if(newRoot) {
                    this.tvContracts.getRoot().getChildren().add(foundContractsList);
                }
            }
            Map<String, SmartContractData> map = new HashMap<>();
            foundContractsList.getChildren().forEach((k) -> map.put(k.getValue().getText(), smartContractData));
            ContactSaver.serialize(map);
        }
    }

    private boolean notElementInList(String element, TreeItem<Label> coontractsRootItem) {
        return coontractsRootItem.getChildren().stream().noneMatch((el) -> el.getValue().getText().equals(element));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.codeArea = SmartContractUtils.initCodeArea(this.pCodePanel);
        initSmartContractTree();
        this.codeArea.setEditable(false);
        this.codeArea.copy();
    }

    private void initSmartContractTree() {
        TreeItem<Label> rootItem = new TreeItem<>(new Label(SMART_CONTRACTS));
        TreeItem<Label> smartContractRootItem = new TreeItem<>(new Label(PERSONAL_CONTRACTS));
        TreeItem<Label> foundContractRootItem = new TreeItem<>(new Label(FOUND_CONTRACTS));
        try {
            this.refreshFormState(null);
            List<SmartContractData> smartContracts =
                AppState.apiClient.getSmartContracts(Converter.decodeFromBASE58(AppState.account));
            smartContracts.forEach(smartContractData -> {

                Label label = new Label(Converter.encodeToBASE58(smartContractData.getAddress()));
                setSmartContractLabelEventOnClick(smartContractData, label);
                smartContractRootItem.getChildren().add(new TreeItem<>(label));
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            FormUtils.showError(e.getMessage());
        }
        Map<String, SmartContractData> map = ContactSaver.deserialize();
        rootItem.getChildren().add(smartContractRootItem);
        if (map != null && map.size() > 0) {
            map.forEach((k, v) -> {
                Label label = new Label(k);
                setSmartContractLabelEventOnClick(v, label);
                foundContractRootItem.getChildren().add(new TreeItem<>(label));
            });
            rootItem.getChildren().add(foundContractRootItem);
        }
        this.tvContracts.setShowRoot(false);
        this.tvContracts.setRoot(rootItem);
    }

    private void refreshFormState(SmartContractData smartContractData) throws WalletDesktopException {
        if (smartContractData == null || smartContractData.getHashState().isEmpty() ||
            smartContractData.getAddress().length == 0) {
            this.pControls.setVisible(false);
            this.spCodePanel.setVisible(false);
        } else {
            this.pControls.setVisible(true);
            this.spCodePanel.setVisible(true);
            this.currentSmartContract = smartContractData;
            String sourceCode = smartContractData.getSourceCode();
            this.tfAddress.setText(Converter.encodeToBASE58(smartContractData.getAddress()));
            List<MethodDeclaration> methods = SourceCodeUtils.parseMethods(sourceCode);
            cbMethods.getItems().clear();
            methods.forEach(method -> {
                method.setBody(null);
                cbMethods.getItems().add(method);
            });
            this.codeArea.clear();
            this.codeArea.replaceText(0, 0, SourceCodeUtils.formatSourceCode(sourceCode));
        }
    }

    @FXML
    private void cbMethodsOnAction() {
        this.pParams.setVisible(false);
        this.currentMethod = this.cbMethods.getSelectionModel().getSelectedItem();
        if (this.currentMethod == null) {
            return;
        }
        List<SingleVariableDeclaration> params = SourceCodeUtils.getMethodParameters(this.currentMethod);
        this.pParamsContainer.getChildren().clear();
        if (params.size() > 0) {
            this.pParams.setVisible(true);
            double layoutY = 10;
            for (SingleVariableDeclaration param : params) {
                TextField paramValueTextField = new TextField();
                paramValueTextField.setLayoutX(150);
                paramValueTextField.setLayoutY(layoutY);
                paramValueTextField.setStyle(
                    "-fx-background-color:  #fff; -fx-border-width: 1; -fx-border-color:  #000; -fx-font-size: 16px");
                paramValueTextField.setPrefSize(225, 56);
                Label paramNameLabel = new Label(param.toString());
                paramNameLabel.setLayoutX(10);
                paramNameLabel.setLayoutY(layoutY + 15);
                paramNameLabel.setStyle("-fx-font-size: 18px");
                paramNameLabel.setLabelFor(paramValueTextField);
                this.pParamsContainer.getChildren().addAll(paramNameLabel, paramValueTextField);
                layoutY += 70;
            }
            this.pParamsContainer.setPrefHeight(layoutY);
        }
    }

    @FXML
    private void handleExecute() {
        try {
            String method = cbMethods.getSelectionModel().getSelectedItem().getName().getIdentifier();
            List<String> params = new ArrayList<>();
            List<SingleVariableDeclaration> currentMethodParams =
                SourceCodeUtils.getMethodParameters(this.currentMethod);
            ObservableList<Node> paramsContainerChildren = this.pParamsContainer.getChildren();

            int i = 0;
            for (Node node : paramsContainerChildren) {
                if (node instanceof TextField) {
                    SingleVariableDeclaration variableDeclaration = currentMethodParams.get(i);
                    String className = SourceCodeUtils.parseClassName(variableDeclaration);
                    String paramValue = ((TextField) node).getText();
                    String paramValueProcessed =
                        SourceCodeUtils.processSmartContractMethodParameterValue(className, paramValue);

                    params.add(paramValueProcessed);

                    ++i;
                }
            }

            // 2DO Select param type
            /*
            List<Variant> varParams = new ArrayList<>();
            for (String p : params) {
                Variant var = new Variant();
                var.setV_string(p);
                varParams.add(var);
            }
            */

            long transactionId = ApiUtils.generateTransactionInnerId();
            SmartContractData smartContractData = this.currentSmartContract;

            SmartContractInvocationData smartContractInvocationData =
                new SmartContractInvocationData("", new byte[0], smartContractData.getHashState(), method, params,
                    false);

            byte[] scBytes = ApiClientUtils.serializeByThrift(smartContractInvocationData);
            TransactionStruct tStruct = new TransactionStruct(transactionId, AppState.account,
                Converter.encodeToBASE58(this.currentSmartContract.getAddress()), new BigDecimal(0), new BigDecimal(0),
                (byte) 1, scBytes);
            ByteBuffer signature = Utils.signTransactionStruct(tStruct);

            ApiResponseData apiResponseData =
                AppState.apiClient.executeSmartContract(transactionId, Converter.decodeFromBASE58(AppState.account),
                    this.currentSmartContract.getAddress(), smartContractInvocationData, signature.array(),
                    TransactionTypeEnum.EXECUTE_SMARTCONTRACT);
        } catch (CreditsException e) {
            LOGGER.error(e.toString(), e);
            Utils.showError(e.toString());
        }
    }

    @FXML
    private void handleFavorite() {

    }

    private void setSmartContractLabelEventOnClick(SmartContractData smartContractData, Label label) {
        label.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                if (event.getClickCount() == 2) {
                    try {
                        this.refreshFormState(smartContractData);
                    } catch (WalletDesktopException e) {
                        LOGGER.error(e.getMessage(), e);
                        FormUtils.showError(e.getMessage());
                    }
                }
            }
        });
    }

}