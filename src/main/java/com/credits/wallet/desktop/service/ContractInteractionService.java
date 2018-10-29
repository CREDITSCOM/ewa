package com.credits.wallet.desktop.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.thrift.call.ThriftCallThread.Callback;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.thrift.generate.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static com.credits.general.util.Converter.objectToVariant;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.contractExecutorService;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils.createVariantObject;

/**
 * Created by Igor Goryunov on 28.10.2018
 */
public class ContractInteractionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContractInteractionService.class);

    public void getSmartContractBalance(String smartContractAddress, Callback<BigDecimal> callback) {
        CompletableFuture
            .supplyAsync(() -> nodeApiService.getSmartContract(smartContractAddress))
            .thenApply((sc) -> executeSmartContract(smartContractAddress, sc, "balanceOf", variantOf("String", account)))
            .whenComplete((result, error) -> {
                if (error == null)
                    callback.onSuccess(result);
                else{
                    LOGGER.error(error.getLocalizedMessage());
                    callback.onError(error);
                }

            });
    }

    private BigDecimal executeSmartContract(String smartContractAddress, SmartContractData sc, String methodName, Variant... params) {
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

        return new BigDecimal(response.getScExecRetVal().getV_string());
    }

    private Variant variantOf(String type, String value){
       return objectToVariant(createVariantObject(type, value));
    }

    //todo add implementation
    public void transferTo(String smart, String target, BigDecimal amount) {
        //        try {
        //            String method = "transfer";
        //            List<Object> params = new ArrayList<>();
        //
        //            params.add(createVariantObject("String", target));
        //            params.add(createVariantObject("String",amount.toString()));
        //            SmartContractData smartContractData = nodeApiService.getSmartContract(smart);
        //            if (smartContractData == null) {
        //                FormUtils.showInfo("SmartContract not found");
        //                return;
        //            }
        //            ApiUtils.executeSmartContractProcess(method, params, smartContractData,
        //                new Callback() {
        //                    @Override
        //                    public void onSuccess(ApiResponseData resultData) {
        //                        FormUtils.showPlatformInfo("Transfer is ok");
        //                    }
        //
        //                    @Override
        //                    public void onError(Exception e) {
        //                        FormUtils.showPlatformError(e.getMessage());
        //                    }
        //                });
        //        } catch (Exception e) {
        //            LOGGER.error(e.getMessage(), e);
        //            FormUtils.showError(e.getMessage());
        //        }


    }
}
