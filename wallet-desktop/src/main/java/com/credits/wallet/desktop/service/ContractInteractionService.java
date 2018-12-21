package com.credits.wallet.desktop.service;

import com.credits.client.executor.pojo.ExecuteResponseData;
import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.util.TransactionIdCalculateUtils;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Callback;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.SmartContractsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.general.util.VariantConverter.STRING_TYPE;
import static com.credits.general.util.VariantConverter.createVariantObject;
import static com.credits.general.util.VariantConverter.objectToVariant;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.contractExecutorService;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.utils.ApiUtils.createSmartContractTransaction;
import static java.util.Arrays.asList;

/**
 * Created by Igor Goryunov on 28.10.2018
 */
public class ContractInteractionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContractInteractionService.class);
    public static final String TRANSFER_METHOD = "transfer";
    public static final String BALANCE_OF_METHOD = "balanceOf";
    public static final String GET_NAME_METHOD = "getName";

    public void getSmartContractBalance(String smartContractAddress, Callback<BigDecimal> callback) {
        CompletableFuture.supplyAsync(() -> nodeApiService.getSmartContract(smartContractAddress), threadPool)
            .thenApply(this::getBalance)
            .whenComplete(handleCallback(callback));
    }

    private BigDecimal getBalance(SmartContractData sc) {
        return new BigDecimal(executeSmartContract(account, sc, BALANCE_OF_METHOD, AppState.DEFAULT_EXECUTION_TIME,
            variantOf(STRING_TYPE, account)));
    }

    private String getName(SmartContractData sc) {
        return executeSmartContract(account, sc, GET_NAME_METHOD, AppState.DEFAULT_EXECUTION_TIME);
    }


    public void getSmartContractBalanceAndName(String smartContractAddress) {
        CompletableFuture.supplyAsync(() -> nodeApiService.getSmartContract(smartContractAddress), threadPool)
            .thenAccept((sc) -> CompletableFuture.supplyAsync(() -> getName(sc))
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> getBalance(sc)), (name, balance) -> {
                    SmartContractsUtils.saveSmartInTokenList(name, balance, smartContractAddress);
                }));
    }

    public void transferTo(String smartContractAddress, String target, BigDecimal amount, Callback<String> callback) {
        CompletableFuture.supplyAsync(() -> nodeApiService.getSmartContract(smartContractAddress), threadPool)
            .thenApply((sc) -> {
                sc.setMethod(TRANSFER_METHOD);
                sc.setParams(asList(createVariantObject(STRING_TYPE, target),
                    createVariantObject(STRING_TYPE, amount.toString())));
                TransactionIdCalculateUtils.CalcTransactionIdSourceTargetResult transactionData =
                    TransactionIdCalculateUtils.calcTransactionIdSourceTarget(AppState.nodeApiService, account,
                        sc.getBase58Address(), true);
                return createSmartContractTransaction(transactionData, sc).getRight().getCode().name();
            })
            .whenComplete(handleCallback(callback));
    }


    private String executeSmartContract(String smartContractAddress, SmartContractData sc, String methodName,
        long executionTime, Variant... params) {
        if (sc == null || sc.getObjectState().length == 0) {
            throw new NodeClientException("SmartContract " + smartContractAddress + " not found");
        }

        ExecuteResponseData response =
            contractExecutorService.executeContractMethod(GeneralConverter.decodeFromBASE58(smartContractAddress),
                sc.getSmartContractDeployData().getByteCode(), sc.getObjectState(), methodName, asList(params),
                executionTime);

        if (response.getCode() != SUCCESS) {
            throw new NodeClientException("Failure. Node response: " + response.getMessage());
        }

        return response.getExecuteBytecodeResult().getV_string();
    }

    private Variant variantOf(String type, String value) {
        return objectToVariant(createVariantObject(type, value));
    }

}
