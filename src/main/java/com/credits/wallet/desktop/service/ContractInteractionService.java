package com.credits.wallet.desktop.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static com.credits.general.util.Converter.objectToVariant;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.contractExecutorService;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils.STRING_TYPE;
import static com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils.createVariantObject;

/**
 * Created by Igor Goryunov on 28.10.2018
 */
public class ContractInteractionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContractInteractionService.class);
    public static final String TRANSFER_METHOD = "transfer";
    public static final String BALANCE_OF_METHOD = "balanceOf";

    public void getSmartContractBalance(String smartContractAddress, Callback<BigDecimal> callback) {
        CompletableFuture
            .supplyAsync(() -> nodeApiService.getSmartContract(smartContractAddress))
            .thenApply((sc) -> new BigDecimal(executeSmartContract(smartContractAddress, sc, BALANCE_OF_METHOD, variantOf(STRING_TYPE, account))))
            .whenComplete(handleCallback(callback));
    }

    public void transferTo(String smartContractAddress, String target, BigDecimal amount, Callback<String> callback) {
        CompletableFuture
            .supplyAsync(() -> nodeApiService.getSmartContract(smartContractAddress))
            .thenApply((sc) -> executeSmartContract(smartContractAddress, sc, TRANSFER_METHOD, variantOf(STRING_TYPE, target), variantOf(STRING_TYPE, amount.toString())))
            .whenComplete(handleCallback(callback));
    }

    private <T> BiConsumer<T, Throwable> handleCallback(Callback<T> callback) {
        return (result, error) -> {
            if (error == null)
                callback.onSuccess(result);
            else{
                LOGGER.error(error.getLocalizedMessage());
                callback.onError(error);
            }
        };
    }

    private String executeSmartContract(String smartContractAddress, SmartContractData sc, String methodName, Variant... params) {
        if (sc == null || sc.getObjectState().length == 0) {
            throw new NodeClientException("SmartContract " + smartContractAddress + " not found");
        }

        ApiResponseData response = contractExecutorService.executeContractMethod(
            sc.getAddress(),
            sc.getByteCode(),
            sc.getObjectState(),
            methodName,
            Arrays.asList(params));

        if(response.getCode() != SUCCESS) {
            throw new NodeClientException("Failure. Node response: " + response.getMessage());
        }

        return response.getScExecRetVal().getV_string();
    }

    private Variant variantOf(String type, String value){
        return objectToVariant(createVariantObject(type, value));
    }

}
