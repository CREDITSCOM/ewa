package com.credits.client.node.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.thrift.API;
import com.credits.client.node.thrift.PoolInfoGetResult;
import com.credits.client.node.thrift.PoolListGetResult;
import com.credits.client.node.thrift.SmartContractAddressesListGetResult;
import com.credits.client.node.thrift.SmartContractGetResult;
import com.credits.client.node.thrift.SmartContractsListGetResult;
import com.credits.client.node.thrift.Transaction;
import com.credits.client.node.thrift.TransactionFlowResult;
import com.credits.client.node.thrift.TransactionGetResult;
import com.credits.client.node.thrift.TransactionsGetResult;
import com.credits.client.node.thrift.TransactionsStateGetResult;
import com.credits.client.node.thrift.WalletBalanceGetResult;
import com.credits.client.node.thrift.WalletDataGetResult;
import com.credits.client.node.thrift.WalletIdGetResult;
import com.credits.client.node.thrift.WalletTransactionsCountGetResult;
import com.credits.client.node.util.NodePojoConverter;
import com.credits.general.thrift.ThriftClientPool;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
class NodeThriftApiClient implements NodeThriftApi {
    private static volatile NodeThriftApiClient instance;
    private ExecutorService threadPoolExecutor;
    private ThriftClientPool<API.Client> pool;

    private NodeThriftApiClient(String apiServerHost, Integer apiServerPort) {
        pool = new ThriftClientPool<>(API.Client::new, apiServerHost, apiServerPort);
        threadPoolExecutor = Executors.newCachedThreadPool();
    }

    public static NodeThriftApiClient getInstance(String host, Integer port) {
        NodeThriftApiClient localInstance = instance;
        if (localInstance == null) {
            synchronized (NodeThriftApiClient.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new NodeThriftApiClient(host, port);
                }
            }
        }
        return localInstance;
    }

    public ThriftClientPool<API.Client> getPool() {
        return pool;
    }

    public TransactionsGetResult getTransactions(byte[] address, long offset, long limit) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.TransactionsGet(ByteBuffer.wrap(address), offset, limit));
    }

    public TransactionGetResult getTransaction(TransactionIdData transactionIdData) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.TransactionGet(NodePojoConverter.transactionIdDataToTransactionId(transactionIdData)));
    }

    public PoolInfoGetResult getPoolInfo(ByteBuffer hashByteBuffer, long index) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.PoolInfoGet(hashByteBuffer, index));
    }

    public PoolListGetResult getPoolList(Long offset, Long limit) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.PoolListGet(offset, limit));
    }

    public WalletBalanceGetResult getBalance(byte[] address) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.WalletBalanceGet(ByteBuffer.wrap(address)));
    }

    public SmartContractGetResult getSmartContract(byte[] address) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.SmartContractGet(ByteBuffer.wrap(address)));
    }

    public SmartContractsListGetResult getSmartContracts(byte[] address) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.SmartContractsListGet(ByteBuffer.wrap(address)));
    }

    public SmartContractAddressesListGetResult getSmartContractAddresses(byte[] address) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.SmartContractAddressesListGet(ByteBuffer.wrap(address)));
    }

    public WalletDataGetResult getWalletData(byte[] address) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.WalletDataGet(ByteBuffer.wrap(address)));
    }

    public WalletIdGetResult getWalletId(byte[] address) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.WalletIdGet(ByteBuffer.wrap(address)));
    }

    public WalletTransactionsCountGetResult getWalletTransactionsCount(byte[] address) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.WalletTransactionsCountGet(ByteBuffer.wrap(address)));
    }

    public TransactionFlowResult executeSyncTransactionFlow(Transaction transaction) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.TransactionFlow(transaction));
    }

    public TransactionsStateGetResult getTransactionsState(byte[] address, List<Long> transactionIdList) throws NodeClientException {
        API.Client client = pool.getResource();
        return callThrift(client, () -> client.TransactionsStateGet(ByteBuffer.wrap(address),transactionIdList));
    }

    public void executeAsyncTransactionFlow(Transaction transaction, TransactionProcessThread.Callback callback) {
        TransactionProcessThread threadRunnable = new TransactionProcessThread(transaction, pool, callback);
        threadPoolExecutor.submit(threadRunnable);
    }

    private <R> R callThrift(API.Client client, Function<R> method) throws NodeClientException {
        try {
            R res = method.apply();
            pool.returnResource(client);
            return res;
        } catch (TException e) {
            pool.returnBrokenResource(client);
            throw new NodeClientException(e);
        }
    }

    private interface Function<R> {
        R apply() throws TException, NodeClientException;
    }
}