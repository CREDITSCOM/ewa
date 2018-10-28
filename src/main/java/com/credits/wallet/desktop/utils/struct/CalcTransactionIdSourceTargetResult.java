package com.credits.wallet.desktop.utils.struct;


import com.credits.general.util.Converter;
import com.credits.general.util.exception.ConverterException;

public class CalcTransactionIdSourceTargetResult {
    private long transactionId;
    private String source;
    private String target;

    public CalcTransactionIdSourceTargetResult(long transactionId, String source, String target) {
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public byte[] getByteSource() throws ConverterException {
        return Converter.decodeFromBASE58(source);
    }
    public byte[] getByteTarget() throws ConverterException {
        return Converter.decodeFromBASE58(target);
    }
}
