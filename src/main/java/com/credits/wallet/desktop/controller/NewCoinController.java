package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsCommonException;
import com.credits.leveldb.client.ApiTransactionThreadRunnable;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.thrift.generated.APIResponse;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.CoinsUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SmartContractUtils;
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

/**
 * Created by goncharov-eg on 07.02.2018.
 */
public class NewCoinController extends Controller implements Initializable {
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
    private void handleSave() throws CreditsCommonException, LevelDbClientException, CreditsNodeException {
        clearLabErr();

        String coin = txCoin.getText().replace(";", "");
        String token = txToken.getText().replace(";", "");

        // VALIDATE
        boolean isValidationSuccessful = true;
        if (coin.isEmpty()) {
            labelErrorCoin.setText(ERR_COIN);
            txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (token.isEmpty()) {
            labelErrorToken.setText(ERR_TOKEN);
            txToken.setStyle(txToken.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (CoinsUtils.getCoins().containsKey(coin)) {
            labelErrorCoin.setText(ERR_COIN_DUPLICATE);
            txCoin.setStyle(txCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (!isValidationSuccessful) {
            return;
        }

        SmartContractUtils.getSmartContractBalance(token, new ApiTransactionThreadRunnable.Callback<APIResponse>() {
            @Override
            public void onSuccess(APIResponse apiResponse) {
                if(apiResponse.getRet_val() != null) {
                    BigDecimal balance = new BigDecimal(apiResponse.getRet_val().getV_string()).setScale(13, BigDecimal.ROUND_DOWN);
                    if (balance != null && balance.compareTo(BigDecimal.ZERO) >= 0) {
                        CoinsUtils.saveCoinsToFile(coin + ";" + token);
                        FormUtils.showPlatformInfo("Make new coin was successful");
                    } else {
                        FormUtils.showPlatformInfo("Error make new coin");
                    }
                } else {
                    FormUtils.showPlatformInfo("Error make new coin");
                }
            }

            @Override
            public void onError(Exception e) {
                FormUtils.showPlatformInfo("Error make new coin");
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
