package com.credits.wallet.desktop.controller;

import com.credits.general.exception.CreditsException;
import com.credits.general.util.Callback;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SmartContractsUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.credits.wallet.desktop.AppState.coinsKeeper;
import static com.credits.wallet.desktop.AppState.contractInteractionService;

/**
 * Created by goncharov-eg on 07.02.2018.
 */
//TODO need refactoring
public class NewCoinController implements FormInitializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(NewCoinController.class);

    private static final String ERR_COIN = "You must enter coin mnemonic";
    private static final String ERR_TOKEN = "You must enter token";
    private static final String ERR_COIN_DUPLICATE = "Coin already exists";

    @FXML
    private TextField txToken;
    @FXML
    private TextField txCoin;

    @FXML
    private Label labelErrorToken;
    @FXML
    private Label labelErrorCoin;

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.WALLET,this);
    }

    @FXML
    private void handleSave(){

        clearFormErrors();

        String coinName = txCoin.getText().replace(";", "");
        String smartContractAddress = txToken.getText().replace(";", "");

        if (validateData(coinName, smartContractAddress)) {
            return;
        }

        addSmartContractTokenBalance(coinName, smartContractAddress);
        VistaNavigator.loadVista(VistaNavigator.WALLET,this);
    }

    private boolean validateData(String coinName, String smartContractAddress) {
        AtomicBoolean isValidationSuccessful = new AtomicBoolean(true);
        if (coinName.isEmpty()) {
            labelErrorCoin.setText(ERR_COIN);
            txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful.set(false);
        }

        if (smartContractAddress.isEmpty()) {
            labelErrorToken.setText(ERR_TOKEN);
            txToken.setStyle(txToken.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful.set(false);
        }

        coinsKeeper.getKeptObject().ifPresent(coinsMap -> {
            if(coinsMap.containsKey(coinName)) {
                labelErrorCoin.setText(ERR_COIN_DUPLICATE);
                txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
                isValidationSuccessful.set(false);
            }
        });

        return !isValidationSuccessful.get();
    }

    public static void addSmartContractTokenBalance(String coinName, String smartContractAddress) {
        contractInteractionService.getSmartContractBalance(smartContractAddress, new Callback<BigDecimal>() {
            @Override
            public void onSuccess(BigDecimal balance) throws CreditsException {
                    SmartContractsUtils.saveSmartInTokenList(coinName, balance, smartContractAddress);
                if(balance != null){
                    FormUtils.showPlatformInfo("Coin \"" + coinName + "\" was created successfully");
                }
            }

            @Override
            public void onError(Throwable e) {
                FormUtils.showError("Coin can't created. Reason: " + e.getMessage());
            }
        });
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {
        clearFormErrors();
    }

    private void clearFormErrors() {
        labelErrorToken.setText("");
        labelErrorCoin.setText("");

        txToken.setStyle(txToken.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
    }
}
