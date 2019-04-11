package pojo.session;

import com.credits.general.pojo.ByteCodeObjectData;
import exception.ContractExecutorException;

import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class DeployContractSession extends Session {
    public final List<ByteCodeObjectData> byteCodeObjectDataList;
    public final String contractAddress;

    public DeployContractSession(
        long accessId,
        String initiatorAddress,
        String contractAddress,
        List<ByteCodeObjectData> byteCodeObjectDataList,
        long executionTime) {

        super(accessId, initiatorAddress, executionTime, new HashMap<>());
        validateArguments(initiatorAddress, contractAddress, byteCodeObjectDataList);
        this.byteCodeObjectDataList = byteCodeObjectDataList;
        this.contractAddress = contractAddress;
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
