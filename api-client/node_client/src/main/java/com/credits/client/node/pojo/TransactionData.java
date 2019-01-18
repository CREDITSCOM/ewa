package com.credits.client.node.pojo;

import com.credits.general.pojo.VariantData;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class TransactionData implements Serializable {
    private static final long serialVersionUID = 4544650022718657167L;
    protected long id;
    protected String blockId;
    protected byte[] source;
    protected byte[] target;
    protected BigDecimal amount;
    protected byte currency = (byte) 1;
    protected byte[] commentBytes;
    protected String method;
    protected List<VariantData> params;


    public TransactionData(){}

    public TransactionData(Long id, byte[] source, byte[] target, BigDecimal amount) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.amount = amount;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
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

    public byte[] getCommentBytes() {
        return commentBytes;
    }

    public void setCommentBytes(byte[] commentBytes) {
        this.commentBytes = commentBytes;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<VariantData> getParams() {
        return params;
    }

    public void setParams(List<VariantData> params) {
        this.params = params;
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
            Arrays.equals(target, that.target) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(id, amount, currency);
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
        sb.append(", currency=").append(currency);
        sb.append('}');
        return sb.toString();
    }
}
