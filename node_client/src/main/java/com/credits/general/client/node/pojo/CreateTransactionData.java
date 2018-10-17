package com.credits.general.client.node.pojo;

import java.math.BigDecimal;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class CreateTransactionData {

    private long innerId;
    private byte[] source;
    private byte[] target;
    private BigDecimal amount;
    private BigDecimal balance;
    private byte currency;
    private Short offeredMaxFee;
    private byte[] signature;

    public CreateTransactionData(
            long innerId,
            byte[] source,
            byte[] target,
            BigDecimal amount,
            BigDecimal balance,
            byte currency,
            Short offeredMaxFee,
            byte[] signature
    ) {
        this.setInnerId(innerId);
        this.setSource(source);
        this.setTarget(target);
        this.setAmount(amount);
        this.setBalance(balance);
        this.setCurrency(currency);
        this.setOfferedMaxFee(offeredMaxFee);
        this.setSignature(signature);
    }

    public long getInnerId() {
        return innerId;
    }

    public void setInnerId(long innerId) {
        this.innerId = innerId;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public byte getCurrency() {
        return currency;
    }

    public void setCurrency(byte currency) {
        this.currency = currency;
    }

    public Short getOfferedMaxFee() {
        return offeredMaxFee;
    }

    public void setOfferedMaxFee(Short offeredMaxFee) {
        this.offeredMaxFee = offeredMaxFee;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
}