package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Dictionaries;
import com.credits.wallet.desktop.Utils;
import com.credits.wallet.desktop.utils.Convertor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.*;
import java.util.*;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form6Controller extends Controller implements Initializable {

    private static Logger LOGGER = LoggerFactory.getLogger(Form6Controller.class);

    private static final String ERR_GETTING_BALANCE = "Error getting balance";

    @FXML
    private Label labCredit;

    @FXML
    private TextField txKey;

    @FXML
    private ComboBox<String> cbCoin;

    @FXML
    private Spinner<Double> numAmount;

    @FXML
    private Spinner<Double> numFee;

    @FXML
    private Label labFee;

    private static final String digits="0123456789";
    private List<String> coins=new ArrayList<String>();

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
        AppState.transactionFeePercent = (transactionFeeValue * 100) / amount;
        this.labFee.setText(Convertor.toString(AppState.transactionFeePercent) + " %");
    }

    @FXML
    private void handleGenerate() {
        AppState.amount = numAmount.getValue();
        AppState.transactionFeeValue = numFee.getValue();
        AppState.toAddress = txKey.getText();
        App.showForm("/fxml/form7.fxml", "Wallet");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labCredit.setText("0");
        txKey.setText("CSx5893eff21fd9c79463d127b3d3512b38dd05a42402c079e4a45d7f00a52e8");
        labFee.setText(Convertor.toString(AppState.transactionFeePercent) + " %");
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
            new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE,
                AppState.transactionFeeValue, 0.1);
        amountValueFactory.setConverter(converter);
        numAmount.setValueFactory(amountValueFactory);

        SpinnerValueFactory<Double> feeValueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE,
                AppState.transactionFeePercent, 0.1);
        feeValueFactory.setConverter(converter);
        numFee.setValueFactory(feeValueFactory);


        // Fill coin list
        cbCoin.getItems().clear();
        coins.clear();
        for (String[] coin : Dictionaries.currencies) {
            cbCoin.getItems().add(coin[0] + " (" + coin[1] + ")");
            coins.add(coin[0]);
        }

        cbCoin.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                try {
                    Double balance=AppState.apiClient.getBalance(AppState.account, coins.get((int)newValue));
                    labCredit.setText(Convertor.toString(balance));
                } catch (Exception e) {
                    labCredit.setText("");
                    e.printStackTrace();
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
            String s1=this.numAmount.getEditor().getText();
            String s2=correctNum(s1);
            if (!s1.equals(s2)) {
                this.numAmount.getEditor().setText(s2);
                this.numAmount.getEditor().positionCaret(s2.length());
            }
        });

        this.numFee.setOnKeyReleased(event -> {
            String s1=this.numFee.getEditor().getText();
            String s2=correctNum(s1);
            if (!s1.equals(s2)) {
                this.numFee.getEditor().setText(s2);
                this.numFee.getEditor().positionCaret(s2.length());
            }
        });
    }

    private String correctNum(String s) {
        int i=0;
        boolean wasPoint=false;
        while (i<s.length()) {
            String c=s.substring(i,i+1);
            if (!(c.equals(AppState.decSep) && !wasPoint) && digits.indexOf(c)<0) {
                if (i==0 && s.length()==1)
                    s="";
                else if (i==0)
                    s=s.substring(1);
                else if (i==s.length()-1)
                    s=s.substring(0,i);
                else
                    s=s.substring(0,i)+s.substring(i+1);
            } else
                i++;

            if (c.equals(AppState.decSep))
                wasPoint=true;
        }
        return s;
    }
}
