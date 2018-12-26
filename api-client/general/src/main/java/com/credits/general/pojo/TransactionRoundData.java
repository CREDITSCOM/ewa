package com.credits.general.pojo;
import java.io.Serializable;

public class TransactionRoundData implements Serializable {
    private static final long serialVersionUID = 5544650022718657167L;
    private String id;
    private String source;
    private String target;
    private String amount ;
    private String currency;

    public TransactionRoundData(String id, String source, String target, String amount, String currency) {
        this.id = id;
        this.source=source;
        this.target=target;
        this.amount = amount;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public long getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(long roundNumber) {
        this.roundNumber = roundNumber;
    }

    private long roundNumber;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    /*private State state;*/


}
