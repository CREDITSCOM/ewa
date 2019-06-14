package com.credits.client.node.pojo;

public enum SmartOperationStateData {
    SOS_Pending(0),
    SOS_Success(1),
    SOS_Failed(2);

    private final int value;

    private SmartOperationStateData(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


    public static SmartOperationStateData findByValue(int value) {
        switch (value) {
            case 0:
                return SOS_Pending;
            case 1:
                return SOS_Success;
            case 2:
                return SOS_Failed;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        switch (value) {
            case 0:
                return "Pending";
            case 1:
                return "Success";
            case 2:
                return "Failed";
            default:
                return "Unknown state value";
        }
    }
}
