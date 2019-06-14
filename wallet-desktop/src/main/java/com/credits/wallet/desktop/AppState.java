package com.credits.wallet.desktop;

import com.credits.client.node.service.NodeApiService;
import javafx.stage.Stage;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import static com.credits.general.util.Constants.DECIMAL_SEPARATOR;


public class AppState {
    public static final String NODE_ERROR="A problem connecting to the Node";
    public static final int CREDITS_DECIMAL = 18;
    public static final long DEFAULT_EXECUTION_TIME = 1000;
    public static final String CREDITS_TOKEN_NAME = "CS";
    public static final int DELAY_AFTER_FULL_SYNC = 5;
    public static final int DELAY_BEFORE_FULL_SYNC = 2;

    public static NodeApiService nodeApiService;
    public static final String decimalSeparator = DECIMAL_SEPARATOR;
    public static final Map<String,Session> sessionMap = new HashMap<>();

    public static PrivateKey privateKey;
    public static PublicKey publicKey;
    public static String pwd;

    public static Stage primaryStage;
}
