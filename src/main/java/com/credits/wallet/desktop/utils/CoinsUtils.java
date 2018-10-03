package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.CommonCurrency;
import com.credits.wallet.desktop.thread.GetBalanceUpdater;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
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
        coins.clear();
        for (CommonCurrency coin : CommonCurrency.values()) {
            coins.put(coin.getMnemonic(),coin.getName());
        }
        return readCoinsFromFile(coins);
    }

    private static Map<String,String> readCoinsFromFile(Map<String,String> coins) {
        try {
            if(Files.exists(Paths.get(AppState.account+"coins.csv"))) {
                Files.readAllLines(Paths.get("coins.csv")).forEach(line -> {
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
            Path coinsPath = Paths.get(AppState.account+"coins.csv");
            List<String> strings = Collections.singletonList(strToWrite);
            Files.write(coinsPath, strings, StandardCharsets.UTF_8, StandardOpenOption.APPEND,
                StandardOpenOption.CREATE);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void fillBalanceCombobox(ComboBox<String> comboBox, Label balanceLabel) {
        comboBox.getItems().clear();
        CoinsUtils.getCoins().forEach((coin, smart) -> {
            comboBox.getItems().add(coin);
        });
        comboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            displayBalance(comboBox, balanceLabel);
        });
    }

    public static void displayBalance(ComboBox<String> comboBox, Label label) {
        String coin = comboBox.getSelectionModel().getSelectedItem();
        label.setText(MSG_RETRIEVE_BALANCE);
        if(coin.equals("cs")) {
            new Thread(new GetBalanceUpdater(coin, label)).start();
        } else {
            if (CoinsUtils.getCoins().get(coin)!= null) {
                BigDecimal balance = SmartContractUtils.getSmartContractBalance(CoinsUtils.getCoins().get(coin));
                if(balance != null) {
                    label.setText(balance.toString());
                }
            }
        }
    }

}
