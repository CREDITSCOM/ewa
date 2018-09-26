package com.credits.wallet.desktop.utils.struct;

public class CalcTransactionIdSourceTargetResult {
    private long transactionId;
    private byte[] source;
    private byte[] target;

    public CalcTransactionIdSourceTargetResult(long transactionId, byte[] source, byte[] target) {
        this.transactionId = transactionId;
        this.source = source;
        this.target = target;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
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
}
