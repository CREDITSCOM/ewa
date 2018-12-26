package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.thrift.generated.*;
import com.credits.general.thrift.ThriftClientPool;
import com.credits.general.thrift.generated.Variant;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.List;

import static com.credits.client.executor.thrift.generated.ContractExecutor.Client;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public class ContractExecutorThriftApiClient implements ContractExecutorThriftApi {

    private static ContractExecutorThriftApiClient instance;
    private final ThriftClientPool<Client> pool;

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
    public ExecuteByteCodeResult executeByteCode(byte[] address, byte[] bytecode, byte[] contractState, String method, List<Variant> params, long executionTime) throws ContractExecutorClientException {
        Client client = pool.getResource();
        return callThrift(client, () -> client.executeByteCode(ByteBuffer.wrap(address), ByteBuffer.wrap(bytecode), ByteBuffer.wrap(contractState), method, params, executionTime));
    }

    @Override
    public ExecuteByteCodeMultipleResult executeByteCodeMultiple(byte[] address, byte[] bytecode, byte[] contractState, String method, List<List<Variant>> params, long executionTime) {
        Client client = pool.getResource();
        return callThrift(client, () -> client.executeByteCodeMultiple(ByteBuffer.wrap(address), ByteBuffer.wrap(bytecode), ByteBuffer.wrap(contractState), method, params, executionTime));
    }

    @Override
    public GetContractMethodsResult getContractMethods(byte[] address) throws ContractExecutorClientException {
        Client client = pool.getResource();
        return callThrift(client, () -> client.getContractMethods(ByteBuffer.wrap(address)));
    }

    @Override
    public GetContractVariablesResult getContractVariables(byte[] byteCode, byte[] contractState) {
        Client client = pool.getResource();
        return callThrift(client, () -> client.getContractVariables(ByteBuffer.wrap(byteCode),ByteBuffer.wrap(contractState)));
    }

    @Override
    public CompileSourceCodeResult compileSourceCode(String sourceCode) {
        Client client = pool.getResource();
        return callThrift(client, () -> client.compileSourceCode(sourceCode));
    }

    private <R extends TBase> R callThrift(Client client, Function<R> method) throws ContractExecutorClientException {
        try {
            R res = method.apply();
            pool.returnResource(client);
            return res;
        } catch (TException e) {
            pool.returnBrokenResource(client);
            throw new ContractExecutorClientException(e);
        }
    }

    private interface Function<R> {
        R apply() throws TException;
    }
}
