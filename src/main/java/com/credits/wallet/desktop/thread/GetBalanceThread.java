package com.credits.wallet.desktop.thread;

import com.credits.common.utils.Converter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.Utils;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Created by goncharov-eg on 10.04.2018.
 */
public class GetBalanceThread implements Runnable {
    private static final String ERR_GETTING_BALANCE = "Error getting balance";

    private static Logger LOGGER = LoggerFactory.getLogger(GetBalanceThread.class);

    private String coin;
    private Label label;

    public GetBalanceThread(String coin, Label label) {
        this.coin = coin;
        this.label = label;
    }

    @Override
    public void run() {
        try {
            BigDecimal balance = AppState.apiClient.getBalance(AppState.account, coin);
            Platform.runLater(new Runnable() {
                public void run() {
                    AppState.balance = balance;
                    label.setText(Converter.toString(balance)
                    );
                }
            });
        } catch (Exception e) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    label.setText(ERR_GETTING_BALANCE);
                    LOGGER.error(ERR_GETTING_BALANCE, e);
                    Utils.showError(ERR_GETTING_BALANCE);
                }
            });
        }
    }
}
