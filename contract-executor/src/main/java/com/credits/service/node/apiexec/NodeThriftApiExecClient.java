package com.credits.service.node.apiexec;

import com.credits.client.executor.thrift.generated.apiexec.APIEXEC;
import com.credits.client.executor.thrift.generated.apiexec.GetSeedResult;
import com.credits.client.executor.thrift.generated.apiexec.GetSmartCodeResult;
import com.credits.client.executor.thrift.generated.apiexec.SendTransactionResult;
import com.credits.client.node.thrift.generated.Transaction;
import com.credits.client.node.thrift.generated.WalletIdGetResult;
import com.credits.exception.ApiClientException;
import com.credits.general.thrift.ThriftClientPool;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class NodeThriftApiExecClient implements NodeThriftApiExec {
    private static volatile NodeThriftApiExecClient instance;
    private final ExecutorService threadPoolExecutor;
    private final ThriftClientPool<APIEXEC.Client> pool;

    private NodeThriftApiExecClient(String apiServerHost, Integer apiServerPort) {
        pool = new ThriftClientPool<>(APIEXEC.Client::new, apiServerHost, apiServerPort);
        threadPoolExecutor = Executors.newCachedThreadPool();
    }

    public static NodeThriftApiExecClient getInstance(String host, Integer port) {
        NodeThriftApiExecClient localInstance = instance;
        if (localInstance == null) {
            synchronized (NodeThriftApiExecClient.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new NodeThriftApiExecClient(host, port);
                }
            }
        }
        return localInstance;
    }

    public ThriftClientPool<APIEXEC.Client> getPool() {
        return pool;
    }

    @Override
    public GetSeedResult getSeed(long accessId) throws ApiClientException {
        APIEXEC.Client client = pool.getResource();
        return callThrift(client, () -> client.GetSeed(accessId));
    }

    @Override
    public GetSmartCodeResult getSmartCode(long accessId, byte[] address) throws ApiClientException {
        APIEXEC.Client client = pool.getResource();
        return callThrift(client, () -> client.GetSmartCode(accessId, ByteBuffer.wrap(address)));
    }

    @Override
    public SendTransactionResult sendTransaction(Transaction transaction) throws ApiClientException {
        APIEXEC.Client client = pool.getResource();
        return callThrift(client, () -> client.SendTransaction(transaction));
    }

    @Override
    public WalletIdGetResult getWalletId(byte[] address) throws ApiClientException {
        APIEXEC.Client client = pool.getResource();
        return callThrift(client, () -> client.WalletIdGet(ByteBuffer.wrap(address)));
    }

    private <R> R callThrift(APIEXEC.Client client, Function<R> method) throws ApiClientException {
        try {
            R res = method.apply();
            pool.returnResource(client);
            return res;
        } catch (TException e) {
            pool.returnBrokenResource(client);
            throw new ApiClientException(e);
        }
    }

    private interface Function<R> {
        R apply() throws TException;
    }
}