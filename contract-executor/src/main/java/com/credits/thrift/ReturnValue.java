package com.credits.thrift;

import com.credits.pojo.apiexec.SmartContractGetResultData;
import com.credits.service.contract.SmartContractMethodResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ReturnValue {
    public byte[] newContractState;
    public final List<SmartContractMethodResult> executeResults;
    public final Map<String, SmartContractGetResultData> externalSmartContracts;

    public ReturnValue(
        byte[] newContractState,
        List<SmartContractMethodResult> executeResults,
        Map<String, SmartContractGetResultData> externalSmartContracts) {
        this.newContractState = newContractState;
        this.externalSmartContracts = externalSmartContracts;
        this.executeResults = executeResults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReturnValue)) {
            return false;
        }

        ReturnValue that = (ReturnValue) o;

        if (!Arrays.equals(newContractState, that.newContractState)) {
            return false;
        }
        if (executeResults != null ? !executeResults.equals(that.executeResults) : that.executeResults != null) {
            return false;
        }
        return externalSmartContracts != null ? externalSmartContracts.equals(that.externalSmartContracts) : that.externalSmartContracts == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(newContractState);
        result = 31 * result + (executeResults != null ? executeResults.hashCode() : 0);
        result = 31 * result + (externalSmartContracts != null ? externalSmartContracts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReturnValue{");
        sb.append("newContractState=").append(Arrays.toString(newContractState));
        sb.append(", executeResults=").append(executeResults);
        sb.append(", externalSmartContracts=").append(externalSmartContracts);
        sb.append('}');
        return sb.toString();
    }
}
