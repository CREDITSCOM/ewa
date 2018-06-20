package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.thrift.executor.APIResponse;
import com.credits.wallet.desktop.thrift.executor.ContractExecutor;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SimpleInMemoryCompiler;
import com.credits.wallet.desktop.utils.SourceCodeUtils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller implements Initializable {

    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);


    @FXML
    private Pane pControls;

    @FXML
    private Label lAddress;

    @FXML
    private ComboBox<MethodDeclaration> cbMethods;

    @FXML
    private TextField txSearchAddress;

    @FXML
    private AnchorPane pCodePanel;

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
            this.refreshFormState(smartContractData);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            FormUtils.showError(e.getMessage());
        }
    }

    private void refreshFormState(SmartContractData smartContractData) throws WalletDesktopException {
        if (
                smartContractData == null
                || smartContractData.getHashState().isEmpty()
                || smartContractData.getAddress().isEmpty()
                ) {
            this.pControls.setVisible(false);
            this.spCodePanel.setVisible(false);
        } else {
            this.pControls.setVisible(true);
            this.spCodePanel.setVisible(true);
            this.currentSmartContract = smartContractData;
            String sourceCode = smartContractData.getSourceCode();
            this.lAddress.setText(smartContractData.getAddress());
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.codeArea = SourceCodeUtils.initCodeArea(this.pCodePanel);
        TreeItem<Label> rootItem = new TreeItem<>(new Label("Smart contracts"));

        try {
            this.refreshFormState(null);
            List<SmartContractData> smartContracts = AppState.apiClient.getSmartContracts(AppState.account);
            smartContracts.forEach(smartContractData -> {

                Label label = new Label(smartContractData.getHashState());

                label.setOnMousePressed(event -> {
                    if (event.isPrimaryButtonDown()) {
                        if(event.getClickCount() == 2){
                            try {
                                this.refreshFormState(smartContractData);
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                                FormUtils.showError(e.getMessage());
                            }
                        }
                    }
                });

                rootItem.getChildren().add(new TreeItem<>(label));
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            FormUtils.showError(e.getMessage());
        }

        this.tvContracts.setRoot(rootItem);
        this.codeArea.setDisable(true);
    }

    @FXML
    private void cbMethodsOnAction() {
        this.pParams.setVisible(false);
        MethodDeclaration selectedMethod = this.cbMethods.getSelectionModel().getSelectedItem();
        if (selectedMethod == null) {
            return;
        }
        List<SingleVariableDeclaration> params = SourceCodeUtils.getMethodParameters(selectedMethod);
        this.pParamsContainer.getChildren().clear();
        if (params.size() > 0) {
            this.pParams.setVisible(true);
            double layoutY = 10;
            for (SingleVariableDeclaration param : params) {
                TextField paramValueTextField = new TextField();
                paramValueTextField.setLayoutX(250);
                paramValueTextField.setLayoutY(layoutY);
                paramValueTextField.setStyle("-fx-background-color:  #fff; -fx-border-width: 1; -fx-border-color:  #000; -fx-font-size: 16px");
                paramValueTextField.setPrefSize(150, 56);
                Label paramNameLabel = new Label(param.toString());
                paramNameLabel.setLayoutX(10);
                paramNameLabel.setLayoutY(layoutY + 15);
                paramNameLabel.setStyle("-fx-font-size: 18px");
                paramNameLabel.setLabelFor(paramValueTextField);
                this.pParamsContainer.getChildren().addAll(paramNameLabel, paramValueTextField);
                layoutY += 70;
            }
        }
    }

    @FXML
    private void handleExecute() {

        // Call contract executor
        if (AppState.contractExecutorHost != null &&
                AppState.contractExecutorPort != null &&
                AppState.contractExecutorDir != null ) {

            String sourceCode = this.currentSmartContract.getSourceCode();
            String address = currentSmartContract.getAddress();
            String method = cbMethods.getSelectionModel().getSelectedItem().getName().getIdentifier();
            List<String> params = new ArrayList<>();
            ObservableList<Node> paramsContainerChildren = this.pParamsContainer.getChildren();
            paramsContainerChildren.forEach(node -> {
                if (node instanceof TextField) {
                    String paramValue = ((TextField)node).getText();
                    params.add(paramValue);
                }
            });
            String token = SourceCodeUtils.generateSmartContractToken();
            String className = SourceCodeUtils.parseClassName(sourceCode);
            try {
                byte[] byteCode = SimpleInMemoryCompiler.compile(sourceCode, className, token);

                TTransport transport;
                transport = new TSocket(AppState.contractExecutorHost, AppState.contractExecutorPort);
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                ContractExecutor.Client client = new ContractExecutor.Client(protocol);
                LOGGER.info("Contract executor request: address = {}; method = {}; params = {}", address, method, params.toArray());

                APIResponse apiResponse = client.executeByteCode(address, Converter.bytesToByteBuffer(byteCode), method, params);

                String contractExecutorResponse = String.format("Contract executor response: code = %s; message = %s", apiResponse.getCode(), apiResponse.getMessage());
                transport.close();

                if (apiResponse.getCode() != 0) {
                    throw new WalletDesktopException(contractExecutorResponse);
                } else {
                    LOGGER.info(contractExecutorResponse);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                FormUtils.showError(e.getMessage());
            }
        }
    }
}