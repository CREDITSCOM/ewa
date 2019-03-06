package com.credits.client.node.pojo;

import com.credits.general.pojo.VariantData;

import java.io.Serializable;
import java.math.BigDecimal;

public class SmartStateTransInfoData extends SmartTransInfoData implements Serializable {
    private static final long serialVersionUID = 7544650022718657111L;


    public boolean success;
    public BigDecimal executionFee;
    public VariantData returnValue;
    public TransactionIdData startTransaction;

    public SmartStateTransInfoData(boolean success, BigDecimal executionFee, VariantData returnValue, TransactionIdData startTransaction) {
        this.success = success;
        this.executionFee = executionFee;
        this.returnValue = returnValue;
        this.startTransaction = startTransaction;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public BigDecimal getExecutionFee() {
        return executionFee;
    }

    public void setExecutionFee(BigDecimal executionFee) {
        this.executionFee = executionFee;
    }

    public VariantData getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(VariantData returnValue) {
        this.returnValue = returnValue;
    }

    public TransactionIdData getStartTransaction() {
        return startTransaction;
    }

    public void setStartTransaction(TransactionIdData startTransaction) {
        this.startTransaction = startTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmartStateTransInfoData)) return false;

        SmartStateTransInfoData that = (SmartStateTransInfoData) o;

        if (isSuccess() != that.isSuccess()) return false;
        if (getExecutionFee() != null ? !getExecutionFee().equals(that.getExecutionFee()) : that.getExecutionFee() != null)
            return false;
        if (getReturnValue() != null ? !getReturnValue().equals(that.getReturnValue()) : that.getReturnValue() != null)
            return false;
        return getStartTransaction() != null ? getStartTransaction().equals(that.getStartTransaction()) : that.getStartTransaction() == null;
    }

    @Override
    public int hashCode() {
        int result = (isSuccess() ? 1 : 0);
        result = 31 * result + (getExecutionFee() != null ? getExecutionFee().hashCode() : 0);
        result = 31 * result + (getReturnValue() != null ? getReturnValue().hashCode() : 0);
        result = 31 * result + (getStartTransaction() != null ? getStartTransaction().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SmartStateTransInfoData{" +
                "success=" + success +
                ", executionFee=" + executionFee +
                ", returnValue=" + returnValue +
                ", startTransaction=" + startTransaction +
                '}';
    }
}
