package com.credits.wallet.desktop.service;

import com.credits.client.executor.pojo.ExecuteResponseData;
import com.credits.client.executor.util.ContractExecutorPojoConverter;
import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.util.TransactionIdCalculateUtils;
import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.generated.ExecuteByteCodeResult;
import com.credits.general.util.Callback;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.general.util.variant.VariantUtils.STRING_TYPE;
import static com.credits.general.util.variant.VariantUtils.createVariantData;
import static com.credits.wallet.desktop.AppState.contractExecutorService;
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
        return new BigDecimal(executeSmartContract(session.account, sc, BALANCE_OF_METHOD, AppState.DEFAULT_EXECUTION_TIME,
            createVariantData(STRING_TYPE, session.account)));
    }

    private String getName(SmartContractData sc) {
        return executeSmartContract(session.account, sc, GET_NAME_METHOD, AppState.DEFAULT_EXECUTION_TIME);
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
                sc.setParams(asList(createVariantData(STRING_TYPE, target), createVariantData(STRING_TYPE, amount.toString())));
                TransactionIdCalculateUtils.CalcTransactionIdSourceTargetResult transactionData =
                    TransactionIdCalculateUtils.calcTransactionIdSourceTarget(AppState.nodeApiService, session.account,
                        sc.getBase58Address(), true);
                return createSmartContractTransaction(transactionData, offeredMaxFee, sc,session).getRight().getCode().name();
            })
            .whenComplete(handleCallback(callback));
    }


    private String executeSmartContract(String initiatorAddress, SmartContractData sc, String methodName,
        long executionTime, VariantData... params) {
        if (sc == null || sc.getObjectState().length == 0) {
            throw new NodeClientException("SmartContract " + initiatorAddress + " not found");
        }

        ExecuteByteCodeResult executeResponseData =
            //todo add request to node accessId
            contractExecutorService.executeContractMethod(0, GeneralConverter.decodeFromBASE58(initiatorAddress), sc.getAddress(),
                GeneralConverter.byteCodeObjectsDataToByteCodeObjects(
                    sc.getSmartContractDeployData().getByteCodeObjects()), sc.getObjectState(), methodName,
                asList(params), executionTime);
        ExecuteResponseData response = ContractExecutorPojoConverter.executeByteCodeResultToExecuteResponseData(executeResponseData);

        if (response.getCode() != SUCCESS) {
            throw new NodeClientException("Failure. Node response: " + response.getMessage());
        }

        return response.getExecuteBytecodeResult().getV_string();
    }
}
