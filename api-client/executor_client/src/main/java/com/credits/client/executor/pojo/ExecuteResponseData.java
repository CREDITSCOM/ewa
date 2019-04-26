package com.credits.client.executor.pojo;

import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.generated.Variant;

import java.util.Objects;

public class ExecuteResponseData extends ApiResponseData {
    private static final long serialVersionUID = 3034570061349778560L;
    private final Variant executeBytecodeResult;

    public ExecuteResponseData(ApiResponseData response, Variant returnValue) {
        super(response.getCode(), response.getMessage());
        executeBytecodeResult = returnValue;
    }

    public Variant getExecuteBytecodeResult() {
        return executeBytecodeResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExecuteResponseData)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ExecuteResponseData that = (ExecuteResponseData) o;

        return Objects.equals(executeBytecodeResult, that.executeBytecodeResult);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (executeBytecodeResult != null ? executeBytecodeResult.hashCode() : 0);
        return result;
    }
}
