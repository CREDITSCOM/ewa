package com.credits.general.client.node.pojo;

import java.math.BigDecimal;

public class WalletData {
    private Integer walletId;
    private BigDecimal balance;
    private Long lastTransactionId;

    public WalletData(Integer walletId, BigDecimal balance, Long lastTransactionId) {
        this.walletId = walletId;
        this.balance = balance;
        this.lastTransactionId = lastTransactionId;
    }

    public Integer getWalletId() {
        return walletId;
    }

    public void setWalletId(Integer walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getLastTransactionId() {
        return lastTransactionId;
    }

    public void setLastTransactionId(Long lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }
}
