package com.credits.thrift;

import com.credits.general.thrift.generated.Variant;

import java.util.Map;

public class ReturnValue {
    private byte[] contractState;
    private Variant variant;
    private Map<String, Variant> contractVariables;

    public ReturnValue(byte[] contractState, Variant variant, Map<String, Variant> contractVariables) {
        this.contractState = contractState;
        this.variant = variant;
        this.contractVariables = contractVariables;
    }

    public byte[] getContractState() {
        return contractState;
    }

    public void setContractState(byte[] contractState) {
        this.contractState = contractState;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public Map<String, Variant> getContractVariables() {
        return contractVariables;
    }

    public void setContractVariables(Map<String, Variant> contractVariables) {
        this.contractVariables = contractVariables;
    }
}
