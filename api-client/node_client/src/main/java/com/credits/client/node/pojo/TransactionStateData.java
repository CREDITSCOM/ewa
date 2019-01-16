package com.credits.client.node.pojo;

public enum TransactionStateData {
    INVALID(0),
    VALID(1),
    INPROGRESS(2);

    private final int value;

    private TransactionStateData(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TransactionStateData findByValue(int value) {
        switch (value) {
            case 0:
                return INVALID;
            case 1:
                return VALID;
            case 2:
                return INPROGRESS;
            default:
                return null;
        }
    }

}
