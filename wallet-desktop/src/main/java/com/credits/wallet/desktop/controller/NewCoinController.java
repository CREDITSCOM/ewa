package com.credits.wallet.desktop.controller;

import com.credits.general.exception.CreditsException;
import com.credits.general.util.Callback;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.CoinsUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.wallet.desktop.AppState.contractInteractionService;

/**
 * Created by goncharov-eg on 07.02.2018.
 */
public class NewCoinController implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(NewCoinController.class);

    private static final String ERR_COIN = "You must enter coin mnemonic";
    private static final String ERR_TOKEN = "You must enter token";
    private static final String ERR_COIN_DUPLICATE = "Coin already exists";

    @FXML
    BorderPane bp;

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
        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @FXML
    private void handleSave(){

        clearLabErr();

        String coinName = txCoin.getText().replace(";", "");
        String smartContractAddress = txToken.getText().replace(";", "");

        // VALIDATE
        boolean isValidationSuccessful = true;
        if (coinName.isEmpty()) {
            labelErrorCoin.setText(ERR_COIN);
            txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (smartContractAddress.isEmpty()) {
            labelErrorToken.setText(ERR_TOKEN);
            txToken.setStyle(txToken.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (CoinsUtils.getCoins().containsKey(coinName)) {
            labelErrorCoin.setText(ERR_COIN_DUPLICATE);
            txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (!isValidationSuccessful) {
                return;
        }

        contractInteractionService.getSmartContractBalance(smartContractAddress, new Callback<BigDecimal>() {
            @Override
            public void onSuccess(BigDecimal balance) throws CreditsException {
                if(balance != null){
                    ConcurrentHashMap<String, String> coins = CoinsUtils.getCoins();
                    coins.put(coinName, smartContractAddress);
                    CoinsUtils.saveCoinsToFile(coins);
                    FormUtils.showPlatformInfo("Coin \"" + coinName + "\" was created successfully");
                }
            }

            @Override
            public void onError(Throwable e) {
                FormUtils.showError("Coin can't created. Reason: " + e.getMessage());
            }
        });
        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);
        clearLabErr();
    }

    private void clearLabErr() {
        labelErrorToken.setText("");
        labelErrorCoin.setText("");

        txToken.setStyle(txToken.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
    }
}
