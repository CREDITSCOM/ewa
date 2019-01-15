package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeMultipleResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.GetContractMethodsResult;
import com.credits.client.executor.thrift.generated.GetContractVariablesResult;
import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.ThriftClientPool;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.variant.VariantConverter;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

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
    public ExecuteByteCodeResult executeByteCode(byte[] address, List<ByteCodeObject> byteCodeObjects, byte[] contractState, String method, List<VariantData> params, long executionTime) throws ContractExecutorClientException {
        Client client = pool.getResource();
        List<Variant> variantList = params.stream().map(variantData -> {
            return VariantConverter.variantDataToVariant(variantData);
        }).collect(Collectors.toList());
        return callThrift(client, () -> client.executeByteCode(ByteBuffer.wrap(address), byteCodeObjects, ByteBuffer.wrap(contractState), method,
                variantList,
                executionTime));
    }

    @Override
    public ExecuteByteCodeMultipleResult executeByteCodeMultiple(byte[] address, List<ByteCodeObject> byteCodeObjects, byte[] contractState, String method, List<List<Variant>> params, long executionTime) {
        Client client = pool.getResource();
        return callThrift(client, () -> client.executeByteCodeMultiple(ByteBuffer.wrap(address), byteCodeObjects, ByteBuffer.wrap(contractState), method, params, executionTime));
    }

    @Override
    public GetContractMethodsResult getContractMethods(List<ByteCodeObject> byteCodeObjects) throws ContractExecutorClientException {
        Client client = pool.getResource();
        return callThrift(client, () -> client.getContractMethods(byteCodeObjects));
    }

    @Override
    public GetContractVariablesResult getContractVariables(List<ByteCodeObject> byteCodeObjects, byte[] contractState) {
        Client client = pool.getResource();
        return callThrift(client, () -> client.getContractVariables(byteCodeObjects,ByteBuffer.wrap(contractState)));
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
