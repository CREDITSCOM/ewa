package com.credits.wallet.desktop;

import com.credits.leveldb.client.ApiClient;
import com.credits.wallet.desktop.controller.Const;
import com.credits.wallet.desktop.struct.TransactionTabRow;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by goncharov-eg on 19.01.2018.
 */
public class AppState {
    public static final String NODE_ERROR="A problem connecting to the Node";

    public static ApiClient apiClient;
    public static String decimalSeparator;
    public static String creditMonitorURL;

    public static boolean newAccount;
    public static boolean detailFromHistory;
    public static boolean noClearForm6;
    public static String account;

    public static BigDecimal amount = BigDecimal.ZERO;
    public static BigDecimal transactionFeeValue = Const.FEE_TRAN_AMOUNT;
    public static BigDecimal transactionFeePercent = BigDecimal.ZERO;
    public static String toAddress;
    public static String coin;
    public static long innerId;

    public static TransactionTabRow selectedTransactionRow;

    public static List<String> coins = new ArrayList<>();

    public static ExecutorService executor;

    public static PrivateKey privateKey;
    public static PublicKey publicKey;

    public static BigDecimal balance = BigDecimal.ZERO;
}
