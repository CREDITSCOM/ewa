package com.credits.wallet.desktop.controller;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionStateData;
import com.credits.client.node.pojo.TransactionsStateGetResultData;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.general.util.Callback;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.TransactionTabRow;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.client.node.service.NodeApiServiceImpl.async;
import static com.credits.client.node.thrift.generated.TransactionState.*;
import static com.credits.wallet.desktop.AppState.NODE_ERROR;


public class HistoryController extends AbstractController {
    private final String ERR_GETTING_TRANSACTION_HISTORY = "Error getting transaction history";
    private final int INIT_PAGE_SIZE = 100;
    private final int FIRST_TRANSACTION_NUMBER = 0;
    private final static Logger LOGGER = LoggerFactory.getLogger(HistoryController.class);
    private final int COUNT_ROUNDS_LIFE = 50;

    @FXML
    private TableView<TransactionTabRow> approvedTableView;

    @FXML
    private TableView<TransactionTabRow> unapprovedTableView;


    @Override
    public void initializeForm(Map<String, Object> objects) {
        initTable(approvedTableView);
        initTable(unapprovedTableView);

        fillApprovedTable();
        fillUnapprovedTable();
    }

    private void initTable(TableView<TransactionTabRow> tableView) {
        tableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("blockId"));
        tableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("source"));
        tableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("target"));
        tableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("amount"));
        tableView.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("state"));
        tableView.setOnMousePressed(event -> {
            if ((event.isPrimaryButtonDown()|| event.getButton() == MouseButton.PRIMARY) && event.getClickCount() == 2) {
                TransactionTabRow tabRow = tableView.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("selectedTransactionRow",tabRow);
                    VistaNavigator.showFormModal(VistaNavigator.TRANSACTION, params);
                }
            }
        });
    }

    private void fillUnapprovedTable() {
        if (session.sourceMap !=null) {
            ConcurrentHashMap<Long, TransactionRoundData> sourceTransactionMap = session.sourceMap;
            /*List<Long> validIds =
                transactionsList.stream().map(TransactionData::getId).collect(Collectors.toList());
            sourceTransactionMap.remove(validIds)*/
            List<Long> ids = new ArrayList<>(sourceTransactionMap.keySet());
            async(() -> AppState.getNodeApiService().getTransactionsState(session.account, ids),
                handleGetTransactionsStateResult(sourceTransactionMap));
        }
    }

    private void fillApprovedTable() {
        async(() -> AppState.getNodeApiService().getTransactions(session.account, FIRST_TRANSACTION_NUMBER, INIT_PAGE_SIZE),
            handleGetTransactionsResult());
    }

    private Callback<List<TransactionData>> handleGetTransactionsResult() {
        return new Callback<List<TransactionData>>() {

            @Override
            public void onSuccess(List<TransactionData> transactionsList) throws CreditsException {

                List<TransactionTabRow> approvedList = new ArrayList<>();
                transactionsList.forEach(transactionData -> {
                    TransactionTabRow tableRow = new TransactionTabRow();
                    tableRow.setAmount(GeneralConverter.toString(transactionData.getAmount()));
                    tableRow.setSource(GeneralConverter.encodeToBASE58(transactionData.getSource()));
                    tableRow.setTarget(GeneralConverter.encodeToBASE58(transactionData.getTarget()));
                    tableRow.setBlockId(transactionData.getBlockId());
                    tableRow.setState(VALID.name());
                    tableRow.setMethod(transactionData.getMethod());
                    tableRow.setParams(transactionData.getParams());
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

                List<TransactionTabRow> unapprovedList = new ArrayList<>();

                transactionMap.forEach((id, value) -> {
                    TransactionTabRow tableRow = new TransactionTabRow();
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
                refreshTableViewItems(unapprovedTableView, unapprovedList);


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

    @Override
    public void formDeinitialize() {

    }
}

