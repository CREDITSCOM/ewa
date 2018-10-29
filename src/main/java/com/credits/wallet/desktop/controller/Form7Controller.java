package com.credits.wallet.desktop.controller;


import com.credits.client.node.thrift.call.ThriftCallThread.Callback;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.Converter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.CoinsUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static com.credits.wallet.desktop.AppState.contractInteractionService;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class Form7Controller extends Controller implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(Form7Controller.class);

    @FXML
    BorderPane bp;

    @FXML
    private TextField toAddress;

    @FXML TextField transactionData;

    @FXML
    private TextField amountInCs;

    @FXML
    private TextField transactionFeeValue;

    @FXML
    private void handleBack() {
        AppState.noClearForm6 = true;
        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @FXML
    private void handleGenerate() {
        try {
            String coin = AppState.coin;
            if(coin.equals("CS")) {
                ApiUtils.callCreateTransaction();
            } else {
                if (CoinsUtils.getCoins().get(coin)!= null) {
                    contractInteractionService.transferTo(CoinsUtils.getCoins().get(coin), AppState.toAddress, AppState.amount, new Callback<String>() {
                        @Override
                        public void onSuccess(String message) throws CreditsException {
                            FormUtils.showPlatformInfo(message);
                        }

                        @Override
                        public void onError(Throwable e) {
                            FormUtils.showPlatformError(e.getLocalizedMessage());
                        }
                    });
                }
            }
        } catch (CreditsException e) {
            LOGGER.error(AppState.NODE_ERROR + ": " + e.getMessage(), e);
            FormUtils.showError(AppState.NODE_ERROR);
            return;
        }

        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);
        this.toAddress.setText(AppState.toAddress);
        this.amountInCs.setText(Converter.toString(AppState.amount) + " " + AppState.coin);
        this.transactionData.setText(AppState.text);
    }

}
