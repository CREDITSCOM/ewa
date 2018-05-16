package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.leveldb.client.exception.ApiClientException;
import com.credits.leveldb.client.util.Validator;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Dictionaries;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form6Controller extends Controller implements Initializable {

    private static Logger LOGGER = LoggerFactory.getLogger(Form6Controller.class);

    private static final String ERR_COIN = "Coin must be selected";
    private static final String ERR_AMOUNT = "Amount must be greater than 0";
    private static final String ERR_FEE = "Fee must be greater than 0";
    private static final String ERR_TO_ADDRESS = "To address must not be empty";

    private static final String ERR_GETTING_BALANCE = "Error getting balance";

    @FXML
    private Label labCredit;

    @FXML
    private Label labErrorCoin;
    @FXML
    private Label labErrorKey;
    @FXML
    private Label labErrorAmount;
    @FXML
    private Label labErrorFee;

    @FXML
    private TextField txKey;

    @FXML
    private TextField numAmount;

    @FXML
    private ComboBox<String> cbCoin;

    @FXML
    private TextField numFee;

    private void refreshTransactionFeePercent(BigDecimal transactionFeeValue, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            AppState.transactionFeePercent = BigDecimal.ZERO;
        } else {
            AppState.transactionFeePercent = (transactionFeeValue.multiply(new BigDecimal("100"))).divide(amount, 18, RoundingMode.HALF_UP);
        }
    }

    @FXML
    private void handleGenerate() throws CreditsException {

        AppState.amount = Converter.toBigDecimal(numAmount.getText());
        AppState.toAddress = txKey.getText();
        AppState.innerId = ApiUtils.generateTransactionInnerId();

        // VALIDATE
        boolean ok = true;
        clearLabErr();
        if (AppState.coin == null || AppState.coin.isEmpty()) {
            labErrorCoin.setText(ERR_COIN);
            cbCoin.setStyle(cbCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            ok = false;
        }
        if (AppState.toAddress == null || AppState.toAddress.isEmpty()) {
            labErrorKey.setText(ERR_TO_ADDRESS);
            txKey.setStyle(txKey.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            ok = false;
        }
        if (AppState.amount.compareTo(BigDecimal.ZERO) <= 0) {
            labErrorAmount.setText(ERR_AMOUNT);
            numAmount.setStyle(numAmount.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            ok = false;
        }
        if (AppState.transactionFeeValue.compareTo(BigDecimal.ZERO) <= 0) {
            labErrorFee.setText(ERR_FEE);
            numFee.setStyle(numFee.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            ok = false;
        }
        try {
            Validator.validateToAddress(AppState.toAddress);
        } catch (ApiClientException e) {
            labErrorKey.setText("Invalid Address");
            txKey.setStyle(txKey.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            ok = false;
        }
        // --------

        if (ok) {
            App.showForm("/fxml/form7.fxml", "Wallet");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clearLabErr();

        labCredit.setText("0");

        numFee.setText(Converter.toString(AppState.transactionFeeValue));

        // Fill coin list
        cbCoin.getItems().clear();
        AppState.coins.clear();
        for (String[] coin : Dictionaries.currencies) {
            cbCoin.getItems().add(coin[0] + " (" + coin[1] + ")");
            AppState.coins.add(coin[1]);
        }
        try {
            FileInputStream fis = new FileInputStream("coins.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(";");
                cbCoin.getItems().add(s[0] + " (" + s[1] + ")");
                AppState.coins.add(s[1]);
            }
            br.close();
        } catch (Exception e) {
            // do nothing - no user's coins
        }

        cbCoin.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                AppState.coin=AppState.coins.get((int) newValue);
                Utils.displayBalance(AppState.coin, labCredit);
            }
        });

        this.numAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                refreshTransactionFeePercent(Converter.toBigDecimal(this.numFee.getText()), Converter.toBigDecimal(newValue));
            } catch (CreditsException e) {
                LOGGER.error(e.getMessage());
            }
        });

        this.numFee.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                refreshTransactionFeePercent(Converter.toBigDecimal(newValue), Converter.toBigDecimal(this.numAmount.getText()));
            } catch (CreditsException e) {
                LOGGER.error(e.getMessage());
            }
        });

        this.numAmount.setOnKeyReleased(event -> {
            String s1 = this.numAmount.getText();
            String s2 = Utils.correctNum(s1);
            if (!s1.equals(s2)) {
                this.numAmount.setText(s2);
                this.numAmount.positionCaret(s2.length());
            }
        });

        this.numFee.setOnKeyReleased(event -> {
            String s1 = this.numFee.getText();
            String s2 = Utils.correctNum(s1);
            if (!s1.equals(s2)) {
                this.numFee.setText(s2);
                this.numFee.positionCaret(s2.length());
            }
        });

        if (AppState.noClearForm6) {
            cbCoin.getSelectionModel().select(AppState.coin);
            txKey.setText(AppState.toAddress);
            numAmount.setText(Converter.toString(AppState.amount));
            numFee.setText(Converter.toString(AppState.transactionFeeValue));

            AppState.noClearForm6 = false;
        } else {
            if (cbCoin.getItems().size() > 0) {
                cbCoin.getSelectionModel().select(0);
            }
        }
    }

    @FXML
    private void handleNewCoin() {
        App.showForm("/fxml/new_coin.fxml", "Wallet");
    }

    private BigDecimal getBalance(String coin) throws Exception {
        return AppState.apiClient.getBalance(AppState.account, coin);
    }

    private void clearLabErr() {
        labErrorCoin.setText("");
        labErrorAmount.setText("");
        labErrorFee.setText("");
        labErrorKey.setText("");

        cbCoin.setStyle(cbCoin.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        txKey.setStyle(txKey.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        numAmount.setStyle(numAmount.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        numFee.setStyle(numFee.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
    }
}
