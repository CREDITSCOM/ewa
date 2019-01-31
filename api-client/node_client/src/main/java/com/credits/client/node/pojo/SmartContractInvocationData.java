package com.credits.client.node.pojo;

import com.credits.general.pojo.ExecuteByteCodeResultData;
import com.credits.general.pojo.VariantData;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rustem Saidaliyev on 06.08.2018.
 */
public class SmartContractInvocationData implements Serializable {
    private static final long serialVersionUID = 4544650022718657168L;
    private SmartContractDeployData smartContractDeployData;
    private String method;
    private List<VariantData> params;
    private boolean forgetNewState;
    private ExecuteByteCodeResultData executeResult;

    public SmartContractInvocationData(SmartContractDeployData smartContractDeployData, String method, List<VariantData> params, boolean forgetNewState, ExecuteByteCodeResultData executeResult) {
        this.smartContractDeployData = smartContractDeployData;
        this.method = method;
        this.params = params;
        this.forgetNewState = forgetNewState;
        this.executeResult = executeResult;
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

    public List<VariantData> getParams() {
        return params;
    }

    public void setParams(List<VariantData> params) {
        this.params = params;
    }

    public boolean isForgetNewState() {
        return forgetNewState;
    }

    public void setForgetNewState(boolean forgetNewState) {
        this.forgetNewState = forgetNewState;
    }

    public ExecuteByteCodeResultData getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(ExecuteByteCodeResultData executeResult) {
        this.executeResult = executeResult;
    }
}
