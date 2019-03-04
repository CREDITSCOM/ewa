package com.credits.wallet.desktop.controller;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.*;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.general.pojo.VariantData;
import com.credits.general.util.Callback;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.variant.VariantUtils;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.ParseResultStruct;
import com.credits.wallet.desktop.struct.SmartContractTabRow;
import com.credits.wallet.desktop.struct.SmartContractTransactionTabRow;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.sourcecode.ParseCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CodeAreaUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.credits.client.node.service.NodeApiServiceImpl.async;
import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.client.node.thrift.generated.TransactionState.*;
import static com.credits.client.node.util.TransactionIdCalculateUtils.calcTransactionIdSourceTarget;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.wallet.desktop.AppState.NODE_ERROR;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.utils.ApiUtils.createSmartContractTransaction;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.getSmartsListFromField;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends AbstractController {

    private static final String ERR_FEE = "Fee must be greater than 0";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);
    public static final String SELECTED_CONTRACT_KEY = "selectedContract";
    public static final String SELECTED_CONTRACTS_TAB_KEY = "selectedContractsTab";
    public static final String SELECTED_CONTRACTS_TAB_MY_SMART_CONTRACTS = "my-contracts";
    public static final String SELECTED_CONTRACTS_TAB_FAVORITES_CONTRACTS = "favorites-contracts";

    private CodeArea codeArea;
    private SmartContractData currentSmartContract;
    private MethodDeclaration currentMethod;
    private HashMap<String, SmartContractData> favoriteContracts;

    private final int INIT_PAGE_SIZE = 100;
    private final int COUNT_ROUNDS_LIFE = 50;
    private final String ERR_GETTING_TRANSACTION_HISTORY = "Error getting transaction history";
    private List<SmartContractTransactionTabRow> unapprovedList = new ArrayList<>();
    private List<SmartContractTransactionTabRow> approvedList = new ArrayList<>();
    private SmartContractData selectedContract;

    @FXML
    public TextField usdSmart;
    @FXML
    public Tab myContractsTab;
    @FXML
    public Tab favoriteContractsTab;
    @FXML
    public ToggleButton tbFavorite;
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
    @FXML
    private TextField feeField;
    @FXML
    private Label actualOfferedMaxFeeLabel;
    @FXML
    private Label feeErrorLabel;
    @FXML
    private TableView<SmartContractTransactionTabRow> approvedTableView;
    @FXML
    private TableView<SmartContractTransactionTabRow> unapprovedTableView;
    @FXML
    private TabPane contractsTabPane;

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
    public void initializeForm(Map<String, Object> objects) {
        tbFavorite.setVisible(false);
        pControls.setVisible(false);
        pCodePanel.setVisible(false);
        codeArea = CodeAreaUtils.initCodeArea(this.pCodePanel, true);
        codeArea.setEditable(false);
        codeArea.copy();
        favoriteContracts = session.favoriteContractsKeeper.getKeptObject().orElseGet(HashMap::new);
        initializeTable(smartContractTableView);
        initializeTable(favoriteContractTableView);
        refreshFavoriteContractsTab();
        FormUtils.initFeeField(feeField,actualOfferedMaxFeeLabel);
        initTable(approvedTableView);
        initTable(unapprovedTableView);

        if (objects != null) {
            selectedContract = (SmartContractData) objects.get(SELECTED_CONTRACT_KEY);
            String selectedContractsTab = (String) objects.get(SELECTED_CONTRACTS_TAB_KEY);
            if (selectedContractsTab.equals(SELECTED_CONTRACTS_TAB_MY_SMART_CONTRACTS)) {
                contractsTabPane.getSelectionModel().select(1);
            }
        }
    }

    private HashMap<String, List<SmartContractTransactionData>> getKeptContractsTransactions() {
        return session.contractsTransactionsKeeper.getKeptObject().orElseGet(HashMap::new);
    }

    private void fillTransactionsTables(String base58Address) {
        approvedTableView.getItems().clear();
        approvedList.clear();
        unapprovedTableView.getItems().clear();
        unapprovedList.clear();
        if (session.sourceMap != null) {
            ConcurrentHashMap<Long, TransactionRoundData> sourceTransactionMap = session.sourceMap;
            List<Long> ids = new ArrayList<>(sourceTransactionMap.keySet());
            async(() -> nodeApiService.getTransactionsState(base58Address, ids),
                    handleGetTransactionsStateResult(sourceTransactionMap));
        }

        List<SmartContractTransactionData> contractTransactions = getKeptContractsTransactions().getOrDefault(base58Address, new ArrayList<>());

        async(() -> nodeApiService.getSmartContractTransactions(base58Address, contractTransactions.size(), INIT_PAGE_SIZE),
                handleGetTransactionsResult());
    }

    private Callback<TransactionsStateGetResultData> handleGetTransactionsStateResult(
            ConcurrentHashMap<Long, TransactionRoundData> transactionMap) {
        return new Callback<TransactionsStateGetResultData>() {
            @Override
            public void onSuccess(TransactionsStateGetResultData transactionsStateGetResultData) throws CreditsException {
                Map<Long, TransactionStateData> states = transactionsStateGetResultData.getStates();
                states.forEach((k, v) -> {
                    if (v.getValue() == VALID.getValue()) {
                        transactionMap.remove(k);
                    }
                });

                int curRound = transactionsStateGetResultData.getRoundNumber();
                transactionMap.entrySet()
                        .removeIf(e -> e.getValue().getRoundNumber() != 0 &&
                                curRound >= e.getValue().getRoundNumber() + COUNT_ROUNDS_LIFE);

                transactionMap.forEach((id, value) -> {
                    SmartContractTransactionTabRow tableRow = new SmartContractTransactionTabRow();
/*
                    tableRow.setInnerId(id);
*/
                    tableRow.setAmount(value.getAmount());
                    tableRow.setCurrency(value.getCurrency());
                    tableRow.setSource(value.getSource());
                    tableRow.setTarget(value.getTarget());
                    if (states.get(id) != null) {
                        if (states.get(id).getValue() == INVALID.getValue()) {
                            tableRow.setState(INVALID.name());
                        } else if (curRound == 0 || states.get(id).getValue() == INPROGRESS.getValue()) {
                            tableRow.setState(INPROGRESS.name());
                        }
                        unapprovedList.add(tableRow);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error(e.getMessage());
                if (e instanceof NodeClientException) {
                    FormUtils.showError(NODE_ERROR);
                } else {
                    FormUtils.showError(ERR_GETTING_TRANSACTION_HISTORY);
                }
            }
        };
    }

    private void keepTransactions(String base58Address, List<SmartContractTransactionData> transactionList) {

        HashMap<String, List<SmartContractTransactionData>> contractsTransactions = getKeptContractsTransactions();
        List<SmartContractTransactionData> transactions = contractsTransactions.getOrDefault(base58Address, new ArrayList<>());
        transactions.addAll(transactionList);
        contractsTransactions.put(base58Address, transactions);
        session.contractsTransactionsKeeper.keepObject(contractsTransactions);
    }

    private Callback<List<SmartContractTransactionData>> handleGetTransactionsResult() {
        return new Callback<List<SmartContractTransactionData>>() {

            @Override
            public void onSuccess(List<SmartContractTransactionData> transactionList) throws CreditsException {

                keepTransactions(selectedContract.getBase58Address(), transactionList);
                List<SmartContractTransactionData> smartContractTransactionDataList =
                        getKeptContractsTransactions().getOrDefault(selectedContract.getBase58Address(), new ArrayList<>());

                smartContractTransactionDataList.forEach(transactionData -> {

                    SmartTransInfoData smartInfo = transactionData.getSmartInfo();

                    SmartContractTransactionTabRow tableRow = new SmartContractTransactionTabRow();
                    tableRow.setAmount(GeneralConverter.toString(transactionData.getAmount()));
                    tableRow.setSource(GeneralConverter.encodeToBASE58(transactionData.getSource()));
                    tableRow.setTarget(GeneralConverter.encodeToBASE58(transactionData.getTarget()));
                    tableRow.setBlockId(transactionData.getBlockId());
                    tableRow.setState(VALID.name());
                    tableRow.setMethod(transactionData.getMethod());
                    tableRow.setParams(transactionData.getParams());
                    tableRow.setSmartInfo(smartInfo);

                    if (smartInfo.isSmartDeploy()) {
                        SmartDeployTransInfoData smartDeployTransInfoData = smartInfo.getSmartDeployTransInfoData();
                        if (smartDeployTransInfoData.getState() == SmartOperationStateData.SOS_Success) {
                            approvedList.add(tableRow);
                        } else {
                            unapprovedList.add(tableRow);
                        }
                    } else if (smartInfo.isSmartExecution()) {
                        SmartExecutionTransInfoData smartExecutionTransInfoData = smartInfo.getSmartExecutionTransInfoData();
                        if (smartExecutionTransInfoData.getState() == SmartOperationStateData.SOS_Success) {
                            approvedList.add(tableRow);
                        } else {
                            unapprovedList.add(tableRow);
                        }
                    } else if (smartInfo.isSmartState()) {
                        SmartStateTransInfoData smartStateTransInfoData = smartInfo.getSmartStateTransInfoData();
                        if (smartStateTransInfoData.success) {
                            approvedList.add(tableRow);
                        } else {
                            unapprovedList.add(tableRow);
                        }
                    }
                });
                Platform.runLater(() -> {
                    approvedTableView.getItems().addAll(approvedList);
                });
                Platform.runLater(() -> {
                    unapprovedTableView.getItems().addAll(unapprovedList);
                });
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error(e.getMessage());
                if (e instanceof NodeClientException) {
                    FormUtils.showError(NODE_ERROR);
                } else {
                    FormUtils.showError(ERR_GETTING_TRANSACTION_HISTORY);
                }
            }
        };
    }

    private void initTable(TableView<SmartContractTransactionTabRow> tableView) {
        tableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("blockId"));
        tableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("source"));
        tableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("target"));
        tableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("amount"));
        tableView.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("state"));
        tableView.setOnMousePressed(event -> {
            if ((event.isPrimaryButtonDown()|| event.getButton() == MouseButton.PRIMARY) && event.getClickCount() == 2) {
                SmartContractTransactionTabRow tabRow = tableView.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("selectedTransactionRow",tabRow);
                    params.put(SELECTED_CONTRACT_KEY, selectedContract);
                    if (contractsTabPane.getSelectionModel().isSelected(0)) {
                        params.put(SELECTED_CONTRACTS_TAB_KEY, SELECTED_CONTRACTS_TAB_FAVORITES_CONTRACTS);
                    } else if (contractsTabPane.getSelectionModel().isSelected(1)) {
                        params.put(SELECTED_CONTRACTS_TAB_KEY, SELECTED_CONTRACTS_TAB_MY_SMART_CONTRACTS);
                    }

                    VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT_TRANSACTION, params);
                }
            }
        });
    }

    @FXML
    private void updateSelectedTab() {
        if (myContractsTab != null && myContractsTab.isSelected()) {
            refreshContractsTab();
        } else if (favoriteContractsTab != null && favoriteContractsTab.isSelected()) {
            refreshFavoriteContractsTab();
        }
    }

    private void initializeTable(TableView<SmartContractTabRow> tableView) {
        initializeRowFactory(tableView);
        initializeColumns(tableView);
    }

    private void initializeColumns(TableView<SmartContractTabRow> tableView) {
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

    private void initializeRowFactory(TableView<SmartContractTabRow> tableView) {
        tableView.setRowFactory(tv -> {
            TableRow<SmartContractTabRow> row = new TableRow<>();
            row.setOnMouseClicked(getMouseEventRowHandler(row));
            return row;
        });
    }

    private EventHandler<MouseEvent> getMouseEventRowHandler(TableRow<SmartContractTabRow> row) {
        return event -> {
            if (event.getClickCount() == 2 && (!row.isEmpty())) {
                refreshFormState(row.getItem().getSmartContractData());
            }
        };
    }

    private EventHandler<ActionEvent> handleFavoriteButtonEvent(ToggleButton pressedButton,
                                                                SmartContractData smartContractData) {
        return event -> switchFavoriteState(pressedButton, smartContractData);
    }

    private void switchFavoriteState(ToggleButton favoriteButton, SmartContractData smartContractData) {
        if (favoriteContracts.containsKey(smartContractData.getBase58Address())) {
            favoriteContracts.remove(smartContractData.getBase58Address());
            favoriteButton.setSelected(false);
            setFavoriteCurrentContract(smartContractData, false);
            changeFavoriteStateIntoTab(smartContractTableView, smartContractData, false);
        } else {
            favoriteContracts.put(smartContractData.getBase58Address(), smartContractData);
            favoriteButton.setSelected(true);
            tbFavorite.setSelected(true);
            setFavoriteCurrentContract(smartContractData, true);
            changeFavoriteStateIntoTab(smartContractTableView, smartContractData, true);
        }
        refreshFavoriteContractsTab();
    }

    private void changeFavoriteStateIntoTab(TableView<SmartContractTabRow> table, SmartContractData smartContractData,
                                            boolean isSelected) {
        table.getItems()
                .stream()
                .filter(row -> row.getSmartContractData().equals(smartContractData))
                .findFirst()
                .ifPresent(row -> row.getFav().setSelected(isSelected));
        table.refresh();
    }

    private void setFavoriteCurrentContract(SmartContractData smartContractData, boolean isSelected) {
        if (currentSmartContract != null &&
                smartContractData.getBase58Address().equals(currentSmartContract.getBase58Address())) {
            tbFavorite.setSelected(isSelected);
        } else {
            tbFavorite.setSelected(false);
        }
    }

    private void refreshFormState(SmartContractData smartContractData) {
        if (smartContractData == null || smartContractData.getSmartContractDeployData().getByteCodeObjects().size() == 0 ||
                smartContractData.getAddress().length == 0) {
            tbFavorite.setVisible(false);
            pControls.setVisible(false);
            pCodePanel.setVisible(false);
        } else {
            selectedContract = smartContractData;

            pControls.setVisible(true);
            pCodePanel.setVisible(true);
            currentSmartContract = smartContractData;

            tbFavorite.setOnAction(handleFavoriteButtonEvent(tbFavorite, currentSmartContract));
            tbFavorite.setVisible(true);
            findInFavoriteThenSelect(smartContractData, tbFavorite);

            String sourceCode = smartContractData.getSmartContractDeployData().getSourceCode();
            tfAddress.setText(smartContractData.getBase58Address());
            ParseResultStruct build =
                    new ParseResultStruct.Builder(sourceCode).methods().build();

            List<MethodDeclaration> methods = build.methods;
            cbMethods.getItems().clear();
            methods.forEach(method -> {
                cbMethods.getItems().add(method);
            });
            codeArea.clear();
            codeArea.replaceText(0, 0, SourceCodeUtils.formatSourceCode(sourceCode));

            fillTransactionsTables(smartContractData.getBase58Address());
        }
    }

    private void findInFavoriteThenSelect(SmartContractData smartContractData, ToggleButton tbFavorite) {
        if (favoriteContracts.containsKey(smartContractData.getBase58Address())) {
            tbFavorite.setSelected(true);
        } else {
            tbFavorite.setSelected(false);
        }
    }

    private void refreshContractsTab() {
        async(() -> nodeApiService.getSmartContracts(session.account), handleGetSmartContractsResult());
    }

    private void refreshFavoriteContractsTab() {
        if (favoriteContractTableView != null && favoriteContracts != null) {
            favoriteContractTableView.getItems().clear();
            favoriteContracts.forEach(
                    (contractName, contractData) -> addContractToTable(favoriteContractTableView, contractData));
        }
        if(session!=null) {
            session.favoriteContractsKeeper.keepObject(favoriteContracts);
        }
    }

    private Callback<List<SmartContractData>> handleGetSmartContractsResult() {
        return new Callback<List<SmartContractData>>() {
            @Override
            public void onSuccess(List<SmartContractData> smartContracts) throws CreditsException {
                Platform.runLater(() -> {
                    smartContractTableView.getItems().clear();
                    smartContracts.forEach(smartContract -> addContractToTable(smartContractTableView, smartContract));

                    if (selectedContract != null) {
                        ObservableList<SmartContractTabRow> items = smartContractTableView.getItems();
                        items.forEach(smartContractTabRow -> {
                            if (smartContractTabRow.getSmartContractData().equals(selectedContract)) {
                                smartContractTableView.requestFocus();
                                smartContractTableView.getSelectionModel().select(smartContractTabRow);
                                refreshFormState(selectedContract);
                                return;
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("Can't get smart-contracts from the node. Reason: {}", e.getMessage());
                FormUtils.showError("Can't get smart-contracts from the node. Reason: " + e.getMessage());
            }
        };
    }

    @FXML
    private void cbMethodsOnAction() {
        this.pParams.setVisible(false);
        this.currentMethod = this.cbMethods.getSelectionModel().getSelectedItem();
        if (this.currentMethod == null) {
            return;
        }
        List<SingleVariableDeclaration> params = this.currentMethod.parameters();
        this.pParamsContainer.getChildren().clear();
        if (params.size() > 0) {
            this.pParams.setVisible(true);
            double layoutY = 10;
            for (SingleVariableDeclaration param : params) {
                TextField paramValueTextField = new TextField();
                paramValueTextField.setLayoutX(250);
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
            List<ByteBuffer> usedSmartsListFromField = getSmartsListFromField(usdSmart.getText());
            // VALIDATE
            AtomicBoolean isValidationSuccessful = new AtomicBoolean(true);
            clearLabErr();
            String transactionFee = feeField.getText();
            if (GeneralConverter.toBigDecimal(transactionFee).compareTo(BigDecimal.ZERO) <= 0) {
                FormUtils.validateField(feeField, feeErrorLabel, ERR_FEE, isValidationSuccessful);
            }
            if (!isValidationSuccessful.get()) {
                return;
            }
            String method = cbMethods.getSelectionModel().getSelectedItem().getName().getIdentifier();
            List<VariantData> params = new ArrayList<>();
            List<SingleVariableDeclaration> currentMethodParams = this.currentMethod.parameters();
            ObservableList<Node> paramsContainerChildren = this.pParamsContainer.getChildren();
            int i = 0;
            for (Node node : paramsContainerChildren) {
                if (node instanceof TextField) {
                    String paramValue = ((TextField) node).getText();
                    SingleVariableDeclaration variableDeclaration = currentMethodParams.get(i);
                    String className = ParseCodeUtils.parseClassName(variableDeclaration.getType());
                    params.add(VariantUtils.createVariantData(className, paramValue));
                    ++i;
                }
            }
            SmartContractData smartContractData = this.currentSmartContract;
            smartContractData.setMethod(method);
            smartContractData.setParams(params);

            CompletableFuture.supplyAsync(() -> calcTransactionIdSourceTarget(AppState.nodeApiService, session.account,
                    smartContractData.getBase58Address(), true), threadPool)
                    .thenApply((transactionData) -> createSmartContractTransaction(transactionData, FormUtils.getActualOfferedMaxFee16Bits(feeField), smartContractData, usedSmartsListFromField, session))
                    .whenComplete(handleCallback(handleExecuteResult()));


        } catch (CreditsException e) {
            LOGGER.error("failed!", e);
            FormUtils.showError(e.toString());
        }
    }

    private Callback<Pair<Long, TransactionFlowResultData>> handleExecuteResult() {
        return new Callback<Pair<Long, TransactionFlowResultData>>() {
            @Override
            public void onSuccess(Pair<Long, TransactionFlowResultData> resultData) {
                ApiUtils.saveTransactionRoundNumberIntoMap(resultData.getRight().getRoundNumber(), //TODO uncomment
                        resultData.getLeft(), session);
                TransactionFlowResultData transactionFlowResultData = resultData.getRight();
                FormUtils.showPlatformInfo(transactionFlowResultData.getMessage());
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("failed!", e);
                FormUtils.showPlatformError(e.getMessage());
            }
        };
    }

    private void addContractToTable(TableView<SmartContractTabRow> table, SmartContractData smartContractData) {
        ToggleButton favoriteButton = new ToggleButton();
        favoriteButton.setOnAction(handleFavoriteButtonEvent(favoriteButton, smartContractData));
        SmartContractTabRow row =
                new SmartContractTabRow(smartContractData.getBase58Address(), favoriteButton, smartContractData);
        row.setFav(favoriteButton);
        findInFavoriteThenSelect(smartContractData, favoriteButton);
        table.getItems().add(row);
    }

    private void clearLabErr() {
        FormUtils.clearErrorOnField(feeField, feeErrorLabel);
    }

    public void handleRefreshSmarts() {
        updateSelectedTab();
    }

    @Override
    public void formDeinitialize() {

    }

    public void handleRefreshTransactions() {
        fillTransactionsTables(selectedContract.getBase58Address());
    }
}