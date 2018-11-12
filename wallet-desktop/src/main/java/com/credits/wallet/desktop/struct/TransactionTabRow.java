package com.credits.wallet.desktop.struct;

/**
 * Created by Rustem Saidaliyev on 25.06.2018.
 */
public class TransactionTabRow {
    private Long innerId;
    private String source;
    private String target;
    private String currency;
    private String amount;
    private String state;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getInnerId() {
        return innerId;
    }

    public void setInnerId(Long innerId) {
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
