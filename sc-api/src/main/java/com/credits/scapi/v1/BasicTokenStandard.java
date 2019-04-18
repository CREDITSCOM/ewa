package com.credits.scapi.v1;

import java.math.BigDecimal;

/**
 * Created by Igor Goryunov on 26.09.2018
 */
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

    void payable(BigDecimal amount);
}
