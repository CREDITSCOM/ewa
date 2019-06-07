package com.credits.scapi.v1;

import java.math.BigDecimal;

public interface BasicTokenStandard {

    String getName();

    String getSymbol();

    int getDecimal();

    boolean setFrozen(boolean frozen);

    BigDecimal totalSupply();

    BigDecimal balanceOf(String owner);

    BigDecimal allowance(String owner, String spender);

    boolean transfer(String to, BigDecimal amount);

    boolean transferFrom(String from, String to, BigDecimal amount);

    void approve(String spender, BigDecimal amount);

    boolean burn(BigDecimal amount);

    String payable(BigDecimal amount, byte[] userData);
}
