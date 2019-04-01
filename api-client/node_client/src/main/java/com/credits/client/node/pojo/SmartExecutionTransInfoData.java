package com.credits.client.node.pojo;

import com.credits.general.thrift.generated.Variant;

import java.io.Serializable;
import java.util.List;

public class SmartExecutionTransInfoData extends SmartTransInfoData implements Serializable {
    private static final long serialVersionUID = 2722063850625055198L;
    public String method;
    public List<Variant> params;
    public SmartOperationStateData state;
    public TransactionIdData stateTransaction;

    public SmartExecutionTransInfoData(String method, List<Variant> params, SmartOperationStateData state, TransactionIdData stateTransaction) {
        this.method = method;
        this.params = params;
        this.state = state;
        this.stateTransaction = stateTransaction;
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
        if (!(o instanceof SmartExecutionTransInfoData)) return false;

        SmartExecutionTransInfoData that = (SmartExecutionTransInfoData) o;

        if (getMethod() != null ? !getMethod().equals(that.getMethod()) : that.getMethod() != null) return false;
        if (getParams() != null ? !getParams().equals(that.getParams()) : that.getParams() != null) return false;
        if (getState() != that.getState()) return false;
        return getStateTransaction() != null ? getStateTransaction().equals(that.getStateTransaction()) : that.getStateTransaction() == null;
    }

    @Override
    public int hashCode() {
        int result = getMethod() != null ? getMethod().hashCode() : 0;
        result = 31 * result + (getParams() != null ? getParams().hashCode() : 0);
        result = 31 * result + (getState() != null ? getState().hashCode() : 0);
        result = 31 * result + (getStateTransaction() != null ? getStateTransaction().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SmartExecutionTransInfoData{" +
                "method='" + method + '\'' +
                ", params=" + params +
                ", state=" + state +
                ", stateTransaction=" + stateTransaction +
                '}';
    }
}
