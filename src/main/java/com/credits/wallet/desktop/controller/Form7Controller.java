package com.credits.wallet.desktop.controller;


import com.credits.common.exception.CreditsCommonException;
import com.credits.common.utils.Converter;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.CoinsUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SmartContractUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class Form7Controller extends Controller implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(Form7Controller.class);

    @FXML
    BorderPane bp;

    @FXML
    private Label toAddress;

    @FXML
    private Label amountInCs;

    @FXML
    private Label transactionFeeValue;

    @FXML
    private void handleBack() {
        AppState.noClearForm6 = true;
        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @FXML
    private void handleGenerate() {
        try {
            String coin = AppState.coin;
            if(coin.equals("cs")) {
                ApiUtils.callCreateTransaction();
            } else {
                if (CoinsUtils.getCoins().get(coin)!= null) {
                    SmartContractUtils.transferTo(CoinsUtils.getCoins().get(coin),AppState.toAddress,AppState.amount);
                }
            }
        } catch (LevelDbClientException | CreditsNodeException | CreditsCommonException e) {
            LOGGER.error(AppState.NODE_ERROR + ": " + e.getMessage(), e);
            FormUtils.showError(AppState.NODE_ERROR);
            return;
        }

        VistaNavigator.loadVista(VistaNavigator.FORM_8);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
FormUtils.resizeForm(bp);
        this.toAddress.setText(AppState.toAddress);
        this.amountInCs.setText(Converter.toString(AppState.amount) + " " + AppState.coin);
    }

}
