package com.credits.thrift;

import com.credits.thrift.generated.Variant;

import java.util.Map;

public class DeployReturnValue {

    private byte[] contractState;
    private Map<String, Variant> contractVariables;

    public DeployReturnValue(byte[] contractState, Map<String, Variant> contractVariables) {
        this.contractState = contractState;
        this.contractVariables = contractVariables;
    }

    public byte[] getContractState() {
        return contractState;
    }

    public void setContractState(byte[] contractState) {
        this.contractState = contractState;
    }

    public Map<String, Variant> getContractVariables() {
        return contractVariables;
    }

    public void setContractVariables(Map<String, Variant> contractVariables) {
        this.contractVariables = contractVariables;
    }
}
