package com.credits.client.executor.service;

import com.credits.client.executor.exception.ExecutorClientException;
import com.credits.client.executor.thrift.APIResponse;
import com.credits.client.executor.thrift.GetContractMethodsResult;
import com.credits.general.thrift.ThriftClientPool;
import com.credits.general.thrift.generate.Variant;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.List;

import static com.credits.client.executor.thrift.ContractExecutor.Client;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public class ContractExecutorThriftApiClient implements ContractExecutorThriftApi {

    private static ContractExecutorThriftApiClient instance;
    private ThriftClientPool<Client> pool;

    private ContractExecutorThriftApiClient(String host, int port) {
        pool = new ThriftClientPool<>(Client::new, host, port);

    }

    public static ContractExecutorThriftApiClient getInstance(String host, Integer port) {
        ContractExecutorThriftApiClient localInstance = instance;
        if (localInstance == null) {
            synchronized (ContractExecutorThriftApiClient.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ContractExecutorThriftApiClient(host, port);
                }
            }
        }
        return localInstance;
    }

    @Override
    public APIResponse executeContractMethod(byte[] address, byte[] bytecode, byte[] objectState, String method, List<Variant> params) throws ExecutorClientException {
        Client client = pool.getResource();
        return callThrift(client, () -> client.executeByteCode(ByteBuffer.wrap(address), ByteBuffer.wrap(bytecode), ByteBuffer.wrap(objectState), method, params));
    }

    @Override
    public GetContractMethodsResult getContractMethods(byte[] address) throws ExecutorClientException {
        Client client = pool.getResource();
        return callThrift(client, () -> client.getContractMethods(ByteBuffer.wrap(address)));
    }

    private <R> R callThrift(Client client, Function<R> method) throws ExecutorClientException {
        try {
            R res = method.apply();
            pool.returnResource(client);
            return res;
        } catch (TException e) {
            pool.returnBrokenResource(client);
            throw new ExecutorClientException(e);
        }
    }

    private interface Function<R> {
        R apply() throws ExecutorClientException, TException;
    }
}
