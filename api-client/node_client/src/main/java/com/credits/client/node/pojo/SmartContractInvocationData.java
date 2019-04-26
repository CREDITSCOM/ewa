package com.credits.client.node.pojo;

import com.credits.general.thrift.generated.Variant;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by Rustem Saidaliyev on 06.08.2018.
 */
public class SmartContractInvocationData implements Serializable {
    private static final long serialVersionUID = 4544650022718657168L;
    private SmartContractDeployData smartContractDeployData;
    private String method;
    private List<Variant> params;
    private List<ByteBuffer> usedContracts;
    private boolean forgetNewState;

    public SmartContractInvocationData(SmartContractDeployData smartContractDeployData, String method, List<Variant> params, List<ByteBuffer> usedContracts, boolean forgetNewState) {
        this.smartContractDeployData = smartContractDeployData;
        this.method = method;
        this.params = params;
        this.forgetNewState = forgetNewState;
        this.usedContracts = usedContracts;
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

    public List<Variant> getParams() {
        return params;
    }

    public void setParams(List<Variant> params) {
        this.params = params;
    }

    public boolean isForgetNewState() {
        return forgetNewState;
    }

    public void setForgetNewState(boolean forgetNewState) {
        this.forgetNewState = forgetNewState;
    }

    public List<ByteBuffer> getUsedContracts() {
        return usedContracts;
    }

    public void setUsedContracts(List<ByteBuffer> usedContracts) {
        this.usedContracts = usedContracts;
    }
}
