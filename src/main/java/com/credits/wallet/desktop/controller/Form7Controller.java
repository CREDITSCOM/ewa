package com.credits.wallet.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class Form7Controller extends Controller implements Initializable {

    @FXML
    private TextField toAddress;

    @FXML
    private Label amountInCs;

    @FXML
    private Label transactionFeeValue;

    @FXML
    private Label transactionFee;

    @FXML
    private TextField transactionCode;

    @FXML
    private void handleGenerate() {
        app.showForm("/fxml/form8.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
