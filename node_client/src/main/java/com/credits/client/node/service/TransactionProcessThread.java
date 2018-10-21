package com.credits.client.node.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionRoundData;
import com.credits.client.node.thrift.API;
import com.credits.client.node.thrift.Transaction;
import com.credits.client.node.thrift.TransactionFlowResult;
import com.credits.client.node.util.NodeClientUtils;
import com.credits.client.node.util.NodePojoConverter;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.ThriftClientPool;
import com.credits.general.util.Converter;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class TransactionProcessThread implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProcessThread.class);
    private final Transaction transaction;
    private final Callback callback;
    private final ThriftClientPool<API.Client> pool;

    TransactionProcessThread(Transaction transaction, ThriftClientPool<API.Client> pool, Callback callback) {
        this.callback = callback;
        this.transaction = transaction;
        this.pool = pool;
    }

    @Override
    public void run() {
        API.Client client = pool.getResource();
        Thread.currentThread().setName("trnId = " + String.valueOf(transaction.getId()));
        NodeApiServiceImpl.sourceMap.computeIfAbsent(NodeApiServiceImpl.account, key -> new ConcurrentHashMap<>());
        Map<Long, TransactionRoundData> sourceMap = NodeApiServiceImpl.sourceMap.get(NodeApiServiceImpl.account);
        try {
            LOGGER.info("Start execute transaction");
            sourceMap.put(transaction.getId(),
                new TransactionRoundData(NodePojoConverter.transactionToTransactionData(transaction)));
            TransactionFlowResult result = client.TransactionFlow(transaction);
            ApiResponseData resultData = NodePojoConverter.apiResponseToApiResponseData(result.getStatus(),
                result.isSetSmart_contract_result() ? result.getSmart_contract_result() : null);
            NodeClientUtils.logApiResponse(result.getStatus());
            NodeClientUtils.processApiResponse(result.getStatus());

            resultData.setSource(Converter.encodeToBASE58(transaction.getSource()));
            resultData.setTarget(Converter.encodeToBASE58(transaction.getTarget()));
            callback.onSuccess(resultData);
            pool.returnResource(client);
            LOGGER.info("End execute transaction");
        } catch (TException e) {
            LOGGER.info("TException");
            callback.onError(e);
            pool.returnBrokenResource(client);
        } catch (NodeClientException e) {
            LOGGER.info("Node client exception");
            callback.onError(e);
            pool.returnResource(client);
        }
    }

    public interface Callback{
        void onSuccess(ApiResponseData resultData);
        void onError(Exception e);
    }
}
