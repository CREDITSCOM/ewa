package com.credits.wallet.desktop;

import com.credits.client.executor.service.ContractExecutorApiService;
import com.credits.client.node.service.NodeApiService;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.general.util.ObjectKeeper;
import com.credits.wallet.desktop.service.ContractInteractionService;
import com.credits.wallet.desktop.struct.TransactionTabRow;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static com.credits.general.util.Constants.ds;

/**
 * Created by goncharov-eg on 19.01.2018.
 */
public class AppState {
    public static final String NODE_ERROR="A problem connecting to the Node";
    public static final int CREDITS_DECIMAL = 18;
    public static final BigDecimal FEE_TRAN_AMOUNT = new BigDecimal("0.1");
    public static final Short OFFERED_MAX_FEE = 0x6648;


    public static ConcurrentHashMap<String, ConcurrentHashMap<Long, TransactionRoundData>> sourceMap = new ConcurrentHashMap<>();

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

    public static BigDecimal amount = BigDecimal.ZERO;
    public static BigDecimal transactionFeeValue = FEE_TRAN_AMOUNT;
    public static BigDecimal transactionFeePercent = BigDecimal.ZERO;
    public static String toAddress;
    public static String coin;

    public static TransactionTabRow selectedTransactionRow;

    public static ExecutorService executor;

    public static PrivateKey privateKey;
    public static PublicKey publicKey;

    public static Short transactionOfferedMaxFeeValue = OFFERED_MAX_FEE;

    public static ObjectKeeper<ConcurrentHashMap<String, String>> coinsKeeper;
    public static ObjectKeeper<HashMap<String, SmartContractData>> favoriteContractsKeeper;
    public static String transactionText;
    public static String lastSmartContract;
}
