package com.credits.client.node.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class TransactionFlowData extends TransactionData implements Serializable {
    protected static final long serialVersionUID = 4544650022718657166L;
    protected Short offeredMaxFee;
    protected byte[] smartContractBytes;
    protected byte[] signature;

    public TransactionFlowData() {
    }


    public TransactionFlowData(long innerId, byte[] source, byte[] target, BigDecimal amount, Short offeredMaxFee,
        byte currency, byte[] smartContractBytes) {
        super();
        this.setId(innerId);
        this.setSource(source);
        this.setTarget(target);
        this.setAmount(amount);
        this.setBalance(balance);
        this.setCurrency(currency);
        this.setOfferedMaxFee(offeredMaxFee);
        this.setSmartContractBytes(smartContractBytes);
    }

    public TransactionFlowData(long innerId, byte[] source, byte[] target, BigDecimal amount, Short offeredMaxFee,
        byte currency, byte[] smartContractBytes, byte[] signature) {
        super();
        this.setId(innerId);
        this.setSource(source);
        this.setTarget(target);
        this.setAmount(amount);
        this.setBalance(balance);
        this.setCurrency(currency);
        this.setOfferedMaxFee(offeredMaxFee);
        this.setSmartContractBytes(smartContractBytes);
        this.setSignature(signature);
    }


    public TransactionFlowData(TransactionFlowData transaction) {
        this(transaction.id, transaction.source, transaction.target, transaction.amount, transaction.offeredMaxFee,
            transaction.currency, transaction.smartContractBytes, transaction.signature);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionFlowData)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TransactionFlowData that = (TransactionFlowData) o;
        return Objects.equals(offeredMaxFee, that.offeredMaxFee) && Arrays.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(super.hashCode(), offeredMaxFee);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }

    public byte[] getSmartContractBytes() {
        return smartContractBytes;
    }

    public void setSmartContractBytes(byte[] smartContractBytes) {
        this.smartContractBytes = smartContractBytes;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionFlowData{");
        sb.append(", id=").append(id);
        sb.append(", source=").append(Arrays.toString(source));
        sb.append(", target=").append(Arrays.toString(target));
        sb.append(", amount=").append(amount);
        sb.append(", balance=").append(balance);
        sb.append(", currency=").append(currency);
        sb.append("offeredMaxFee=").append(offeredMaxFee);
        sb.append(", signature=").append(Arrays.toString(signature));
        sb.append('}');
        return sb.toString();
    }
}