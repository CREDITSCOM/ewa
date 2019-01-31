package com.credits.general.pojo;

import java.util.Arrays;

public class ExecuteByteCodeResultData extends ApiResponseData {

    private static final long serialVersionUID = 4773349687892366689L;

    private final byte[] contractState;
    private final VariantData retVal;

    public ExecuteByteCodeResultData(ApiResponseData apiResponseData, byte[] contractState, VariantData retVal) {
        super(apiResponseData);
        this.contractState = contractState;
        this.retVal = retVal;
    }

    public ExecuteByteCodeResultData(ApiResponseCode code, String message, byte[] contractState, VariantData retVal) {
        super(code, message);
        this.contractState = contractState;
        this.retVal = retVal;
    }

    public byte[] getContractState() {
        return contractState;
    }

    public VariantData getRetVal() {
        return retVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecuteByteCodeResultData)) return false;
        if (!super.equals(o)) return false;

        ExecuteByteCodeResultData that = (ExecuteByteCodeResultData) o;

        if (!Arrays.equals(contractState, that.contractState)) return false;
        return retVal.equals(that.retVal);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(contractState);
        result = 31 * result + retVal.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ExecuteByteCodeResultData{" +
                "contractState=" + Arrays.toString(contractState) +
                ", retVal=" + retVal +
                '}';
    }
}
