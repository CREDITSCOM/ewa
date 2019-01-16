package com.credits.client.node.pojo;


public enum TokenStandartData {
    NotAToken(0),
    CreditsBasic(1),
    CreditsExtended(2);

    private final int value;

    private TokenStandartData(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TokenStandartData findByValue(int value) {
        switch (value) {
            case 0:
                return NotAToken;
            case 1:
                return CreditsBasic;
            case 2:
                return CreditsExtended;
            default:
                return null;
        }
    }
}
