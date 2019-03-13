package com.credits.thrift;

import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.pojo.apiexec.SmartContractGetResultData;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnValue {
    private byte[] contractState;
    private List<Variant> variants;
    private Map<String, SmartContractGetResultData> externalContracts;
    private List<APIResponse> statuses;

    public ReturnValue(byte[] contractState, List<Variant> variant, Map<String, SmartContractGetResultData> externalContracts, List<APIResponse> statuses) {
        this.externalContracts = externalContracts;
        this.contractState = contractState;
        this.variants = variant;
        this.statuses = statuses;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

    public Map<String, SmartContractGetResultData> getExternalContracts() {
        return externalContracts;
    }

    public Map<ByteBuffer, ByteBuffer> getExternalContractsStateForNode() {
        if(externalContracts!=null) {
            Map<ByteBuffer, ByteBuffer> externalContractStates = new HashMap<>();
            externalContracts.forEach((key,value)->{
                ByteBuffer contractAddress = ByteBuffer.wrap(GeneralConverter.decodeFromBASE58(key));
                ByteBuffer contractState = ByteBuffer.wrap(value.getContractState());
                externalContractStates.put(contractAddress,contractState);
            });
            return externalContractStates;
        }
        return null;
    }

    public void setExternalContracts(Map<String, SmartContractGetResultData> externalContracts) {
        this.externalContracts = externalContracts;
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
