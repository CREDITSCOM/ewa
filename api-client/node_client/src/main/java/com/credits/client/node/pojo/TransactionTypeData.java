package com.credits.client.node.pojo;

public enum TransactionTypeData {
    TT_Normal(0, "TT_Normal"),
    TT_SmartDeploy(1, "TT_SmartDeploy"),
    TT_SmartExecute(2, "TT_SmartExecute"),
    TT_SmartState(3, "TT_SmartState");

    private final int value;
    private final String name;

    private TransactionTypeData(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static TransactionTypeData findByValue(int value) {
        switch (value) {
            case 0:
                return TT_Normal;
            case 1:
                return TT_SmartDeploy;
            case 2:
                return TT_SmartExecute;
            case 3:
                return TT_SmartState;
            default:
                return null;
        }
    }
}
