package com.credits.wallet.desktop.controller;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.*;
import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Callback;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.variant.VariantConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.MethodData;
import com.credits.wallet.desktop.struct.SmartContractTabRow;
import com.credits.wallet.desktop.struct.SmartContractTransactionTabRow;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
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
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.credits.client.node.service.NodeApiServiceImpl.async;
import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.client.node.thrift.generated.TransactionState.*;
import static com.credits.client.node.util.TransactionIdCalculateUtils.calcTransactionIdSourceTarget;
import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static com.credits.general.util.GeneralConverter.createObjectFromString;
import static com.credits.general.util.Utils.rethrowUnchecked;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.general.util.variant.VariantConverter.toVariant;
import static com.credits.wallet.desktop.AppState.NODE_ERROR;
import static com.credits.wallet.desktop.utils.ApiUtils.createSmartContractTransaction;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.getSmartsListFromField;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.deleteDirectory;


public class SmartContractController extends AbstractController {

    private static final String ERR_FEE = "Fee must be greater than 0";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);

    private CodeArea codeArea;
    private Method currentMethod;
    private HashMap<String, CompiledSmartContract> favoriteContracts;

    private final int COUNT_ROUNDS_LIFE = 50;
    private final String ERR_GETTING_TRANSACTION_HISTORY = "Error getting transaction history";
    private final List<SmartContractTransactionTabRow> unapprovedList = new ArrayList<>();
    private final List<SmartContractTransactionTabRow> approvedList = new ArrayList<>();
    private CompiledSmartContract selectedContract;

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
    private ComboBox<MethodData> cbMethods;
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
        async(() -> AppState.getNodeApiService().getSmartContract(address), handleGetSmartContractResult());

    }

    private Callback<SmartContractData> handleGetSmartContractResult() {
        return new Callback<SmartContractData>() {
            @Override
            public void onSuccess(SmartContractData contractData) throws CreditsException {
                refreshSmartContractForm(compileSmartContract(contractData));
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
        favoriteContracts.forEach((key, value) -> {
            SmartContractClass contractClass;
            try {
                contractClass = compileContractClass(value.getSmartContractDeployData().getByteCodeObjects());
            } catch (Throwable e) {
                rethrowUnchecked(() -> deleteDirectory(session.favoriteContractsKeeper.getAccountDirectory().toFile()));
                return;
            }
            value.setContractClass(contractClass);
        });
        initializeContractsTable(smartContractTableView);
        initializeContractsTable(favoriteContractTableView);
        refreshFavoriteContractsTab();
        FormUtils.initFeeField(feeField, actualOfferedMaxFeeLabel);
        initTransactionHistoryTable(approvedTableView);
        initTransactionHistoryTable(unapprovedTableView);
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
            async(
                () -> AppState.getNodeApiService().getTransactionsState(base58Address, ids),
                handleGetTransactionsStateResult(sourceTransactionMap));
        }

        List<SmartContractTransactionData> contractTransactions = getKeptContractsTransactions().getOrDefault(base58Address, new ArrayList<>());
        async(
            () -> {
                SmartContractData smartContractData = AppState.getNodeApiService().getSmartContract(base58Address);
                long transactionCount = smartContractData.getTransactionsCount();
                return AppState.getNodeApiService().getSmartContractTransactions(base58Address, 0, transactionCount - contractTransactions.size());
            },
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
                    FormUtils.showError(ERR_GETTING_TRANSACTION_HISTORY + "\n" + e.getMessage());
                }
            }
        };
    }

    private void keepTransactions(String base58Address, List<SmartContractTransactionData> transactionList) {
        if (transactionList.size() == 0) {
            return;
        }
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
                    tableRow.setType(transactionData.getType().getName());

                    if (smartInfo == null) {
                        approvedList.add(tableRow);
                    } else if (smartInfo.isSmartDeploy()) {
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
                    FormUtils.showError(ERR_GETTING_TRANSACTION_HISTORY + "\n" + e.getMessage());
                }
            }
        };
    }

    private void initTransactionHistoryTable(TableView<SmartContractTransactionTabRow> tableView) {
        tableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("blockId"));
        tableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("source"));
        tableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("target"));
        tableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("amount"));
        tableView.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("state"));
        tableView.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("type"));
        tableView.setOnMousePressed(event -> {
            if ((event.isPrimaryButtonDown() || event.getButton() == MouseButton.PRIMARY) && event.getClickCount() == 2) {
                SmartContractTransactionTabRow tabRow = tableView.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("selectedTransactionRow", tabRow);
                    VistaNavigator.showFormModal(VistaNavigator.SMART_CONTRACT_TRANSACTION, params);
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

    private void initializeContractsTable(TableView<SmartContractTabRow> tableView) {
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
                refreshSmartContractForm(row.getItem().getCompiledSmartContract());
            }
        };
    }

    private EventHandler<ActionEvent> handleFavoriteButtonEvent(
        ToggleButton pressedButton,
        CompiledSmartContract compiledSmartContract) {
        return event -> switchFavoriteState(pressedButton, compiledSmartContract);
    }

    private void switchFavoriteState(ToggleButton favoriteButton, CompiledSmartContract compiledSmartContract) {
        if (favoriteContracts.containsKey(compiledSmartContract.getBase58Address())) {
            favoriteContracts.remove(compiledSmartContract.getBase58Address());
            favoriteButton.setSelected(false);
            setFavoriteCurrentContract(compiledSmartContract, false);
            changeFavoriteStateIntoTab(smartContractTableView, compiledSmartContract, false);
        } else {
            favoriteContracts.put(compiledSmartContract.getBase58Address(), compiledSmartContract);
            favoriteButton.setSelected(true);
            tbFavorite.setSelected(true);
            setFavoriteCurrentContract(compiledSmartContract, true);
            changeFavoriteStateIntoTab(smartContractTableView, compiledSmartContract, true);
        }
        refreshFavoriteContractsTab();
    }

    private void changeFavoriteStateIntoTab(
        TableView<SmartContractTabRow> table, SmartContractData smartContractData,
        boolean isSelected) {
        table.getItems()
            .stream()
            .filter(row -> row.getCompiledSmartContract().equals(smartContractData))
            .findFirst()
            .ifPresent(row -> row.getFav().setSelected(isSelected));
        table.refresh();
    }

    private void setFavoriteCurrentContract(SmartContractData smartContractData, boolean isSelected) {
        if (selectedContract != null &&
            smartContractData.getBase58Address().equals(selectedContract.getBase58Address())) {
            tbFavorite.setSelected(isSelected);
        } else {
            tbFavorite.setSelected(false);
        }
    }


    private CompiledSmartContract compileSmartContract(SmartContractData smartContractData) {
        return new CompiledSmartContract(
            smartContractData,
            compileContractClass(smartContractData.getSmartContractDeployData().getByteCodeObjects()),
            smartContractData.getSmartContractDeployData().getByteCodeObjects());
    }

    private SmartContractClass compileContractClass(List<ByteCodeObjectData> byteCodeObjects) {
        Class<?> contractClass = null;
        List<Class<?>> innerContractClasses = new ArrayList<>();
        ByteCodeContractClassLoader byteCodeContractClassLoader = new ByteCodeContractClassLoader();
        for (ByteCodeObjectData byteCodeObject : byteCodeObjects) {
            Class<?> clazz = byteCodeContractClassLoader.loadClass(byteCodeObject.getName(), byteCodeObject.getByteCode());
            if (clazz.getName().contains("$")) {
                innerContractClasses.add(clazz);
            } else {
                contractClass = clazz;
            }
        }
        return new SmartContractClass(contractClass, innerContractClasses);
    }

    private final static Set<String> objectMethods = new HashSet<>(asList(
        "getClass",
        "hashCode",
        "equals",
        "toString",
        "notify",
        "notifyAll",
        "wait",
        "finalize"));

    private void refreshSmartContractForm(CompiledSmartContract compiledSmartContract) {
        if (compiledSmartContract == null || compiledSmartContract.getSmartContractDeployData().getByteCodeObjects().size() == 0 ||
            compiledSmartContract.getAddress().length == 0) {
            tbFavorite.setVisible(false);
            pControls.setVisible(false);
            pCodePanel.setVisible(false);
        } else {
            selectedContract = compiledSmartContract;

            pControls.setVisible(true);
            pCodePanel.setVisible(true);

            tbFavorite.setOnAction(handleFavoriteButtonEvent(tbFavorite, selectedContract));
            tbFavorite.setVisible(true);
            findInFavoriteThenSelect(compiledSmartContract, tbFavorite);

            String sourceCode = compiledSmartContract.getSmartContractDeployData().getSourceCode();
            tfAddress.setText(compiledSmartContract.getBase58Address());
            MethodData[] methods = Arrays.stream(compiledSmartContract.getContractClass().getRootClass().getMethods())
                .filter(m -> !objectMethods.contains(m.getName()))
                .map(MethodData::new)
                .toArray(MethodData[]::new);
            cbMethods.getItems().clear();
            cbMethods.getItems().addAll(methods);

            codeArea.clear();
            codeArea.replaceText(0, 0, SourceCodeUtils.formatSourceCode(sourceCode));

            fillTransactionsTables(compiledSmartContract.getBase58Address());
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
        async(() -> AppState.getNodeApiService().getSmartContracts(session.account), handleGetSmartContractsResult());
    }

    private void refreshFavoriteContractsTab() {
        if (favoriteContractTableView != null && favoriteContracts != null) {
            favoriteContractTableView.getItems().clear();
            favoriteContracts.forEach(
                (contractName, contractData) -> addContractToTable(favoriteContractTableView, contractData));
        }
        if (session != null) {
            session.favoriteContractsKeeper.keepObject(favoriteContracts);
        }
    }

    private Callback<List<SmartContractData>> handleGetSmartContractsResult() {
        return new Callback<List<SmartContractData>>() {
            @Override
            public void onSuccess(List<SmartContractData> smartContracts) throws CreditsException {
                Platform.runLater(() -> {
                    smartContractTableView.getItems().clear();
                    smartContracts.forEach(smartContract -> addContractToTable(smartContractTableView, compileSmartContract(smartContract)));

                    if (selectedContract != null) {
                        ObservableList<SmartContractTabRow> items = smartContractTableView.getItems();
                        items.forEach(smartContractTabRow -> {
                            if (smartContractTabRow.getCompiledSmartContract().equals(selectedContract)) {
                                smartContractTableView.requestFocus();
                                smartContractTableView.getSelectionModel().select(smartContractTabRow);
                                refreshSmartContractForm(selectedContract);
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
        this.currentMethod = this.cbMethods.getSelectionModel().getSelectedItem().getMethod();
        if (this.currentMethod == null) {
            return;
        }
        Parameter[] params = this.currentMethod.getParameters();
        this.pParamsContainer.getChildren().clear();
        if (params.length > 0) {
            this.pParams.setVisible(true);
            double layoutY = 10;
            for (Parameter param : params) {
                TextField paramValueTextField = new TextField();
                paramValueTextField.setLayoutX(250);
                paramValueTextField.setLayoutY(layoutY);
                paramValueTextField.setStyle(
                    "-fx-background-color:  #fff; -fx-border-radius:15; -fx-border-width: 1; -fx-border-color:  #000; -fx-font-size: 16px");
                paramValueTextField.setPrefSize(500, 30);
                Label paramNameLabel = new Label(param.getType().getSimpleName() + " " + param.getName());
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
            Method method = cbMethods.getSelectionModel().getSelectedItem().getMethod();
            List<Variant> params = new ArrayList<>();
            Parameter[] currentMethodParams = this.currentMethod.getParameters();
            ObservableList<Node> paramsContainerChildren = this.pParamsContainer.getChildren();
            int i = 0;
            for (Node node : paramsContainerChildren) {
                if (node instanceof TextField) {
                    String paramValue = ((TextField) node).getText();
                    Parameter parameter = currentMethodParams[i];
                    params.add(toVariant(parameter.getType().getTypeName(), createObjectFromString(paramValue, parameter.getType())));
                    ++i;
                }
            }
            SmartContractData smartContractData = this.selectedContract;
            smartContractData.setMethod(method.getName());
            smartContractData.setParams(params);

            CompletableFuture
                .supplyAsync(
                    () -> calcTransactionIdSourceTarget(
                        AppState.getNodeApiService(),
                        session.account,
                        smartContractData.getBase58Address(),
                        true),
                    threadPool)
                .thenApply(
                    (transactionData) -> createSmartContractTransaction(
                        transactionData,
                        FormUtils.getActualOfferedMaxFee16Bits(feeField),
                        smartContractData,
                        usedSmartsListFromField,
                        session))
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
                Variant contractResult = transactionFlowResultData.getContractResult().orElseGet(() -> new Variant(V_STRING, "unknown result"));
                String message = transactionFlowResultData.getMessage();

                if (!contractResult.isSetV_void()) {
                    Object resultObj = VariantConverter.toObject(contractResult);
                    message = "Result: " + resultObj.toString() + "\n\n" + message;
                }

                FormUtils.showPlatformInfo(message);
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("failed!", e);
                FormUtils.showPlatformError(e.getMessage());
            }
        };
    }

    private void addContractToTable(TableView<SmartContractTabRow> table, CompiledSmartContract smartContractData) {
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