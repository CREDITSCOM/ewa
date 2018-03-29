package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Dictionaries;
import com.credits.wallet.desktop.utils.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
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
    private ComboBox<String> cbCoin;

    @FXML
    private Spinner<Double> numAmount;

    @FXML
    private Spinner<Double> numFee;

    /**
     * c&p from Spinner
     */
    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) {
            return;
        }
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }

    private void refreshTransactionFeePercent(Double transactionFeeValue, Double amount) {
        if (amount == 0d) {
            AppState.transactionFeePercent = 0d;
        } else {
            AppState.transactionFeePercent = (transactionFeeValue * 100) / amount;
        }
    }

    @FXML
    private void handleGenerate() {
        AppState.amount = numAmount.getValue();
        AppState.toAddress = txKey.getText();

        // VALIDATE
        boolean ok = true;
        clearLabErr();
        if (AppState.coin == null || AppState.coin.isEmpty()) {
            labErrorCoin.setText(ERR_COIN);
            cbCoin.setStyle(cbCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            ok = false;
        }
        if (AppState.amount <= 0) {
            labErrorAmount.setText(ERR_AMOUNT);
            numAmount.setStyle(numAmount.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            ok = false;
        }
        if (AppState.transactionFeeValue <= 0) {
            labErrorFee.setText(ERR_FEE);
            numFee.setStyle(numFee.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            ok = false;
        }
        if (AppState.toAddress == null || AppState.toAddress.isEmpty()) {
            labErrorKey.setText(ERR_TO_ADDRESS);
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
        StringConverter converter = new StringConverter<Double>() {
            private final DecimalFormat df = new DecimalFormat("#.##########");

            @Override
            public String toString(Double value) {
                // If the specified value is null, return a zero-length String
                if (value == null) {
                    return "";
                }

                return df.format(value);
            }

            @Override
            public Double fromString(String value) {
                try {
                    // If the specified value is null or zero-length, return null
                    if (value == null) {
                        return null;
                    }

                    value = value.trim();

                    if (value.length() < 1) {
                        return null;
                    }

                    // Perform the requested parsing
                    return df.parse(value).doubleValue();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        SpinnerValueFactory<Double> amountValueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0, 0.1);
        amountValueFactory.setConverter(converter);
        numAmount.setValueFactory(amountValueFactory);

        SpinnerValueFactory<Double> feeValueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, AppState.transactionFeePercent, 0);
        feeValueFactory.setConverter(converter);
        numFee.setValueFactory(feeValueFactory);

        numFee.getValueFactory().setValue(AppState.transactionFeeValue);

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
                try {
                    AppState.coin=AppState.coins.get((int) newValue);
                    double balance=getBalance(AppState.coins.get((int) newValue));
                    labCredit.setText(Converter.toString(balance));
                } catch (Exception e) {
                    labCredit.setText("");
                    LOGGER.error(ERR_GETTING_BALANCE, e);
                    Utils.showError(ERR_GETTING_BALANCE);
                }
            }
        });

        this.numAmount.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                return;
            }
            //intuitive method on textField, has no effect, though
            //spinner.getEditor().commitValue();
            commitEditorText(this.numAmount);
        });

        this.numFee.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                return;
            }
            //intuitive method on textField, has no effect, though
            //spinner.getEditor().commitValue();
            commitEditorText(this.numFee);
        });

        this.numAmount.valueProperty().addListener((observable, oldValue, newValue) -> {
            refreshTransactionFeePercent(this.numFee.getValue(), newValue);
        });

        this.numFee.valueProperty().addListener((observable, oldValue, newValue) -> {
            refreshTransactionFeePercent(newValue, this.numAmount.getValue());
        });

        this.numAmount.setOnKeyReleased(event -> {
            String s1 = this.numAmount.getEditor().getText();
            String s2 = Utils.correctNum(s1);
            if (!s1.equals(s2)) {
                this.numAmount.getEditor().setText(s2);
                this.numAmount.getEditor().positionCaret(s2.length());
            }
        });

        this.numFee.setOnKeyReleased(event -> {
            String s1 = this.numFee.getEditor().getText();
            String s2 = Utils.correctNum(s1);
            if (!s1.equals(s2)) {
                this.numFee.getEditor().setText(s2);
                this.numFee.getEditor().positionCaret(s2.length());
            }
        });

        if (AppState.noClearForm6) {
            cbCoin.getSelectionModel().select(AppState.coin);
            txKey.setText(AppState.toAddress);
            numAmount.getValueFactory().setValue(AppState.amount);
            numFee.getValueFactory().setValue(AppState.transactionFeeValue);

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

    private double getBalance(String coin) throws Exception {
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
