package com.credits.client.node.pojo;

import com.credits.general.thrift.generated.Variant;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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
    protected List<Variant> params;
    protected TransactionTypeData type;
    protected SmartTransInfoData smartInfo;



    public TransactionData(){}

    public TransactionData(Long id, byte[] source, byte[] target, BigDecimal amount, TransactionTypeData type) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.amount = amount;
        this.type = type;
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

    public List<Variant> getParams() {
        return params;
    }

    public void setParams(List<Variant> params) {
        this.params = params;
    }

    public TransactionTypeData getType() {
        return type;
    }

    public void setType(TransactionTypeData type) {
        this.type = type;
    }

    public SmartTransInfoData getSmartInfo() {
        return smartInfo;
    }

    public void setSmartInfo(SmartTransInfoData smartInfo) {
        this.smartInfo = smartInfo;
    }
//      TODO спросить у Игоря
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (!(o instanceof TransactionData)) {
//            return false;
//        }
//        TransactionData that = (TransactionData) o;
//        return id == that.id && currency == that.currency && Arrays.equals(source, that.source) &&
//            Arrays.equals(target, that.target) && Objects.equals(amount, that.amount);
//    }
//
//    @Override
//    public int hashCode() {
//
//        int result = Objects.hash(id, amount, currency);
//        result = 31 * result + Arrays.hashCode(source);
//        result = 31 * result + Arrays.hashCode(target);
//        return result;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionData)) return false;

        TransactionData that = (TransactionData) o;

        if (getId() != that.getId()) return false;
        if (getCurrency() != that.getCurrency()) return false;
        if (getBlockId() != null ? !getBlockId().equals(that.getBlockId()) : that.getBlockId() != null) return false;
        if (!Arrays.equals(getSource(), that.getSource())) return false;
        if (!Arrays.equals(getTarget(), that.getTarget())) return false;
        if (getAmount() != null ? !getAmount().equals(that.getAmount()) : that.getAmount() != null) return false;
        if (!Arrays.equals(getCommentBytes(), that.getCommentBytes())) return false;
        if (getMethod() != null ? !getMethod().equals(that.getMethod()) : that.getMethod() != null) return false;
        if (getParams() != null ? !getParams().equals(that.getParams()) : that.getParams() != null) return false;
        if (getType() != that.getType()) return false;
        return getSmartInfo() != null ? getSmartInfo().equals(that.getSmartInfo()) : that.getSmartInfo() == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + (getBlockId() != null ? getBlockId().hashCode() : 0);
        result = 31 * result + Arrays.hashCode(getSource());
        result = 31 * result + Arrays.hashCode(getTarget());
        result = 31 * result + (getAmount() != null ? getAmount().hashCode() : 0);
        result = 31 * result + (int) getCurrency();
        result = 31 * result + Arrays.hashCode(getCommentBytes());
        result = 31 * result + (getMethod() != null ? getMethod().hashCode() : 0);
        result = 31 * result + (getParams() != null ? getParams().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getSmartInfo() != null ? getSmartInfo().hashCode() : 0);
        return result;
    }

//    @Override
//    public String toString() {
//        final StringBuilder sb = new StringBuilder("TransactionData{");
//        sb.append("id=").append(id);
//        sb.append(", source=").append(Arrays.toString(source));
//        sb.append(", target=").append(Arrays.toString(target));
//        sb.append(", amount=").append(amount);
//        sb.append(", currency=").append(currency);
//        sb.append('}');
//        return sb.toString();
//    }


    @Override
    public String toString() {
        return "TransactionData{" +
                "id=" + id +
                ", blockId='" + blockId + '\'' +
                ", source=" + Arrays.toString(source) +
                ", target=" + Arrays.toString(target) +
                ", amount=" + amount +
                ", currency=" + currency +
                ", commentBytes=" + Arrays.toString(commentBytes) +
                ", method='" + method + '\'' +
                ", params=" + params +
                ", type=" + type +
                ", smartInfo=" + smartInfo +
                '}';
    }
}
