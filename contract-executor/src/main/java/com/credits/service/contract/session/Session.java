package com.credits.service.contract.session;

import static java.util.Objects.requireNonNull;

public class Session {
    public final long accessId;
    public final String initiatorAddress;
    public final long executionTime;

    public Session(long accessId, String initiatorAddress, long executionTime) {
        validateArguments(initiatorAddress);
        this.executionTime = executionTime;
        this.accessId = accessId;
        this.initiatorAddress = initiatorAddress;
    }

    private void validateArguments(String initiatorAddress) {
        requireNonNull(initiatorAddress, "initiator address is null");
    }
}
