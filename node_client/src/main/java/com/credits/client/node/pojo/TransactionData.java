package com.credits.client.node.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class TransactionData implements Serializable {
    protected static final long serialVersionUID = 4544650022718657167L;
    protected long id;
    protected byte[] source;
    protected byte[] target;
    protected BigDecimal amount;
    protected BigDecimal balance;
    protected byte currency;


    public TransactionData(){}

    public TransactionData(Long id, byte[] source, byte[] target, BigDecimal amount) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.amount = amount;
    }

    public long getId() {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionData)) {
            return false;
        }
        TransactionData that = (TransactionData) o;
        return id == that.id && currency == that.currency && Arrays.equals(source, that.source) &&
            Arrays.equals(target, that.target) && Objects.equals(amount, that.amount) &&
            Objects.equals(balance, that.balance);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(id, amount, balance, currency);
        result = 31 * result + Arrays.hashCode(source);
        result = 31 * result + Arrays.hashCode(target);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionData{");
        sb.append("id=").append(id);
        sb.append(", source=").append(Arrays.toString(source));
        sb.append(", target=").append(Arrays.toString(target));
        sb.append(", amount=").append(amount);
        sb.append(", balance=").append(balance);
        sb.append(", currency=").append(currency);
        sb.append('}');
        return sb.toString();
    }
}
