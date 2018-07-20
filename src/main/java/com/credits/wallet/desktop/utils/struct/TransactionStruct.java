package com.credits.wallet.desktop.utils.struct;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by goncharov-eg on 20.07.2018.
 */
public class TransactionStruct implements Serializable {
    private long innerId;
    private String source;
    private String target;
    private BigDecimal amount;
    private BigDecimal balance;
    private byte currency;

    public TransactionStruct(long innerId, String source, String target, BigDecimal amount, BigDecimal balance, byte currency) {
        this.innerId = innerId;
        this.source = source;
        this.target = target;
        this.amount = amount;
        this.balance = balance;
        this.currency = currency;
    }
}
