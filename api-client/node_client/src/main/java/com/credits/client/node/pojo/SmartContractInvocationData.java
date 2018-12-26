package com.credits.client.node.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rustem Saidaliyev on 06.08.2018.
 */
public class SmartContractInvocationData implements Serializable {
    private static final long serialVersionUID = 4544650022718657168L;
    private SmartContractDeployData smartContractDeployData;
    private String method;
    private List<Object> params;
    private boolean forgetNewState;

    public SmartContractInvocationData(SmartContractDeployData smartContractDeployData, String method, List<Object> params, boolean forgetNewState) {
        this.smartContractDeployData = smartContractDeployData;
        this.method = method;
        this.params = params;
        this.forgetNewState = forgetNewState;
    }

    public SmartContractDeployData getSmartContractDeployData() {
        return smartContractDeployData;
    }

    public void setSmartContractDeployData(SmartContractDeployData smartContractDeployData) {
        this.smartContractDeployData = smartContractDeployData;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public boolean isForgetNewState() {
        return forgetNewState;
    }

    public void setForgetNewState(boolean forgetNewState) {
        this.forgetNewState = forgetNewState;
    }
}
