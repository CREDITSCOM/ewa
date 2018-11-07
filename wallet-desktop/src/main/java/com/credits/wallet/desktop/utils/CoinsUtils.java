package com.credits.wallet.desktop.utils;

import com.credits.general.util.ObjectKeeper;
import com.credits.wallet.desktop.AppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class CoinsUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(CoinsUtils.class);
    private static final String MSG_RETRIEVE_BALANCE = "Retrieving balance...";
    private static ObjectKeeper<ConcurrentHashMap<String, String>> coinsKeeper = new ObjectKeeper<>(AppState.account, "coins");

    public static ConcurrentHashMap<String,String> getCoins() {
        if (coinsKeeper.getKeptObject() == null)
            return new ConcurrentHashMap<>();
        else
            return coinsKeeper.getKeptObject();
    }


    public static void saveCoinsToFile(ConcurrentHashMap<String, String> map) {
        try {
            coinsKeeper.keepObject(map);
        } catch (Exception e) {
            LOGGER.error("failed!", e );
        }
    }

}
