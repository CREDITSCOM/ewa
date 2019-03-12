package com.credits.thrift;

import com.credits.service.contract.SmartContractMethodResult;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class ReturnValue {
    public byte[] newContractState;
    public final List<SmartContractMethodResult> executeResults;
    public final Map<String,ByteBuffer> externalContractStates;

    public ReturnValue(byte[] newContractState, List<SmartContractMethodResult> executeResults, Map<String,ByteBuffer> externalContractStates) {
        this.newContractState = newContractState;
        this.externalContractStates = externalContractStates;
        this.executeResults = executeResults;
    }
}
