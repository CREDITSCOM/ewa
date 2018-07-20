package com.credits.wallet.desktop.utils.struct;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by goncharov-eg on 20.07.2018.
 */
public class TransactionStruct implements Serializable {
    private String innerId;
    private String source;
    private String target;
    private BigDecimal amount;
    private BigDecimal balance;
    private String currency;

    public TransactionStruct(String innerId, String source, String target, BigDecimal amount, BigDecimal balance, String currency) {
        this.innerId = innerId;
        this.source = source;
        this.target = target;
        this.amount = amount;
        this.balance = balance;
        this.currency = currency;
    }
}
