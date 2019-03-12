package com.credits.service.contract;

import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;

public class SmartContractMethodResult {
    public final APIResponse status;
    public final Variant result;

    public SmartContractMethodResult(APIResponse status, Variant result) {
        this.status = status;
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SmartContractMethodResult)) {
            return false;
        }

        SmartContractMethodResult that = (SmartContractMethodResult) o;

        if (status != null ? !status.equals(that.status) : that.status != null) {
            return false;
        }
        return result != null ? result.equals(that.result) : that.result == null;
    }

    @Override
    public int hashCode() {
        int result1 = status != null ? status.hashCode() : 0;
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SmartContractMethodResult{");
        sb.append("status=").append(status);
        sb.append(", result=").append(result);
        sb.append('}');
        return sb.toString();
    }
}
