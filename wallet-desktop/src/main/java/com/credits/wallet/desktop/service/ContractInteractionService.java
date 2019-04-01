package com.credits.wallet.desktop.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.TransactionFlowResultData;
import com.credits.client.node.util.TransactionIdCalculateUtils;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Callback;
import com.credits.general.util.variant.VariantConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Session;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.client.node.util.TransactionIdCalculateUtils.calcTransactionIdSourceTarget;
import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static com.credits.general.util.GeneralConverter.createObjectFromString;
import static com.credits.general.util.Utils.getClassType;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.general.util.variant.VariantConverter.toVariant;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.utils.ApiUtils.createSmartContractTransaction;
import static java.util.Arrays.asList;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Created by Igor Goryunov on 28.10.2018
 */
public class ContractInteractionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContractInteractionService.class);
    public static final String TRANSFER_METHOD = "transfer";
    public static final String BALANCE_OF_METHOD = "balanceOf";
    public static final String GET_NAME_METHOD = "getName";
    public Session session;

    public ContractInteractionService(Session session) {
        this.session = session;
    }

    public void getSmartContractBalance(String smartContractAddress, Callback<BigDecimal> callback) {
        supplyAsync(() -> nodeApiService.getSmartContract(smartContractAddress), threadPool)
            .thenApply(this::getBalance)
            .whenComplete(handleCallback(callback));
    }

    private BigDecimal getBalance(SmartContractData sc) {
        return new BigDecimal(executeSmartContract(
            session.account,
            sc,
            BALANCE_OF_METHOD,
            toVariant(getClassType(session.account), createObjectFromString(session.account, String.class))));
    }

    private String getName(SmartContractData sc) {
        return executeSmartContract(session.account, sc, GET_NAME_METHOD);
    }


    /*public void getSmartContractBalanceAndName(String smartContractAddress) {
        supplyAsync(() -> nodeApiService.getSmartContract(smartContractAddress), threadPool)
            .thenAccept(sc -> supplyAsync(() -> getName(sc))
                .thenAcceptBoth(supplyAsync(() -> getBalance(sc)),
                (name, balance) -> SmartContractsUtils.saveSmartInTokenList(name, balance, smartContractAddress))
                    .whenComplete((aVoid, throwable) -> LOGGER.warn("cannot add balance of contract to tokens balances list. Reason: {}", throwable.getMessage())));
    }*/

    public void transferTo(String smartContractAddress, String target, BigDecimal amount, short offeredMaxFee, Callback<String> callback) {
        supplyAsync(() -> nodeApiService.getSmartContract(smartContractAddress), threadPool)
            .thenApply((sc) -> {
                sc.setMethod(TRANSFER_METHOD);
                sc.setParams(asList(
                    VariantConverter.toVariant(getClassType(target), createObjectFromString(target, String.class)),
                    VariantConverter.toVariant(String.class.getTypeName(), createObjectFromString(amount.toString(), String.class))));
                TransactionIdCalculateUtils.CalcTransactionIdSourceTargetResult transactionData =
                    TransactionIdCalculateUtils.calcTransactionIdSourceTarget(AppState.nodeApiService, session.account,
                                                                              sc.getBase58Address(), true);
                return createSmartContractTransaction(transactionData, offeredMaxFee, sc, new ArrayList<>(), session).getRight().getCode().name();
            })
            .whenComplete(handleCallback(callback));
    }


    private String executeSmartContract(String initiatorAddress, SmartContractData sc, String methodName, Variant... params) {
        if (sc == null || sc.getObjectState().length == 0) {
            throw new NodeClientException("SmartContract " + initiatorAddress + " not found");
        }
        sc.setMethod(methodName);
        sc.getParams().addAll(Arrays.asList(params));
        byte[] objectState = new byte[] {};
        sc.setObjectState(objectState);
        TransactionIdCalculateUtils.CalcTransactionIdSourceTargetResult calcTransactionIdSourceTargetResult =
            calcTransactionIdSourceTarget(AppState.nodeApiService, session.account, sc.getBase58Address(), true);

        //todo Коммисию пофиксить надо С НУЛЕМ НЕ РАБОТАЕТ
        Pair<Long, TransactionFlowResultData> smartContractTransaction =
            createSmartContractTransaction(calcTransactionIdSourceTargetResult, (short) 20613, sc, new ArrayList<>(),
                                           session);
        return smartContractTransaction.getValue().getContractResult().orElse(new Variant(V_STRING, "0")).getV_string();
        //todo add request to node accessId
    }
}
