package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.leveldb.client.data.TransactionData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.struct.TransactionTabRow;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 29.01.2018.
 */
public class HistoryController extends Controller implements Initializable {
    private static final String ERR_GETTING_TRANSACTION_HISTORY = "Error getting transaction history";

    private static final int INIT_PAGE_SIZE = 10;
    private static final int FIRST_PAGE_NUMBER = 1;

    private final static Logger LOGGER = LoggerFactory.getLogger(HistoryController.class);

    private int pageNumber = FIRST_PAGE_NUMBER;
    private int pageSize = INIT_PAGE_SIZE;

    @FXML
    private ComboBox<Integer> cbPageSize;

    @FXML
    private TableView tabTransaction;

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
        for (int i = 1; i <= INIT_PAGE_SIZE; i++) {
            cbPageSize.getItems().add(INIT_PAGE_SIZE * i);
        }

        TableColumn[] tableColumns = new TableColumn[tabTransaction.getColumns().size()];
        for (int i = 0; i < tabTransaction.getColumns().size(); i++) {
            tableColumns[i] = (TableColumn) tabTransaction.getColumns().get(i);
        }
        tableColumns[0].setCellValueFactory(new PropertyValueFactory<TransactionTabRow, String>("innerId"));
        tableColumns[1].setCellValueFactory(new PropertyValueFactory<TransactionTabRow, String>("target"));
        tableColumns[2].setCellValueFactory(new PropertyValueFactory<TransactionTabRow, String>("currency"));
        tableColumns[3].setCellValueFactory(new PropertyValueFactory<TransactionTabRow, String>("amount"));

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
                TransactionTabRow tabRow = (TransactionTabRow) tabTransaction.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    AppState.selectedTransactionRow = tabRow;
                    AppState.detailFromHistory = true;
                    App.showForm("/fxml/transaction.fxml", "Wallet");
                }
            }
        });
    }

    private void fillTable() {
        tabTransaction.getItems().clear();
        List<TransactionData> transactionList;
        try {
            transactionList =
                AppState.apiClient.getTransactions(AppState.account, (pageNumber - 1) * pageSize, pageSize);
        } catch (LevelDbClientException e) {
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

        transactionList.forEach(transactionData -> {
            TransactionTabRow tableRow = new TransactionTabRow();
            tableRow.setAmount(Converter.toString(transactionData.getAmount()));
            tableRow.setCurrency(transactionData.getCurrency());
            tableRow.setTarget(transactionData.getTarget());
            tableRow.setInnerId(transactionData.getInnerId());
            tabTransaction.getItems().add(tableRow);

        });

        /*
        tabTransaction.setItems(
            FXCollections.observableList(transactionList.stream()
                .map(transaction -> {
                    TransactionHistoryTableRow tr = new TransactionHistoryTableRow();
                    tr.setTarget(transaction.getTarget());
                    tr.setCurrency(transaction.getCurrency());
                    tr.setAmount(Converter.toString(transaction.getAmount()));
                    tr.setId(transaction.getInnerId());
                    return tr;
                })
                .collect(Collectors.toList()))
        );
        */
    }

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form6.fxml", "Wallet");
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

