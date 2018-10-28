package com.credits.client.node.thrift.call;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.CreateTransactionData;
import com.credits.client.node.pojo.TransactionRoundData;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.util.exception.ConverterException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TransactionFlowThread extends ThriftCallThread<ApiResponseData> {
    private CreateTransactionData transaction;
    private NodeApiService nodeApiService;

    public TransactionFlowThread(NodeApiService nodeApiService, Callback<ApiResponseData> callback, CreateTransactionData transaction) {
        super(callback);
        this.nodeApiService = nodeApiService;
        this.transaction = transaction;
    }

    @Override
    protected ApiResponseData call() throws NodeClientException, ConverterException {
        Thread.currentThread().setName("trnId = " + String.valueOf(transaction.getInnerId()));

        ApiResponseData responseData = nodeApiService.createTransaction(transaction, false);
        NodeApiServiceImpl.sourceMap.computeIfAbsent(NodeApiServiceImpl.account, key -> new ConcurrentHashMap<>());
        Map<Long, TransactionRoundData> sourceMap = NodeApiServiceImpl.sourceMap.get(NodeApiServiceImpl.account);
        TransactionRoundData transactionRoundData = new TransactionRoundData(transaction, responseData.getRoundNumber());
        sourceMap.put(transaction.getInnerId(), transactionRoundData);
        return responseData;
    }
}
