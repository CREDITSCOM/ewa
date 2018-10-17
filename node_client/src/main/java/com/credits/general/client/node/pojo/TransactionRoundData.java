package com.credits.general.client.node.pojo;

import java.io.Serializable;

public class TransactionRoundData implements Serializable {
    private static final long serialVersionUID = 5544650022718657167L;
    private TransactionData transaction;
    private Integer roundNumber;
    /*private State state;*/

    public TransactionRoundData(TransactionData transaction) {
        this.transaction = transaction;
    }

    public TransactionRoundData(TransactionData transaction, Integer roundNumber) {
        this.transaction = transaction;
        this.roundNumber = roundNumber;
    }

    public TransactionData getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionData transaction) {
        this.transaction = transaction;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }
}
