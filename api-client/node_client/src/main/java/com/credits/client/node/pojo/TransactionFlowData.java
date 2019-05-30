package com.credits.client.node.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;


public class TransactionFlowData extends TransactionData implements Serializable {
    private static final long serialVersionUID = 4544650022718657166L;
    protected Short offeredMaxFee16Bits;
    protected byte[] smartContractBytes;
    protected byte[] signature;

    public TransactionFlowData() {
    }

    public TransactionFlowData(long innerId, byte[] source, byte[] target, BigDecimal amount, Short offeredMaxFee16Bits,
        byte[] smartContractBytes, byte[] commentBytes) {
        super();
        this.setId(innerId);
        this.setSource(source);
        this.setTarget(target);
        this.setAmount(amount);
        this.setCurrency(currency);
        this.setOfferedMaxFee16Bits(offeredMaxFee16Bits);
        this.setSmartContractBytes(smartContractBytes);
        this.setCommentBytes(commentBytes);
    }

    public TransactionFlowData(long innerId, byte[] source, byte[] target, BigDecimal amount, Short offeredMaxFee16Bits,
        byte currency, byte[] smartContractBytes, byte[] commentBytes, byte[] signature) {
        super();
        this.setId(innerId);
        this.setSource(source);
        this.setTarget(target);
        this.setAmount(amount);
        this.setCurrency(currency);
        this.setOfferedMaxFee16Bits(offeredMaxFee16Bits);
        this.setSmartContractBytes(smartContractBytes);
        this.setCommentBytes(commentBytes);
        this.setSignature(signature);
    }


    public TransactionFlowData(TransactionFlowData transaction) {
        this(transaction.id, transaction.source, transaction.target, transaction.amount, transaction.offeredMaxFee16Bits,
            transaction.currency, transaction.smartContractBytes, transaction.commentBytes, transaction.signature);
    }

    public Short getOfferedMaxFee16Bits() {
        return offeredMaxFee16Bits;
    }

    public void setOfferedMaxFee16Bits(Short offeredMaxFee16Bits) {
        this.offeredMaxFee16Bits = offeredMaxFee16Bits;
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
        return Objects.equals(offeredMaxFee16Bits, that.offeredMaxFee16Bits) && Arrays.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(super.hashCode(), offeredMaxFee16Bits);
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
        sb.append(", currency=").append(currency);
        sb.append("offeredMaxFee16Bits=").append(offeredMaxFee16Bits);
        sb.append(", signature=").append(Arrays.toString(signature));
        sb.append('}');
        return sb.toString();
    }
}