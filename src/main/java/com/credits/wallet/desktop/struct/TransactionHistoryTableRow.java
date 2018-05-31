package com.credits.wallet.desktop.struct;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class TransactionHistoryTableRow {
    private StringProperty id;
    private StringProperty target;
    private StringProperty currency;
    private StringProperty amount;

    public String getId() {
        return idProperty().get();
    }

    public void setId(String id) {
        idProperty().set(id);
    }

    public StringProperty idProperty() {
        if (id == null)  {
            id = new SimpleStringProperty(this, "id");
        }
        return id;
    }

    public String getTarget() {
        return targetProperty().get();
    }

    public void setTarget(String target) {
        targetProperty().set(target);
    }

    public StringProperty targetProperty() {
        if (target == null)  {
            target = new SimpleStringProperty(this, "target");
        }
        return target;
    }

    public String getCurrency() {
        return currencyProperty().get();
    }

    public void setCurrency(String currency) {
        currencyProperty().set(currency);
    }

    public StringProperty currencyProperty() {
        if (currency == null)  {
            currency = new SimpleStringProperty(this, "currency");
        }
        return currency;
    }

    public String getAmount() {
        return amountProperty().get();
    }

    public void setAmount(String amount) {
        amountProperty().set(amount);
    }

    public StringProperty amountProperty() {
        if (amount == null)  {
            amount = new SimpleStringProperty(this, "amount");
        }
        return amount;
    }
}
