package com.credits.wallet.desktop;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public enum CommonCurrency {
    CREDITS("Credits", "cs");

    private String name;
    private String mnemonic;

    CommonCurrency(String name, String mnemonic) {
        this.name = name;
        this.mnemonic = mnemonic;
    }

    public String getName() {
        return name;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public String toString() {
        return name + " (" + mnemonic + ")";
    }
}
