package com.credits.wallet.desktop;

import com.credits.client.executor.service.ContractExecutorApiService;
import com.credits.client.node.service.NodeApiService;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import static com.credits.general.util.Constants.ds;

/**
 * Created by goncharov-eg on 19.01.2018.
 */
public class AppState {
    public static final String NODE_ERROR="A problem connecting to the Node";
    public static final int CREDITS_DECIMAL = 18;
    public static final int DEFAULT_EXECUTION_TIME = 1000;
    public static final String CREDITS_TOKEN_NAME = "CS";
    public static final int DELAY_AFTER_FULL_SYNC = 5;
    public static final int DELAY_BEFORE_FULL_SYNC = 2;



    public static NodeApiService nodeApiService;
    public static ContractExecutorApiService contractExecutorService;
    public static String decimalSeparator = ds;
    public static Map<String,Session> sessionMap = new HashMap<>();


    public static PrivateKey privateKey;
    public static PublicKey publicKey;


}
