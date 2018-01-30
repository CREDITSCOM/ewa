package com.credits.wallet.desktop.struct;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class TransactionTabRow {
    private String target;
    private String currency;
    private String amount;
    private String fee;
    private String time;
    private long timeN;

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

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimeN() {
        return timeN;
    }

    public void setTimeN(long timeN) {
        this.timeN = timeN;
    }
}
