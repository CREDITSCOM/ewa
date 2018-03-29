package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.leveldb.client.TransactionData;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.struct.TransactionTabRow;
import com.credits.wallet.desktop.utils.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(HistoryController.class);

    private int pageNumber;
    private int pageSize;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 0; i < 10; i++) {
            cbPageSize.getItems().add(10 * (i + 1));
        }
        cbPageSize.getSelectionModel().select(0);
        pageSize=10;

        TableColumn[] tableColumns = new TableColumn[tabTransaction.getColumns().size()];
        for (int i = 0; i < tabTransaction.getColumns().size(); i++) {
            tableColumns[i] = (TableColumn) tabTransaction.getColumns().get(i);
        }
        tableColumns[0].setCellValueFactory(new PropertyValueFactory<TransactionTabRow, String>("target"));
        tableColumns[1].setCellValueFactory(new PropertyValueFactory<TransactionTabRow, String>("currency"));
        tableColumns[2].setCellValueFactory(new PropertyValueFactory<TransactionTabRow, String>("amount"));
        tableColumns[3].setCellValueFactory(new PropertyValueFactory<TransactionTabRow, String>("hash"));

        pageNumber = 1;
        setPage();

        fillTable();

        cbPageSize.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pageSize=cbPageSize.getItems().get((int)newValue);
                pageNumber = 1;
                setPage();

                fillTable();
            }
        });

        tabTransaction.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    TransactionTabRow tabRow = (TransactionTabRow) tabTransaction.getSelectionModel().getSelectedItem();
                    if (tabRow != null) {
                        AppState.selectedTransactionRow = tabRow;
                        AppState.detailFromHistory=true;
                        App.showForm("/fxml/transaction.fxml", "Wallet");
                    }
                }
            }
        });
    }

    private void fillTable() {
        tabTransaction.getItems().clear();

        try {
            List<TransactionData> transactionList=AppState.apiClient.getTransactions(AppState.account,
                (pageNumber-1)*pageSize, pageSize);
            for (TransactionData transaction : transactionList) {
                TransactionTabRow tr = new TransactionTabRow();

                tr.setTarget(transaction.getTarget());
                tr.setCurrency(transaction.getCurrency());
                tr.setAmount(Converter.toString(transaction.getAmount()));
                tr.setHash(transaction.getHash());
                tr.setId(transaction.getInnerId());

                tabTransaction.getItems().add(tr);
            }

        } catch(Exception e) {
            LOGGER.error(ERR_GETTING_TRANSACTION_HISTORY, e);
            Utils.showError(ERR_GETTING_TRANSACTION_HISTORY);
        }
    }

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handlePageFirst() {
        pageNumber = 1;
        setPage();
        fillTable();
    }

    @FXML
    private void handlePagePrev() {
        if (pageNumber > 1) {
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
        btnFirst.setDisable(pageNumber <= 1);
        btnPrev.setDisable(pageNumber <= 1);

        labPage.setText(Integer.toString(pageNumber));
    }
}

