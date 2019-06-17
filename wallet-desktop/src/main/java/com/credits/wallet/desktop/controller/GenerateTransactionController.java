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
import static com.credits.wallet.desktop.utils.ApiUtils.createTransaction;


public class GenerateTransactionController extends AbstractController {
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

    private short actualOfferedMaxFee16Bits;

    @FXML
    private void handleBack() {
        Map<String, Object> params = new HashMap<>();
        params.put("transactionFee",transactionFeeValue.getText());
        params.put("transactionToAddress",transactionToAddress.getText());
        params.put("transactionAmount",transactionAmount.getText());
        params.put("transactionText",transactionText.getText());
        params.put("coinType", coinType.getText());
        VistaNavigator.loadVista(VistaNavigator.WALLET, params);
    }

    @FXML
    private void handleGenerate() {
        String toAddress = transactionToAddress.getText();
        try {
            if(coinType.getText().equals(CREDITS_TOKEN_NAME)) {
                CompletableFuture
                    .supplyAsync(() -> TransactionIdCalculateUtils.calcTransactionIdSourceTarget(AppState.getNodeApiService(),session.account,
                                                                                                                             toAddress,
                        true),threadPool)
                    .thenApply((transactionData) -> createTransaction(transactionData, GeneralConverter.toBigDecimal(
                        transactionAmount.getText()), actualOfferedMaxFee16Bits, transactionText.getText(),session))
                    .whenComplete(handleCallback(handleTransactionResult()));
            } else {
                session.coinsKeeper.getKeptObject().ifPresent(coinsMap ->
                    Optional.ofNullable(coinsMap.get(coinType.getText())).ifPresent(
                        coin -> session.contractInteractionService.transferTo(coin, toAddress, GeneralConverter.toBigDecimal(
                            transactionAmount.getText()), actualOfferedMaxFee16Bits, handleTransferTokenResult())));
            }
        } catch (CreditsException e) {
            LOGGER.error(NODE_ERROR + ": " + e.getMessage(), e);
            FormUtils.showError(NODE_ERROR);
            return;
        }

        VistaNavigator.loadVista(VistaNavigator.WALLET);
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
                    resultData.getLeft(),session);
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
        transactionFeeValue.setText(objects.get("transactionFee").toString());
        transactionAmount.setText(objects.get("transactionAmount").toString());
        transactionText.setText(objects.get("transactionText").toString());
        coinType.setText(objects.get("coinType").toString());
        actualOfferedMaxFee16Bits = (Short)objects.get("actualOfferedMaxFee16Bits");

    }

    @Override
    public void formDeinitialize() {

    }
}
