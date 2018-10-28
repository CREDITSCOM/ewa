package com.credits.wallet.desktop.service;

import com.credits.client.node.thrift.call.ThriftCallThread.Callback;

import java.math.BigDecimal;

/**
 * Created by Igor Goryunov on 28.10.2018
 */
public class ContractInteractionService {

    //todo add implementation
    public void getSmartContractBalance(String smartContractAddress, Callback<BigDecimal> callback) {
//        String method = "balanceOf";
//        ExecuteSmartContractData executeSmartContractData = new ExecuteSmartContractData(ByteBuffer.wrap(decodeFromBASE58(AppState.account)),
//            method,
//            Collections.singletonList(SourceCodeUtils.createVariantObject("String", AppState.account)));
//        AppState.levelDbService.getSmartContractBalance(executeSmartContractData,smart, callback);
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
