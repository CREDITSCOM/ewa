package com.credits.client.node.thrift.call;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.pojo.TransactionRoundData;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.util.exception.ConverterException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
public class TransactionFlowThread extends ThriftCallThread<ApiResponseData> {
    private TransactionFlowData transaction;
    private NodeApiService nodeApiService;

    public TransactionFlowThread(NodeApiService nodeApiService, TransactionFlowData transaction, com.credits.general.util.Callback<ApiResponseData> callback) {
        super(nodeApiService, callback);
        this.nodeApiService = nodeApiService;
        this.transaction = transaction;
    }

    @Override
    protected ApiResponseData call() throws NodeClientException, ConverterException {
        Thread.currentThread().setName("trnId = " + String.valueOf(transaction.getId()));

        ApiResponseData responseData = nodeApiService.transactionFlow(transaction);
        NodeApiServiceImpl.sourceMap.computeIfAbsent(NodeApiServiceImpl.account, key -> new ConcurrentHashMap<>());
        Map<Long, TransactionRoundData> sourceMap = NodeApiServiceImpl.sourceMap.get(NodeApiServiceImpl.account);
        TransactionRoundData transactionRoundData = new TransactionRoundData(transaction, responseData.getRoundNumber());
        sourceMap.put(transaction.getId(), transactionRoundData);
        return responseData;
    }
}
