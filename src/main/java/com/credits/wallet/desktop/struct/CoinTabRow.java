package com.credits.wallet.desktop.struct;

import java.io.Serializable;

public class CoinTabRow implements Serializable {

    private static final long serialVersionUID = 4222650022718657167L;

    private String name;
    private String balance;
    private String smartName;

    public String getSmartName() {
        return smartName;
    }

    public void setSmartName(String smartName) {
        this.smartName = smartName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
