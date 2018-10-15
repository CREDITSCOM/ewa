package com.credits.wallet.desktop.struct;

import java.io.Serializable;
import java.math.BigDecimal;

public class CoinTabRow implements Serializable {

    private static final long serialVersionUID = 4222650022718657167L;

    private String name;
    private BigDecimal balance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
