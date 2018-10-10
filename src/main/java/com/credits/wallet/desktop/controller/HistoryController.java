package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.utils.Converter;
import com.credits.leveldb.client.data.TransactionData;
import com.credits.leveldb.client.data.TransactionRoundData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.service.LevelDbServiceImpl;
import com.credits.leveldb.client.thrift.TransactionState;
import com.credits.leveldb.client.thrift.TransactionsStateGetResult;
import com.credits.wallet.desktop.AppState;
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

/**
 * Created by goncharov-eg on 29.01.2018.
 */
public class HistoryController extends Controller implements Initializable {
    private static final String ERR_GETTING_TRANSACTION_HISTORY = "Error getting transaction history";

    private static final int INIT_PAGE_SIZE = 10;
    private static final int FIRST_PAGE_NUMBER = 1;

    private final static Logger LOGGER = LoggerFactory.getLogger(HistoryController.class);
    public static final int COUNT_ROUNDS_LIFE = 10;

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
        tabTransaction.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("target"));
        tabTransaction.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("currency"));
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
                    AppState.selectedTransactionRow = tabRow;
                    AppState.detailFromHistory = true;
                    VistaNavigator.loadVista(VistaNavigator.TRANSACTION);
                }
            }
        });
    }

    private void fillTable() {
        tabTransaction.getItems().clear();
        List<TransactionData> transactionList;
        try {
            transactionList = AppState.levelDbService.getTransactions(AppState.account,
                (pageNumber - 1) * pageSize, pageSize);
        } catch (LevelDbClientException | CreditsCommonException e) {
            LOGGER.error(ERR_GETTING_TRANSACTION_HISTORY, e);
            FormUtils.showError(ERR_GETTING_TRANSACTION_HISTORY);

            LOGGER.error(AppState.NODE_ERROR + ": " + e.toString(), e);
            FormUtils.showError(AppState.NODE_ERROR);

            return;
        } catch (CreditsNodeException e) {
            LOGGER.error(ERR_GETTING_TRANSACTION_HISTORY, e);
            FormUtils.showError(ERR_GETTING_TRANSACTION_HISTORY);

            LOGGER.error(AppState.NODE_ERROR + ": " + e.getMessage(), e);
            FormUtils.showError(AppState.NODE_ERROR);

            return;
        }

        btnNext.setDisable(transactionList.size() < pageSize);


        try {
            if (LevelDbServiceImpl.sourceMap != null && LevelDbServiceImpl.sourceMap.get(AppState.account) != null) {
                Map<Long, TransactionRoundData> sourceTransactionMap =
                    LevelDbServiceImpl.sourceMap.get(AppState.account);
                synchronized (sourceTransactionMap) {
                    List<Long> ids = new ArrayList<>(sourceTransactionMap.keySet());
                    TransactionsStateGetResult transactionsStateResult =
                        AppState.levelDbService.getTransactionsState(AppState.account, ids);

                    Map<Long, TransactionState> states = transactionsStateResult.getStates();
                    states.forEach((k, v) -> {
                        if (v.getValue() == TransactionState.VALID.getValue()) {
                            sourceTransactionMap.remove(k);
                        }
                    });

                    int curRound = transactionsStateResult.getRoundNum();
                    sourceTransactionMap.entrySet()
                        .removeIf(e -> e.getValue().getRoundNumber() == null ||
                            curRound >= e.getValue().getRoundNumber() + COUNT_ROUNDS_LIFE);

                    sourceTransactionMap.forEach((key, value) -> {
                        TransactionTabRow tableRow = new TransactionTabRow();
                        TransactionData transaction = value.getTransaction();
                        tableRow.setInnerId(key.toString());
                        tableRow.setAmount(Converter.toString(transaction.getAmount()));
                        tableRow.setCurrency(transaction.getCurrency());
                        tableRow.setTarget(Converter.encodeToBASE58(transaction.getTarget()));
                        if(states.get(key)!=null) {
                            if(states.get(key).getValue()==TransactionState.INVALID.getValue()) {
                                tableRow.setState("INVALID");
                            }
                        }
                        if(states.get(key).getValue()==TransactionState.INPROGRES.getValue()) {
                            tableRow.setState("INPROGRESS");
                        }
                        tabTransaction.getItems().add(tableRow);
                    });
                }
            }

        } catch (CreditsNodeException | LevelDbClientException | CreditsCommonException e) {
            tabTransaction.getItems().clear();
        }


        transactionList.forEach(transactionData -> {
            TransactionTabRow tableRow = new TransactionTabRow();
            tableRow.setAmount(Converter.toString(transactionData.getAmount()));
            tableRow.setCurrency(transactionData.getCurrency());
            tableRow.setTarget(Converter.encodeToBASE58(transactionData.getTarget()));
            tableRow.setInnerId(transactionData.getId().toString());
            tableRow.setState("VALID");
            tabTransaction.getItems().add(tableRow);
        });
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

