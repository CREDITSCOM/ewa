package pojo;

import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;

import java.util.Objects;

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

        if (!Objects.equals(status, that.status)) {
            return false;
        }
        return Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        int result1 = status != null ? status.hashCode() : 0;
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        String sb = "SmartContractMethodResult{" + "status=" + status +
            ", result=" + result +
            '}';
        return sb;
    }
}
