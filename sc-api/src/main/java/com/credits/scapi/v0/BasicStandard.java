package com.credits.scapi.v0;

public interface BasicStandard {
    String getName();

    String getSymbol();

    int getDecimal();

    boolean setFrozen(boolean frozen);

    String totalSupply();

    String balanceOf(String owner);

    String allowance(String owner, String spender);

    boolean transfer(String to, String amount);

    boolean transferFrom(String from, String to, String amount);

    void approve(String spender, String amount);

    boolean burn(String amount);
}
