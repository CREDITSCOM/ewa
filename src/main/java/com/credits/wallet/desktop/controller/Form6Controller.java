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
 * Created by goncharov-eg on 18.01.2018.
 */
public class Form6Controller extends Controller implements Initializable {
    @FXML
    private Label labCredit;

    @FXML
    private TextField txKey;

    @FXML
    private Spinner<Double> numAmount;

    @FXML
    private Spinner<Double> numFee;

    @FXML
    private Label labFee;

    @FXML
    private void handleGenerate() {
        System.out.println(numFee.getValue());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labCredit.setText("0");
        txKey.setText("CSx5893eff21fd9c79463d127b3d3512b38dd05a42402c079e4a45d7f00a52e8");

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
                        0.0, 1.0);
        amountValueFactory.setConverter(converter);
        numAmount.setValueFactory(amountValueFactory);

        SpinnerValueFactory<Double> feeValueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE,
                        0.0006758, 1);
        feeValueFactory.setConverter(converter);
        numFee.setValueFactory(feeValueFactory);

        labFee.setText("0.00051");
    }
}
