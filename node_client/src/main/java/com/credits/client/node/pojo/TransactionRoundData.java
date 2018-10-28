package com.credits.client.node.pojo;

import java.io.Serializable;

public class TransactionRoundData implements Serializable {
    private static final long serialVersionUID = 5544650022718657167L;
    private CreateTransactionData transaction;
    private long roundNumber;
    /*private State state;*/

    public TransactionRoundData(CreateTransactionData transaction) {
        this.transaction = transaction;
    }

    public TransactionRoundData(CreateTransactionData transaction, long roundNumber) {
        this.transaction = transaction;
        this.roundNumber = roundNumber;
    }

    public CreateTransactionData getTransaction() {
        return transaction;
    }

    public void setTransaction(CreateTransactionData transaction) {
        this.transaction = transaction;
    }

    public long getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(long roundNumber) {
        this.roundNumber = roundNumber;
    }
}
