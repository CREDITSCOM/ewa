package com.credits.client.node.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class TransactionData implements Serializable {
    private static final long serialVersionUID = 4544650022718657167L;
    private Long id;
    private byte[] source;
    private byte[] target;
    private BigDecimal amount;
    private BigDecimal balance;
    private byte currency;

    public TransactionData(){}

    public TransactionData(Long id, byte[] source, byte[] target, BigDecimal amount) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }

    public byte[] getTarget() {
        return target;
    }

    public void setTarget(byte[] target) {
        this.target = target;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public byte getCurrency() {
        return currency;
    }

    public void setCurrency(byte currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("{id : %s, account : %s, target : %s, amount : %s, balance : %s, currency : %s}",
            this.id,
            Arrays.toString(this.source),
            Arrays.toString(this.target),
            this.amount,
            this.balance,
            this.currency
        );
    }
}
