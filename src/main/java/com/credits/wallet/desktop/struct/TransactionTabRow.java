package com.credits.wallet.desktop.struct;

/**
 * Created by Rustem Saidaliyev on 25.06.2018.
 */
public class TransactionTabRow {
    private String innerId;
    private String target;
    private String currency;
    private String amount;

    public String getInnerId() {
        return innerId;
    }

    public void setInnerId(String innerId) {
        this.innerId = innerId;
    }

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
}
