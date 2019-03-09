package com.credits.service.contract;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class SmartContractConstants {
    private static Map<Long, SmartContractConstants> sessions = Collections.synchronizedMap(new WeakHashMap<>());
    public final String initiator;
    public final String contractAddress;
    public final long accessId;

    private SmartContractConstants(String initiator, String contractAddress, long accessId) {
        this.initiator = initiator;
        this.contractAddress = contractAddress;
        this.accessId = accessId;
    }

    public static void initSessionSmartContractConstants(long threadId, String initiator, String contractAddress, long accessId) {
        sessions.put(threadId, new SmartContractConstants(initiator, contractAddress, accessId));
    }


    public static SmartContractConstants getSessionSmartContractConstants(long threadId) {
        return sessions.remove(threadId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmartContractConstants that = (SmartContractConstants) o;

        if (accessId != that.accessId) return false;
        if (initiator != null ? !initiator.equals(that.initiator) : that.initiator != null) return false;
        return contractAddress != null ? contractAddress.equals(that.contractAddress) : that.contractAddress == null;
    }

    @Override
    public int hashCode() {
        int result = initiator != null ? initiator.hashCode() : 0;
        result = 31 * result + (contractAddress != null ? contractAddress.hashCode() : 0);
        result = 31 * result + (int) (accessId ^ (accessId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "SmartContractConstants{" +
                "initiator='" + initiator + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", accessId=" + accessId +
                '}';
    }
}
