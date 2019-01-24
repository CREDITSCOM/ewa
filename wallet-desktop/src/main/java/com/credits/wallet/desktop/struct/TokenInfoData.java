package com.credits.wallet.desktop.struct;

import java.math.BigDecimal;

public class TokenInfoData {
    public final String address;
    public final String name;
    public final BigDecimal balance;

    public TokenInfoData(String smartContractAddress, String tokenName, BigDecimal balance) {
        this.address = smartContractAddress;
        this.name = tokenName;
        this.balance = balance;
    }
}