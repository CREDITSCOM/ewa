package com.credits.wallet.desktop;

import com.credits.client.executor.service.ContractExecutorApiService;
import com.credits.client.node.pojo.TransactionRoundData;
import com.credits.client.node.service.NodeApiService;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.util.ObjectKeeper;
import com.credits.wallet.desktop.controller.Const;
import com.credits.wallet.desktop.service.ContractInteractionService;
import com.credits.wallet.desktop.struct.TransactionTabRow;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static com.credits.general.util.Constants.ds;

/**
 * Created by goncharov-eg on 19.01.2018.
 */
public class AppState {
    public static final String NODE_ERROR="A problem connecting to the Node";
    public static final int CREDITS_DECIMAL = 18;

    public static NodeApiService nodeApiService;
    public static ContractExecutorApiService contractExecutorService;
    public static ContractInteractionService contractInteractionService;
    public static String decimalSeparator = ds;
    public static String creditMonitorURL;
    public static Double screenWidth;
    public static Double screenHeight;

    public static boolean newAccount;
    public static boolean detailFromHistory;
    public static boolean noClearForm6;
    public static String account;

    public static ConcurrentHashMap<String, ConcurrentHashMap<Long, TransactionRoundData>> sourceMap = new ConcurrentHashMap<>();

    public static BigDecimal amount = BigDecimal.ZERO;
    public static BigDecimal transactionFeeValue = Const.FEE_TRAN_AMOUNT;
    public static BigDecimal transactionFeePercent = BigDecimal.ZERO;
    public static String toAddress;
    public static String coin;

    public static TransactionTabRow selectedTransactionRow;

    public static List<String> coins = new ArrayList<>();

    public static ExecutorService executor;

    public static PrivateKey privateKey;
    public static PublicKey publicKey;

    public static Map<String, Long> walletLastTransactionIdCache = new HashMap<>();

    public static Short transactionOfferedMaxFeeValue = Const.OFFERED_MAX_FEE;

    public static ObjectKeeper<ConcurrentHashMap<String, SmartContractData>> smartContractsKeeper;
    public static String text;
}
