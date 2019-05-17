package pojo.session;

import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.thrift.generated.Variant;
import exception.ContractExecutorException;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class InvokeMethodSession extends DeployContractSession {
    public final byte[] contractState;
    public final String methodName;
    public final Variant[][] paramsTable;

    public InvokeMethodSession(
        long accessId, String initiatorAddress, String contractAddress,
        List<ByteCodeObjectData> byteCodeObjectDataList, byte[] contractState, String methodName,
        Variant[][] paramsTable, long executionTime) {

        super(accessId, initiatorAddress, contractAddress, byteCodeObjectDataList, executionTime);
        validateArguments(contractState, methodName);
        this.contractState = contractState;
        this.methodName = methodName;
        this.paramsTable = paramsTable == null ? new Variant[][] {{}} : paramsTable;
    }

    private void validateArguments(byte[] contractState, String methodName) {
        requireNonNull(contractState, "contract state is null");
        requireNonNull(methodName, "method name is null");
        if (contractState.length == 0) {
            throw new ContractExecutorException("contract state is empty");
        } else if (methodName.isEmpty()) {
            throw new ContractExecutorException("method name is empty");
        }
    }
}
