package com.credits.wallet.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.11.2017.
 */
public class AccountController extends Controller implements Initializable {

    @FXML
    private Label wallet;

    @FXML
    private Label balance;

    public void initialize(URL location, ResourceBundle resources) {
        this.wallet.setText("wallet dummy");

        this.balance.setText("balance dummy");
    }
}
