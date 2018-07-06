package com.credits.thrift;

public class ReturnValue {
    private byte[] contractState;
    private Variant variant;

    public ReturnValue() {
    }

    public ReturnValue(byte[] contractState, Variant variant) {
        this.contractState = contractState;
        this.variant = variant;
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
}
