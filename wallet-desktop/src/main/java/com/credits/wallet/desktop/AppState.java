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
    public static final String CREDITS_TOKEN_NAME = "CS";
    public static final int DELAY_AFTER_FULL_SYNC = 5;
    public static final int DELAY_BEFORE_FULL_SYNC = 2;

    public static final String decimalSeparator = DECIMAL_SEPARATOR;

    private static final Map<String,Session> sessionMap = new HashMap<>();
    private static NodeApiService nodeApiService;
    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private static String pwd;
    private static Stage primaryStage;

    public static void setNodeApiService(NodeApiService nodeApiService) {
        AppState.nodeApiService = nodeApiService;
    }
    public static NodeApiService getNodeApiService() {
        return nodeApiService;
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void setPrivateKey(PrivateKey privateKey) {
        AppState.privateKey = privateKey;
    }

    public static PublicKey getPublicKey() {
        return publicKey;
    }

    public static void setPublicKey(PublicKey publicKey) {
        AppState.publicKey = publicKey;
    }

    public static String getPwd() {
        return pwd;
    }

    public static void setPwd(String pwd) {
        AppState.pwd = pwd;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setPrimaryStage(Stage primaryStage) {
        AppState.primaryStage = primaryStage;
    }

    public static Map<String, Session> getSessionMap() {
        return sessionMap;
    }
}
