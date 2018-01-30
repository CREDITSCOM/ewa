package com.credits.wallet.desktop;

import com.credits.wallet.desktop.struct.TransactionTabRow;

/**
 * Created by goncharov-eg on 19.01.2018.
 */
public class AppState {
    public static String apiAddr;

    public static boolean newAccount;
    public static String account;

    public static Double amount = 0.0;
    public static Double transactionFeeValue = 0.0;
    public static Double transactionFeePercent = 0.0;
    public static String toAddress;
    public static String transactionHash;

    public static TransactionTabRow selectedTransactionRow;
}
