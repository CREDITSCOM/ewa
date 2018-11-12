package com.credits.wallet.desktop.controller;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.thrift.generated.TransactionState;
import com.credits.client.node.thrift.generated.TransactionsStateGetResult;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.general.util.Callback;
import com.credits.general.util.Converter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.TransactionTabRow;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.client.node.service.NodeApiServiceImpl.async;
import static com.credits.client.node.thrift.generated.TransactionState.INPROGRESS;
import static com.credits.client.node.thrift.generated.TransactionState.INVALID;
import static com.credits.client.node.thrift.generated.TransactionState.VALID;
import static com.credits.wallet.desktop.AppState.NODE_ERROR;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.detailFromHistory;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.AppState.selectedTransactionRow;

/**
 * Created by goncharov-eg on 29.01.2018.
 */
public class HistoryController implements Initializable {
    private static final String ERR_GETTING_TRANSACTION_HISTORY = "Error getting transaction history";
    private static final int INIT_PAGE_SIZE = 10;
    private static final int FIRST_PAGE_NUMBER = 1;
    private final static Logger LOGGER = LoggerFactory.getLogger(HistoryController.class);
    public static final int COUNT_ROUNDS_LIFE = 50;

    private int pageNumber = FIRST_PAGE_NUMBER;
    private int pageSize = INIT_PAGE_SIZE;

    @FXML
    BorderPane bp;
    @FXML
    private ComboBox<Integer> cbPageSize;
    @FXML
    private TableView<TransactionTabRow> approvedTableView;

    @FXML
    private TableView<TransactionTabRow> unapprovedTableView;

    @FXML
    private Label labPage;
    @FXML
    private Button btnFirst;
    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);

        initCombobox(cbPageSize);

        initTable(approvedTableView);
        initTable(unapprovedTableView);

        setPage();

        fillApprovedTable();
        fillUnapprovedTable();

    }

    private void initCombobox(ComboBox<Integer> comboBox) {
        for (int i = 1; i <= INIT_PAGE_SIZE; i++) {
            comboBox.getItems().add(INIT_PAGE_SIZE * i);
        }
        comboBox.getSelectionModel().select(0);
        comboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            pageSize = comboBox.getItems().get((int) newValue);
            pageNumber = 1;
            setPage();
            fillApprovedTable();
        });
    }

    private void initTable(TableView<TransactionTabRow> tableView) {
        tableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("innerId"));
        tableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("source"));
        tableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("target"));
        tableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("amount"));
        tableView.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("state"));
        tableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                TransactionTabRow tabRow = tableView.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    selectedTransactionRow = tabRow;
                    detailFromHistory = true;
                    VistaNavigator.loadVista(VistaNavigator.TRANSACTION);
                }
            }
        });
    }

    private void fillUnapprovedTable() {
        if (AppState.sourceMap.get(account) != null) {
            ConcurrentHashMap<Long, TransactionRoundData> sourceTransactionMap = AppState.sourceMap.get(account);
            /*List<Long> validIds =
                transactionsList.stream().map(TransactionData::getId).collect(Collectors.toList());
            sourceTransactionMap.remove(validIds)*/
            List<Long> ids = new ArrayList<>(sourceTransactionMap.keySet());
            async(() -> nodeApiService.getTransactionsState(account, ids),
                handleGetTransactionsStateResult(sourceTransactionMap));
        }
    }

    private void fillApprovedTable() {
        async(() -> nodeApiService.getTransactions(account, (pageNumber - 1) * pageSize, pageSize),
            handleGetTransactionsResult());
    }

    private Callback<List<TransactionData>> handleGetTransactionsResult() {
        return new Callback<List<TransactionData>>() {

            @Override
            public void onSuccess(List<TransactionData> transactionsList) throws CreditsException {
                btnNext.setDisable(transactionsList.size() < pageSize);

                List<TransactionTabRow> approvedList = new ArrayList<>();
                transactionsList.forEach(transactionData -> {
                    TransactionTabRow tableRow = new TransactionTabRow();
                    tableRow.setAmount(Converter.toString(transactionData.getAmount()));
                    tableRow.setSource(Converter.encodeToBASE58(transactionData.getSource()));
                    tableRow.setTarget(Converter.encodeToBASE58(transactionData.getTarget()));
                    tableRow.setInnerId(transactionData.getId());
                    tableRow.setState(VALID.name());
                    approvedList.add(tableRow);
                });
                refreshTableViewItems(approvedTableView, approvedList);
            }

            private void refreshTableViewItems(TableView<TransactionTabRow> tableView, List<TransactionTabRow> itemList) {
                Platform.runLater(() -> {
                    tableView.getItems().clear();
                    tableView.getItems().addAll(itemList);
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

    private Callback<TransactionsStateGetResult> handleGetTransactionsStateResult(
        ConcurrentHashMap<Long, TransactionRoundData> transactionMap) {
        return new Callback<TransactionsStateGetResult>() {
            @Override
            public void onSuccess(TransactionsStateGetResult transactionsStates) throws CreditsException {
                Map<Long, TransactionState> states = transactionsStates.getStates();
                states.forEach((k, v) -> {
                    if (v.getValue() == VALID.getValue()) {
                        transactionMap.remove(k);
                    }
                });

                int curRound = transactionsStates.getRoundNum();
                transactionMap.entrySet()
                    .removeIf(e -> e.getValue().getRoundNumber() != 0 &&
                        curRound >= e.getValue().getRoundNumber() + COUNT_ROUNDS_LIFE);

                List<TransactionTabRow> unapprovedList = new ArrayList<>();

                transactionMap.forEach((id, value) -> {
                    TransactionTabRow tableRow = new TransactionTabRow();
                    tableRow.setInnerId(id);
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
                refreshTableViewItems(unapprovedTableView, unapprovedList);


            }

            @Override
            public void onError(Throwable e) {
                Platform.runLater(()->approvedTableView.getItems().clear());
            }
        };
    }

    private void refreshTableViewItems(TableView<TransactionTabRow> tableView, List<TransactionTabRow> itemList) {
        Platform.runLater(() -> {
            tableView.getItems().clear();
            tableView.getItems().addAll(itemList);
        });
    }

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @FXML
    private void handleRefresh() {
        fillApprovedTable();
        fillUnapprovedTable();
    }

    @FXML
    private void handlePageFirst() {
        pageNumber = FIRST_PAGE_NUMBER;
        setPage();
        fillApprovedTable();
    }

    @FXML
    private void handlePagePrev() {
        if (pageNumber > FIRST_PAGE_NUMBER) {
            pageNumber = pageNumber - 1;
        }
        setPage();
        fillApprovedTable();
    }

    @FXML
    private void handlePageNext() {
        pageNumber = pageNumber + 1;
        setPage();
        fillApprovedTable();
    }

    private void setPage() {
        btnFirst.setDisable(pageNumber <= FIRST_PAGE_NUMBER);
        btnPrev.setDisable(pageNumber <= FIRST_PAGE_NUMBER);
        labPage.setText(Integer.toString(pageNumber));
    }
}

