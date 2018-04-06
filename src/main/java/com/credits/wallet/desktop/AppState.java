package com.credits.wallet.desktop;

import com.credits.leveldb.client.*;
import com.credits.wallet.desktop.controller.Const;
import com.credits.wallet.desktop.struct.TransactionTabRow;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by goncharov-eg on 19.01.2018.
 */
public class AppState {
    public static ApiClient apiClient;
    public static String decSep;
    public static String contractExecutorHost;
    public static Integer contractExecutorPort;
    public static String creditMonitorURL;
    public static String csSenderBotHost;
    public static Integer csSenderBotPort;


    public static boolean newAccount;
    public static boolean detailFromHistory;
    public static boolean noClearForm6;
    public static String account;

    public static Double amount = 0.0;
    public static Double transactionFeeValue = Const.FEE_TRAN_AMOUNT;
    public static Double transactionFeePercent = 0.0;
    public static String toAddress;
    public static String coin;
    public static String hash;
    public static String innerId;

    public static TransactionTabRow selectedTransactionRow;

    public static List<String> coins=new ArrayList<>();

    public static ExecutorService executor;

    public static PrivateKey privateKey;
    public static PublicKey publicKey;
}
