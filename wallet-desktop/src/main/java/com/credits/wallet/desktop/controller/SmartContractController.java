package com.credits.wallet.desktop.controller;

import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Callback;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.SmartContractTabRow;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SmartContractUtils;
import com.credits.wallet.desktop.utils.TransactionIdCalculateUtils;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.client.node.service.NodeApiServiceImpl.async;
import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.AppState.smartContractsKeeper;
import static com.credits.wallet.desktop.utils.ApiUtils.createSmartContractTransaction;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController implements Initializable {

    private static final String PERSONAL_CONTRACTS = "Personal contracts";
    private static final String FOUND_CONTRACTS = "Found contracts";
    private static final String SMART_CONTRACTS = "Smart contracts";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);

    @FXML
    public ToggleButton tbFavourite;

    @FXML
    BorderPane bp;

    @FXML
    private Pane pControls;

    @FXML
    private TextField tfAddress;

    @FXML
    private ComboBox<MethodDeclaration> cbMethods;

    @FXML
    TableView<SmartContractTabRow> smartContractTableView;

    @FXML
    TableView<SmartContractTabRow> favoriteContractTableView;

    @FXML
    private TextField tfSearchAddress;

    @FXML
    private StackPane pCodePanel;

    @FXML
    private AnchorPane pParams;

    @FXML
    private AnchorPane pParamsContainer;

    private CodeArea codeArea;

    private SmartContractData currentSmartContract;

    private MethodDeclaration currentMethod;

    private static Map<String, SmartContractData> favoriteContracts = new HashMap<>();

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @FXML
    private void handleCreate() {
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT_DEPLOY);
    }

    @FXML
    private void handleSearch() {
        String address = tfSearchAddress.getText();
        async(() -> nodeApiService.getSmartContract(address), handleGetSmartContractResult());

    }

    private Callback<SmartContractData> handleGetSmartContractResult() {
        return new Callback<SmartContractData>() {
            @Override
            public void onSuccess(SmartContractData contractData) throws CreditsException {
                refreshFormState(contractData);
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("failed!", e);
                FormUtils.showError("Can't get smart-contract from the node. Reason: " + e.getMessage());
            }
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);
        this.tbFavourite.setVisible(false);
        this.pControls.setVisible(false);
        this.pCodePanel.setVisible(false);
        this.codeArea = SmartContractUtils.initCodeArea(this.pCodePanel, true);
        initSmartContracts();
        this.codeArea.setEditable(false);
        this.codeArea.copy();
    }

    private void initSmartContracts() {

        initializeTable(smartContractTableView);
        initializeTable(favoriteContractTableView);
        initMySmartTab();
    }

    private void initializeTable(TableView<SmartContractTabRow> tableView) {
        setRowFactory(tableView);
        initColumns(tableView);
    }

    private void initColumns(TableView<SmartContractTabRow> tableView) {
        TableColumn<SmartContractTabRow, String> idColumn = new TableColumn<>();
        idColumn.setPrefWidth(tableView.getPrefWidth() * 0.85);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        FormUtils.addTooltipToColumnCells(idColumn);

        TableColumn<SmartContractTabRow, String> favColumn = new TableColumn<>();
        favColumn.setPrefWidth(tableView.getPrefWidth() * 0.1);
        favColumn.setCellValueFactory(new PropertyValueFactory<>("fav"));

        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(favColumn);
    }

    private void setRowFactory(TableView<SmartContractTabRow> tableView) {
        tableView.setRowFactory(tv -> {
            TableRow<SmartContractTabRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    refreshFormState(row.getItem().getSmartContractData());
                }
            });
            return row;
        });
    }

    private void setFavorite(ToggleButton favoriteButton, ConcurrentHashMap<String, SmartContractData> map,
        SmartContractData smartContractData) {
        String smartContractAddress = smartContractData.getBase58Address();
        if (map != null && map.size() > 0 && map.get(smartContractAddress) != null) {
            favoriteButton.setSelected(map.get(smartContractAddress).isFavorite()); //fixme getObject favorite
        } else {
            favoriteButton.setSelected(false);
        }
    }

    private void setFavoriteButtonEvent(ToggleButton favoriteButton, SmartContractData smartContractData) {
        favoriteButton.setOnAction(event -> {
            smartContractData.setFavorite(favoriteButton.isSelected());
            saveFavorite(smartContractData);
            initMySmartTab();
            initFavoriteTab();

        });
    }

    private void saveFavorite(SmartContractData smartContractData) {
        String contractName = smartContractData.getBase58Address();
        smartContractsKeeper.modify(smartContractsKeeper.new Modifier() {
            @Override
            public ConcurrentHashMap<String, SmartContractData> modify( ConcurrentHashMap<String, SmartContractData> keptObject) {
            if (keptObject == null) {
                keptObject = new ConcurrentHashMap<>();
            }
            keptObject.put(contractName, smartContractData);
            return keptObject;
            }
        });
    }

    private void refreshFormState(SmartContractData smartContractData) {
        if (smartContractData == null || smartContractData.getByteCode().length == 0 || smartContractData.getAddress().length == 0) {
            this.tbFavourite.setVisible(false);
            this.pControls.setVisible(false);
            this.pCodePanel.setVisible(false);
        } else {
            this.pControls.setVisible(true);
            this.pCodePanel.setVisible(true);
            this.currentSmartContract = smartContractData;

            ConcurrentHashMap<String, SmartContractData> map = smartContractsKeeper.getKeptObject();
            setFavorite(tbFavourite, map, smartContractData);
            setFavoriteButtonEvent(tbFavourite, currentSmartContract);
            this.tbFavourite.setVisible(true);
            String sourceCode = smartContractData.getSourceCode();
            this.tfAddress.setText(smartContractData.getBase58Address());
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
                    "-fx-background-color:  #fff; -fx-border-radius:15; -fx-border-width: 1; -fx-border-color:  #000; -fx-font-size: 16px");
                paramValueTextField.setPrefSize(500, 30);
                Label paramNameLabel = new Label(param.toString());
                paramNameLabel.setLayoutX(10);
                paramNameLabel.setLayoutY(layoutY + 5);
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
            smartContractData.setMethod(method);
            smartContractData.setParams(params);

            CompletableFuture
                .supplyAsync(() -> TransactionIdCalculateUtils.calcTransactionIdSourceTarget(account,smartContractData.getBase58Address()))
                .thenApply((transactionData) -> createSmartContractTransaction(transactionData, smartContractData))
                .whenComplete(handleCallback(handleExecuteResult()));


        } catch (CreditsException e) {
            LOGGER.error("failed!", e);
            FormUtils.showError(e.toString());
        }
    }

    private Callback<ApiResponseData> handleExecuteResult() {
        return new Callback<ApiResponseData>() {
            @Override
            public void onSuccess(ApiResponseData resultData) {
                ApiUtils.saveTransactionRoundNumberIntoMap(resultData);
                Variant res = resultData.getScExecRetVal();
                if (res != null) {
                    String retVal = res.toString() + '\n';
                    LOGGER.info("Return value is {}", retVal);
                    FormUtils.showPlatformInfo("Execute smart contract was success: return value is: " + retVal);
                } else {
                    FormUtils.showPlatformInfo("Execute smart contract was success");
                }
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("failed!", e);
                FormUtils.showPlatformError(e.getMessage());
            }
        };
    }

    private void initMySmartTab() {
        smartContractTableView.getItems().clear();
        async(() -> nodeApiService.getSmartContracts(account), handleGetSmartContractsResult());
    }

    private Callback<List<SmartContractData>> handleGetSmartContractsResult() {
        return new Callback<List<SmartContractData>>() {
            @Override
            public void onSuccess(List<SmartContractData> smartContracts) throws CreditsException {
                ConcurrentHashMap<String, SmartContractData> map = smartContractsKeeper.getKeptObject();
                smartContracts.forEach(smartContractData -> {
                    ToggleButton favoriteButton = new ToggleButton();
                    setFavorite(favoriteButton, map, smartContractData);
                    setFavoriteButtonEvent(favoriteButton, smartContractData);
                    smartContractTableView.getItems()
                        .add(new SmartContractTabRow(smartContractData.getBase58Address(), favoriteButton, smartContractData));
                });
                if (currentSmartContract != null) {
                    setFavorite(tbFavourite, map, currentSmartContract);
                }
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("failed!", e);
                FormUtils.showError("Can't getObject smart-contracts from the node. Reason: " + e.getMessage());
            }
        };
    }

    @FXML
    private void initFavoriteTab() {
        favoriteContractTableView.getItems().clear();
        try {
            Map<String, SmartContractData> map = smartContractsKeeper.getKeptObject();
            if (map != null && map.size() > 0) {
                map.forEach((smartName, smartContractData) -> {
                    if (smartContractData.isFavorite()) { //fixme get favorite
                        ToggleButton favoriteButton = new ToggleButton();
                        favoriteButton.setSelected(true);
                        setFavoriteButtonEvent(favoriteButton, smartContractData);
                        favoriteContractTableView.getItems()
                            .add(new SmartContractTabRow(smartContractData.getBase58Address(), favoriteButton,
                                smartContractData));
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("failed!", e);
            FormUtils.showError(e.getMessage());
        }
    }

}