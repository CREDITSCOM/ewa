package com.credits.wallet.desktop.utils;

import com.credits.common.utils.ObjectKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class CoinsUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(CoinsUtils.class);
    private static final String MSG_RETRIEVE_BALANCE = "Retrieving balance...";
    private static ObjectKeeper<ConcurrentHashMap<String, String>> coinsKeeper = new ObjectKeeper<>("coins.ser");

    public static ConcurrentHashMap<String,String> getCoins() {
        if (coinsKeeper.deserialize() == null) return new ConcurrentHashMap<>(); else return coinsKeeper.deserialize();
    }


    public static void saveCoinsToFile(ConcurrentHashMap<String, String> map) {
        try {
            coinsKeeper.serialize(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
