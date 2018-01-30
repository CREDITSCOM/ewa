package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.*;

/**
 * Created by goncharov-eg on 29.01.2018.
 */
public class HistoryController extends Controller implements Initializable {
    private static final String ERR_GETTING_TRANSACTION_HISTORY="Error getting transaction history";

    private int pageNumber;

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
        for (int i=0; i<10; i++)
            cbPageSize.getItems().add(10*(i+1));
        cbPageSize.getSelectionModel().select(0);

        TableColumn[] tableColumns = new TableColumn[tabTransaction.getColumns().size()];
        for (int i=0; i<tabTransaction.getColumns().size(); i++) {
            tableColumns[i] = (TableColumn) tabTransaction.getColumns().get(i);
        }
        tableColumns[0].setCellValueFactory(new PropertyValueFactory<TabRow, String>("target"));
        tableColumns[1].setCellValueFactory(new PropertyValueFactory<TabRow, String>("currency"));
        tableColumns[2].setCellValueFactory(new PropertyValueFactory<TabRow, String>("amount"));
        tableColumns[3].setCellValueFactory(new PropertyValueFactory<TabRow, String>("fee"));
        tableColumns[4].setCellValueFactory(new PropertyValueFactory<TabRow, String>("time"));

        pageNumber=1;
        setPage();

        fillTable();

        cbPageSize.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pageNumber=1;
                setPage();

                fillTable();
            }
        });

    }

    private void fillTable() {
        tabTransaction.getItems().clear();

        String transactionHistory = Utils.callAPI("gettransactions?account=" + AppState.account+"&currency=", ERR_GETTING_TRANSACTION_HISTORY);
        if (transactionHistory!=null) {
            JsonElement jelement = new JsonParser().parse(transactionHistory);
            JsonArray jTransactions=jelement.getAsJsonObject().get("response").getAsJsonArray();
            for (int i=0; i<jTransactions.size(); i++) {
                TabRow tr = new TabRow();

                tr.setTarget(jTransactions.get(i).getAsJsonObject().get("target").getAsString());
                tr.setCurrency(jTransactions.get(i).getAsJsonObject().get("currency").getAsString());
                JsonObject jAmount=jTransactions.get(i).getAsJsonObject().get("amount").getAsJsonObject();
                JsonObject jFee=jTransactions.get(i).getAsJsonObject().get("fee").getAsJsonObject();
                String amountStr=Long.toString(jAmount.get("integral").getAsLong())+
                        "."+Long.toString(jAmount.get("fraction").getAsLong());
                String feeStr=Long.toString(jFee.get("integral").getAsLong())+
                        "."+Long.toString(jFee.get("fraction").getAsLong());
                tr.setAmount(amountStr);
                tr.setFee(feeStr);
                tr.setTime((new Date(jTransactions.get(i).getAsJsonObject().get("time").getAsLong())).toString());

                tabTransaction.getItems().add(tr);
            }
        }
    }

    public static class TabRow {
        String target;
        String currency;
        String amount;
        String fee;
        String time;

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getFee() {
            return fee;
        }

        public void setFee(String fee) {
            this.fee = fee;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handlePageFirst() {
        pageNumber=1;
        setPage();
    }

    @FXML
    private void handlePagePrev() {
        if (pageNumber>1)
            pageNumber=pageNumber-1;
        setPage();
    }

    @FXML
    private void handlePageNext() {
        pageNumber=pageNumber+1;
        setPage();
    }

    @FXML
    private void handlePageLast() {

    }

    private void setPage() {
        btnFirst.setDisable(pageNumber<=1);
        btnPrev.setDisable(pageNumber<=1);

        labPage.setText(Integer.toString(pageNumber));
    }
}

