package com.credits.wallet.desktop.struct;

import com.credits.general.thrift.generated.Variant;

import java.util.List;


public class TransactionTabRow {
    private Long innerId;
    private String blockId;
    private String source;
    private String target;
    private String currency;
    private String amount;
    private String state;
    private String method;
    private List<Variant> params;

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

    public void setBlockId(String blockTransactionId) {
        this.blockId = blockTransactionId;
    }

    public String getBlockId() {
        return blockId;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Variant> getParams() {
        return params;
    }

    public void setParams(List<Variant> params) {
        this.params = params;
    }
}
