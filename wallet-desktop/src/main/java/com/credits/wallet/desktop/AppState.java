package com.credits.wallet.desktop;

import com.credits.client.executor.service.ContractExecutorApiService;
import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.util.ObjectKeeper;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.wallet.desktop.service.ContractInteractionService;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.general.util.Constants.ds;

/**
 * Created by goncharov-eg on 19.01.2018.
 */
public class AppState {
    public static final String NODE_ERROR="A problem connecting to the Node";
    public static final int CREDITS_DECIMAL = 18;
    public static final BigDecimal FEE_TRAN_AMOUNT = new BigDecimal("0.1");
    public static final Short OFFERED_MAX_FEE = 0x6648;
    public static final int DEFAULT_EXECUTION_TIME = 1000;
    public static final String CREDITS_TOKEN_NAME = "CS";


    public static ConcurrentHashMap<String, ConcurrentHashMap<Long, TransactionRoundData>> sourceMap = new ConcurrentHashMap<>();

    public static NodeApiService nodeApiService;
    public static ContractExecutorApiService contractExecutorService;
    public static ContractInteractionService contractInteractionService;
    public static String decimalSeparator = ds;

    //todo move to session
    public static ObjectKeeper<ConcurrentHashMap<String, String>> coinsKeeper;
    public static ObjectKeeper<HashMap<String, SmartContractData>> favoriteContractsKeeper;
    public static String lastSmartContract;
    public static String account;
    //todo move to session

    public static BigDecimal transactionFeeValue = FEE_TRAN_AMOUNT;

    public static PrivateKey privateKey;
    public static PublicKey publicKey;

    public static Short transactionOfferedMaxFeeValue = OFFERED_MAX_FEE;

}
