package com.credits.wallet.desktop.controller;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.thrift.generated.TransactionState;
import com.credits.client.node.thrift.generated.TransactionsStateGetResult;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.general.util.Callback;
import com.credits.general.util.Converter;
import com.credits.general.util.Utils;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.TransactionTabRow;
import com.credits.wallet.desktop.utils.FormUtils;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.credits.client.node.service.NodeApiServiceImpl.async;
import static com.credits.client.node.thrift.generated.TransactionState.INPROGRESS;
import static com.credits.client.node.thrift.generated.TransactionState.INVALID;
import static com.credits.client.node.thrift.generated.TransactionState.VALID;
import static com.credits.general.util.CriticalSection.doSafe;
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
    private TableView<TransactionTabRow> tabTransaction;
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

        for (int i = 1; i <= INIT_PAGE_SIZE; i++) {
            cbPageSize.getItems().add(INIT_PAGE_SIZE * i);
        }

        tabTransaction.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("innerId"));
        tabTransaction.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("source"));
        tabTransaction.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("target"));
        tabTransaction.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("amount"));
        tabTransaction.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("state"));


        cbPageSize.getSelectionModel().select(0);
        setPage();
        fillTable();

        cbPageSize.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            pageSize = cbPageSize.getItems().get((int) newValue);
            pageNumber = 1;
            setPage();

            fillTable();
        });

        tabTransaction.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                TransactionTabRow tabRow = tabTransaction.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    selectedTransactionRow = tabRow;
                    detailFromHistory = true;
                    VistaNavigator.loadVista(VistaNavigator.TRANSACTION);
                }
            }
        });
    }

    private void fillTable() {
        tabTransaction.getItems().clear();
        async(() -> nodeApiService.getTransactions(account, (pageNumber - 1) * pageSize, pageSize),
            handleGetTransactionsResult());
    }

    private Callback<List<TransactionData>> handleGetTransactionsResult() {
        return new Callback<List<TransactionData>>() {

            @Override
            public void onSuccess(List<TransactionData> transactionsList) throws CreditsException {
                btnNext.setDisable(transactionsList.size() < pageSize);

                if (Utils.sourceMap.get(account) != null) {
                    ConcurrentHashMap<Long, TransactionRoundData> sourceTransactionMap = Utils.sourceMap.get(account);
                    List<Long> validIds =
                        transactionsList.stream().map(TransactionData::getId).collect(Collectors.toList());
                    sourceTransactionMap.remove(validIds);
                    List<Long> ids = new ArrayList<>(sourceTransactionMap.keySet());
                    Lock lock = new ReentrantLock();
                    async(() -> nodeApiService.getTransactionsState(account, ids),
                        handleGetTransactionsStateResult(sourceTransactionMap, lock));
                }

                transactionsList.forEach(transactionData -> {
                    TransactionTabRow tableRow = new TransactionTabRow();
                    tableRow.setAmount(Converter.toString(transactionData.getAmount()));
                    tableRow.setSource(Converter.encodeToBASE58(transactionData.getSource()));
                    tableRow.setTarget(Converter.encodeToBASE58(transactionData.getTarget()));
                    tableRow.setInnerId(String.valueOf(transactionData.getId()));
                    tableRow.setState(VALID.name());
                    tabTransaction.getItems().add(tableRow);
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
        ConcurrentHashMap<Long, TransactionRoundData> transactionMap, Lock lock) {
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
                transactionMap.entrySet().removeIf(e -> e.getValue().getRoundNumber() != 0 && curRound >= e.getValue().getRoundNumber() + COUNT_ROUNDS_LIFE);

                transactionMap.forEach((key, value) -> {
                    TransactionTabRow tableRow = new TransactionTabRow();
                    tableRow.setInnerId(key.toString());
                    tableRow.setAmount(value.getAmount());
                    tableRow.setCurrency(value.getCurrency());
                    tableRow.setTarget(value.getTarget());
                    if (states.get(key) != null) {
                        if (states.get(key).getValue() == INVALID.getValue()) {
                            tableRow.setState(INVALID.name());
                        } else if (curRound == 0 || states.get(key).getValue() == INPROGRESS.getValue()) {
                            tableRow.setState(INPROGRESS.name());
                        }
                        doSafe(() -> tabTransaction.getItems().add(tableRow), lock);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                doSafe(() -> tabTransaction.getItems().clear(), lock);
            }
        };
    }


    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @FXML
    private void handleRefresh() {
        fillTable();
    }

    @FXML
    private void handlePageFirst() {
        pageNumber = FIRST_PAGE_NUMBER;
        setPage();
        fillTable();
    }

    @FXML
    private void handlePagePrev() {
        if (pageNumber > FIRST_PAGE_NUMBER) {
            pageNumber = pageNumber - 1;
        }
        setPage();
        fillTable();
    }

    @FXML
    private void handlePageNext() {
        pageNumber = pageNumber + 1;
        setPage();
        fillTable();
    }

    private void setPage() {
        btnFirst.setDisable(pageNumber <= FIRST_PAGE_NUMBER);
        btnPrev.setDisable(pageNumber <= FIRST_PAGE_NUMBER);
        labPage.setText(Integer.toString(pageNumber));
    }
}

