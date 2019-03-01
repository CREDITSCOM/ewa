package com.credits.service.contract;

public class SmartContractConstants {
    public final String initiator;
    public final String contractAddress;
    public final long accessId;

    public SmartContractConstants(String initiator, String contractAddress, long accessId) {
        System.out.println(Thread.currentThread() + " const init accessId " + accessId);
        this.initiator = initiator;
        this.contractAddress = contractAddress;
        this.accessId = accessId;
    }
}
