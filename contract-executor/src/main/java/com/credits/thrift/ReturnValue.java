package com.credits.thrift;

import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class ReturnValue {
    private byte[] contractState;
    private List<Variant> variants;
    private Map<ByteBuffer,ByteBuffer> externalContractsState ;
    private List<APIResponse> statuses;

    public ReturnValue(byte[] contractState, List<Variant> variant, Map<ByteBuffer,ByteBuffer> externalContractsState, List<APIResponse> statuses) {
        this.externalContractsState = externalContractsState;
        this.contractState = contractState;
        this.variants = variant;
        this.statuses = statuses;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

    public Map<ByteBuffer,ByteBuffer> getExternalContractsState() {
        return externalContractsState;
    }

    public void setExternalContractsState(Map<ByteBuffer,ByteBuffer> externalContractsState) {
        this.externalContractsState = externalContractsState;
    }

    public List<APIResponse> getStatuses() {
        return statuses;
    }

    public byte[] getContractState() {
        return contractState;
    }

    public void setContractState(byte[] contractState) {
        this.contractState = contractState;
    }

    public List<Variant> getVariantsList() {
        return variants;
    }

    public void setVariant(List<Variant> variant) {
        this.variants = variant;
    }

    public List<APIResponse> getStatusesList() {
        return statuses;
    }

    public void setStatuses(List<APIResponse> statuses) {
        this.statuses = statuses;
    }
}
