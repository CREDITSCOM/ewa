package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.general.pojo.ByteCodeObjectData;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class Session {
    final long accessId;
    final String initiatorAddress;
    final String contractAddress;
    final List<ByteCodeObjectData> byteCodeObjectDataList;
    final long executionTime;

    public Session(long accessId, String initiatorAddress, String contractAddress,
        List<ByteCodeObjectData> byteCodeObjectDataList, long executionTime) {
        validateArguments(initiatorAddress, contractAddress, byteCodeObjectDataList);
        this.accessId = accessId;
        this.initiatorAddress = initiatorAddress;
        this.contractAddress = contractAddress;
        this.byteCodeObjectDataList = byteCodeObjectDataList;
        this.executionTime = executionTime;
    }

    private void validateArguments(String initiatorAddress, String contractAddress, List<ByteCodeObjectData> byteCodeObjectDataList) {
        requireNonNull(initiatorAddress, "initiator address is null");
        requireNonNull(contractAddress, "contract address is null");
        requireNonNull(byteCodeObjectDataList, "byte code objects is null");
        if (byteCodeObjectDataList.isEmpty()) {
            throw new ContractExecutorException("bytecode objects is empty");
        }
    }
}
