package com.credits.client.node.pojo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class SmartContractTransactionFlowData extends TransactionFlowData implements Serializable {
    protected static final long serialVersionUID = 4544650022718657169L;

    protected SmartContractInvocationData smartContractData;

    public SmartContractTransactionFlowData(TransactionFlowData transaction, SmartContractInvocationData scData){
        super(transaction);
        smartContractData = scData;
    }

    public SmartContractInvocationData getSmartContractData() {
        return smartContractData;
    }

    public void setSmartContractData(SmartContractInvocationData smartContractData) {
        this.smartContractData = smartContractData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SmartContractTransactionFlowData)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SmartContractTransactionFlowData that = (SmartContractTransactionFlowData) o;
        return Objects.equals(smartContractData, that.smartContractData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), smartContractData);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SmartContractTransactionFlowData{");
        sb.append(", id=").append(id);
        sb.append(", source=").append(Arrays.toString(source));
        sb.append(", target=").append(Arrays.toString(target));
        sb.append(", amount=").append(amount);
        sb.append(", currency=").append(currency);
        sb.append(", offeredMaxFee=").append(offeredMaxFee);
        sb.append(", signature=").append(Arrays.toString(signature));
        sb.append("smartContractData=").append(smartContractData);
        sb.append('}');
        return sb.toString();
    }
}
