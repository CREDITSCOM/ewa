package com.credits.wallet.desktop.thread;

import com.credits.common.utils.Converter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Created by goncharov-eg on 10.04.2018.
 */
public class GetBalanceUpdater implements Runnable {
    private static final String ERR_GETTING_BALANCE = "Error getting balance";

    private static Logger LOGGER = LoggerFactory.getLogger(GetBalanceUpdater.class);

    private String coin;
    private Label label;

    public GetBalanceUpdater(String coin, Label label) {
        this.coin = coin;
        this.label = label;
    }

    @Override
    public void run() {
        try {
            BigDecimal balance = AppState.apiClient.getBalance(AppState.account, coin);
            AppState.balance = balance;
            label.setText(Converter.toString(balance));
        } catch (Exception e) {
            //label.setText(ERR_GETTING_BALANCE);
            //LOGGER.error(ERR_GETTING_BALANCE, e);
            //FormUtils.showError(ERR_GETTING_BALANCE);

            label.setText(AppState.NODE_ERROR);
            LOGGER.error(AppState.NODE_ERROR + ": " + e.toString(), e);
            FormUtils.showError(AppState.NODE_ERROR);
        }
    }
}
