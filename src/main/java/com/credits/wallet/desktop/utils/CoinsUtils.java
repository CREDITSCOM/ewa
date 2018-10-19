package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoinsUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(CoinsUtils.class);
    private static final String MSG_RETRIEVE_BALANCE = "Retrieving balance...";

    public static Map<String,String> getCoins() {
        Map<String,String> coins = new HashMap<>();
        return readCoinsFromFile(coins);
    }

    private static Map<String,String> readCoinsFromFile(Map<String,String> coins) {
        try {
            String fileName = AppState.account + "coins.csv";
            if(Files.exists(Paths.get(fileName))) {
                Files.readAllLines(Paths.get(fileName)).forEach(line -> {
                    if (line != null && !line.trim().isEmpty()) {
                        String[] s = line.split(";");
                        coins.put(s[0], s[1]);
                    }
                });
            }
        } catch (IOException e) {
            FormUtils.showInfo("Сoins type are not loaded");
        }
        return coins;
    }

    public static void saveCoinsToFile(String strToWrite) {
        try {
            String fileName = AppState.account + "coins.csv";
            Path coinsPath = Paths.get(fileName);
            List<String> strings = Collections.singletonList(strToWrite);
            Files.write(coinsPath, strings, StandardCharsets.UTF_8, StandardOpenOption.APPEND,
                StandardOpenOption.CREATE);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
