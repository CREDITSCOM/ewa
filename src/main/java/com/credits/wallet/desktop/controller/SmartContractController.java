package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.common.utils.sourcecode.SourceCodeUtils;
import com.credits.leveldb.client.callback.Callback;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.data.SmartContractInvocationData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.util.ApiAlertUtils;
import com.credits.leveldb.client.util.ApiClientUtils;
import com.credits.thrift.generated.Variant;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.ContactSaver;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SmartContractUtils;
import com.credits.wallet.desktop.utils.Utils;
import com.credits.wallet.desktop.utils.struct.CalcTransactionIdSourceTargetResult;
import com.credits.wallet.desktop.utils.struct.TransactionStruct;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
                AppState.levelDbService.getSmartContract(Converter.decodeFromBASE58(address));
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
                if (newRoot) {
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
                AppState.levelDbService.getSmartContracts(Converter.decodeFromBASE58(AppState.account));
            smartContracts.forEach(smartContractData -> {

                Label label = new Label(Converter.encodeToBASE58(smartContractData.getAddress()));
                label.setPadding(new Insets(0, 0, 0, -20));
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
                paramValueTextField.setPrefSize(200, 30);
                Label paramNameLabel = new Label(param.toString());
                paramNameLabel.setLayoutX(10);
                paramNameLabel.setLayoutY(layoutY + 15);
                paramNameLabel.setStyle("-fx-font-size: 18px");
                paramNameLabel.setLabelFor(paramValueTextField);
                this.pParamsContainer.getChildren().addAll(paramNameLabel, paramValueTextField);
                layoutY += 40;
            }
            this.pParamsContainer.setPrefHeight(layoutY);
        }
    }

    @FXML
    private void handleExecute() {
        try {
            String method = cbMethods.getSelectionModel().getSelectedItem().getName().getIdentifier();
            List<Object> params = new ArrayList<>();
            List<SingleVariableDeclaration> currentMethodParams =
                SourceCodeUtils.getMethodParameters(this.currentMethod);

            ObservableList<Node> paramsContainerChildren = this.pParamsContainer.getChildren();

            int i = 0;
            for (Node node : paramsContainerChildren) {
                if (node instanceof TextField) {
                    String paramValue = ((TextField) node).getText();

                    SingleVariableDeclaration variableDeclaration = currentMethodParams.get(i);

                    String className = SourceCodeUtils.parseClassName(variableDeclaration);

                    params.add(SourceCodeUtils.createVariantObject(className, paramValue));

                    ++i;
                }

            }

            SmartContractData smartContractData = this.currentSmartContract;

            executeSmartContractProcess(method, params, smartContractData, new Callback() {
                @Override
                public void onSuccess(ApiResponseData resultData) {
                    Variant res = resultData.getScExecRetVal();
                    if (res != null) {
                        String retVal = res.toString() + '\n';
                        LOGGER.info("Return value is {}", retVal);
                        ApiAlertUtils.showAlertInfo(
                            "Execute smart contract was success: return value is: " + retVal);
                    } else {
                        ApiAlertUtils.showAlertInfo("Execute smart contract was success");
                    }

                }

                @Override
                public void onError(Exception e) {
                    FormUtils.showError(e.getMessage());
                }
            });
        } catch (CreditsException e) {
            LOGGER.error(e.toString(), e);
            FormUtils.showError(e.toString());
        }
    }

    public static void executeSmartContractProcess(String method, List<Object> params,
        SmartContractData smartContractData, Callback callback)
        throws LevelDbClientException, CreditsCommonException, CreditsNodeException {
        SmartContractInvocationData smartContractInvocationData =
            new SmartContractInvocationData("", new byte[0], smartContractData.getHashState(), method, params, false);

        byte[] scBytes = ApiClientUtils.serializeByThrift(smartContractInvocationData);
        CalcTransactionIdSourceTargetResult calcTransactionIdSourceTargetResult =
            ApiUtils.calcTransactionIdSourceTarget(AppState.account,
                Converter.encodeToBASE58(smartContractData.getAddress()));

        TransactionStruct tStruct = new TransactionStruct(calcTransactionIdSourceTargetResult.getTransactionId(),
            calcTransactionIdSourceTargetResult.getSource(), calcTransactionIdSourceTargetResult.getTarget(),
            new BigDecimal(0), new BigDecimal(0), (byte) 1, scBytes);

        ByteBuffer signature = Utils.signTransactionStruct(tStruct);

        AppState.levelDbService.executeSmartContract(calcTransactionIdSourceTargetResult.getTransactionId(),
            calcTransactionIdSourceTargetResult.getSource(), calcTransactionIdSourceTargetResult.getTarget(),
            smartContractInvocationData, signature.array(), callback);
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