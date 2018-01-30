package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Dictionaries;
import com.credits.wallet.desktop.Utils;
import com.credits.wallet.desktop.utils.Convertor;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ResourceBundle;

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

    /**
     * c&p from Spinner
     */
    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
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
        /*
        // get a localized format for parsing
        NumberFormat format = NumberFormat.getNumberInstance();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                // NumberFormat evaluates the beginning of the text
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 ||
                        parsePosition.getIndex() < c.getControlNewText().length()) {
                    // reject parsing the complete text failed
                    return null;
                }
            }
            return c;
        };

        TextFormatter<Double> amountFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, filter);

        numAmount.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                -Double.MAX_VALUE, Double.MAX_VALUE, 0.0, 1.0));
        numAmount.setEditable(true);
        numAmount.getEditor().setTextFormatter(amountFormatter);

        TextFormatter<Double> transactionFeeFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, filter);

        numFee.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                -Double.MAX_VALUE, Double.MAX_VALUE, 0.0, 1.0));
        numFee.setEditable(true);
        numFee.getEditor().setTextFormatter(transactionFeeFormatter);
        */
        StringConverter converter=new StringConverter<Double>() {
            private final DecimalFormat df = new DecimalFormat("#.##########");

            @Override public String toString(Double value) {
                // If the specified value is null, return a zero-length String
                if (value == null) {
                    return "";
                }

                return df.format(value);
            }

            @Override public Double fromString(String value) {
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
        for (String[] coin : Dictionaries.currencies) {
            cbCoin.getItems().add(coin[0]+" ("+coin[1]+")");
        }

        cbCoin.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                String balanceInfo=Utils.callAPI("getbalance?account="+AppState.account, ERR_GETTING_BALANCE);
                if (balanceInfo!=null) {
                    JsonElement jelement = new JsonParser().parse(balanceInfo);
                    JsonObject jObject=jelement.getAsJsonObject().get("response").getAsJsonObject().get("CS").getAsJsonObject();
                    String balStr=Long.toString(jObject.get("integral").getAsLong())+
                            "."+Long.toString(jObject.get("fraction").getAsLong());
                    labCredit.setText(balStr);
                }
            }
        });

        this.numAmount.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) return;
            //intuitive method on textField, has no effect, though
            //spinner.getEditor().commitValue();
            commitEditorText(this.numAmount);
        });

        this.numFee.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) return;
            //intuitive method on textField, has no effect, though
            //spinner.getEditor().commitValue();
            commitEditorText(this.numFee);
        });

        this.numAmount.valueProperty().addListener((observable, oldValue, newValue) -> {
                refreshTransactionFeePercent(this.numFee.getValue(), newValue);
            }
        );

        this.numFee.valueProperty().addListener((observable, oldValue, newValue) -> {
                refreshTransactionFeePercent(newValue, this.numAmount.getValue());
            }
        );
    }
}
