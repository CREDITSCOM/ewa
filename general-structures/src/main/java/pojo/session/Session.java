package pojo.session;

import pojo.ExternalSmartContract;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Session {
    public final long accessId;
    public final String initiatorAddress;
    public final long executionTime;
    public final Map<String, ExternalSmartContract> usedContracts;

    public Session(
        long accessId,
        String initiatorAddress,
        long executionTime,
        Map<String, ExternalSmartContract> usedContracts) {
        validateArguments(initiatorAddress);
        this.executionTime = executionTime;
        this.accessId = accessId;
        this.initiatorAddress = initiatorAddress;
        this.usedContracts = usedContracts == null ? new HashMap<>() : usedContracts;
    }

    private void validateArguments(String initiatorAddress) {
        requireNonNull(initiatorAddress, "initiator address is null");
    }
}
