package com.credits.client.node.pojo;

public enum TransactionTypeData {
    TT_Normal(0),
    TT_SmartDeploy(1),
    TT_SmartExecute(2),
    TT_SmartState(3);

    private final int value;

    private TransactionTypeData(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
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
