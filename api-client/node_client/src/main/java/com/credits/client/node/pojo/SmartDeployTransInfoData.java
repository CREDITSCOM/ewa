package com.credits.client.node.pojo;

import java.io.Serializable;

public class SmartDeployTransInfoData extends SmartTransInfoData implements Serializable {

    private static final long serialVersionUID = -8735671478802406797L;
    private SmartOperationStateData state;
    private TransactionIdData stateTransaction;

    public SmartDeployTransInfoData(SmartOperationStateData state, TransactionIdData stateTransaction) {
        this.state = state;
        this.stateTransaction = stateTransaction;
    }

    public SmartOperationStateData getState() {
        return state;
    }

    public void setState(SmartOperationStateData state) {
        this.state = state;
    }

    public TransactionIdData getStateTransaction() {
        return stateTransaction;
    }

    public void setStateTransaction(TransactionIdData stateTransaction) {
        this.stateTransaction = stateTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmartDeployTransInfoData)) return false;

        SmartDeployTransInfoData that = (SmartDeployTransInfoData) o;

        if (getState() != that.getState()) return false;
        return getStateTransaction() != null ? getStateTransaction().equals(that.getStateTransaction()) : that.getStateTransaction() == null;
    }

    @Override
    public int hashCode() {
        int result = getState() != null ? getState().hashCode() : 0;
        result = 31 * result + (getStateTransaction() != null ? getStateTransaction().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SmartDeployTransInfoData{" +
                "state=" + state +
                ", stateTransaction=" + stateTransaction +
                '}';
    }
}
