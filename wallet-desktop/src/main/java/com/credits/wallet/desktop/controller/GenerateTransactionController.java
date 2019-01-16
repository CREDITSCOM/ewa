package com.credits.wallet.desktop.controller;


import com.credits.client.node.pojo.TransactionFlowResultData;
import com.credits.client.node.util.TransactionIdCalculateUtils;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.Callback;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.wallet.desktop.AppState.CREDITS_TOKEN_NAME;
import static com.credits.wallet.desktop.AppState.NODE_ERROR;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.coinsKeeper;
import static com.credits.wallet.desktop.AppState.contractInteractionService;
import static com.credits.wallet.desktop.utils.ApiUtils.createTransaction;

/**
 * Created by Rustem.Saidaliyev on 26.01.2018.
 */
public class GenerateTransactionController implements FormInitializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(GenerateTransactionController.class);

    @FXML
    public Label coinType;

    @FXML
    private TextField transactionToAddress;

    @FXML TextField transactionText;

    @FXML
    private TextField transactionAmount;

    @FXML
    private TextField transactionFeeValue;

    @FXML
    private void handleBack() {
        String toAddress = transactionToAddress.getText();
        String amount = transactionAmount.getText();
        Map<String, Object> params = new HashMap<>();
        params.put("transactionToAddress",toAddress);
        params.put("transactionAmount",amount);
        VistaNavigator.loadVista(VistaNavigator.WALLET, params, this);
    }

    @FXML
    private void handleGenerate() {
        String toAddress = transactionToAddress.getText();
        try {
            if(coinType.equals(CREDITS_TOKEN_NAME)) {
                CompletableFuture
                    .supplyAsync(() -> TransactionIdCalculateUtils.calcTransactionIdSourceTarget(AppState.nodeApiService,account,toAddress,
                        true),threadPool)
                    .thenApply((transactionData) -> createTransaction(transactionData, GeneralConverter.toBigDecimal(
                        transactionAmount.getText()), transactionText.getText()))
                    .whenComplete(handleCallback(handleTransactionResult()));
            } else {
                coinsKeeper.getKeptObject().ifPresent(coinsMap ->
                    Optional.ofNullable(coinsMap.get(coinType)).ifPresent(
                        coin -> contractInteractionService.transferTo(coin, toAddress, GeneralConverter.toBigDecimal(
                            transactionAmount.getText()), handleTransferTokenResult())));
            }
        } catch (CreditsException e) {
            LOGGER.error(NODE_ERROR + ": " + e.getMessage(), e);
            FormUtils.showError(NODE_ERROR);
            return;
        }

        VistaNavigator.loadVista(VistaNavigator.WALLET,this);
    }

    private Callback<String> handleTransferTokenResult() {
        return new Callback<String>() {
            @Override
            public void onSuccess(String message) throws CreditsException {
                FormUtils.showPlatformInfo(message);
            }

            @Override
            public void onError(Throwable e) {
                FormUtils.showPlatformError(e.getLocalizedMessage());
            }
        };
    }

    private Callback<Pair<Long, TransactionFlowResultData>> handleTransactionResult() {
        return new Callback<Pair<Long, TransactionFlowResultData>>() {
            @Override
            public void onSuccess(Pair<Long, TransactionFlowResultData> resultData) {
                ApiUtils.saveTransactionRoundNumberIntoMap(resultData.getRight().getRoundNumber(),
                    resultData.getLeft());
                FormUtils.showPlatformInfo("Transaction created");
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("Failed!", e);
                FormUtils.showPlatformError(e.getMessage());
            }
        };
    }

    @Override
    public void initializeForm(Map<String,Object> objects) {

        transactionToAddress.setText(objects.get("transactionToAddress").toString());
        transactionAmount.setText(objects.get("transactionAmount").toString());
        transactionText.setText(objects.get("transactionText").toString());
        coinType.setText(objects.get("coinType").toString());
    }
}
