package com.credits.scapi.v0;

import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Function;
import com.credits.general.util.variant.VariantConverter;
import exception.ContractExecutorException;
import exception.ExternalSmartContractException;
import pojo.ExternalSmartContract;
import pojo.ReturnValue;
import pojo.SmartContractConstants;
import pojo.session.InvokeMethodSession;
import service.executor.ContractExecutorService;
import service.node.NodeApiExecInteractionService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.credits.general.pojo.ApiResponseCode.FAILURE;
import static com.credits.general.util.Utils.getClassType;
import static com.credits.general.util.variant.VariantConverter.toVariant;
import static java.lang.Long.MAX_VALUE;

public abstract class SmartContract implements Serializable {

    private static final long serialVersionUID = -7544650022718657167L;

    private static NodeApiExecInteractionService nodeApiService;
    private static ContractExecutorService contractExecutorService;
    private static ExecutorService cachedPool;
    private transient Map<String, ExternalSmartContract> usedContracts;

    protected final transient long accessId;
    protected final transient String initiator;
    protected final String contractAddress;

    public SmartContract() {
        SmartContractConstants contractConstants =
            SmartContractConstants.getSessionSmartContractConstants(Thread.currentThread().getId());
        initiator = contractConstants.initiator;
        accessId = contractConstants.accessId;
        contractAddress = contractConstants.contractAddress;
        usedContracts = contractConstants.usedContracts;
    }

    final protected void sendTransaction(String from, String to, double amount, double fee, byte... userData) {
        callService(() -> {
            nodeApiService.sendTransaction(accessId, from, to, amount, fee, userData);
            return null;
        });
    }

    final protected Object invokeExternalContract(String contractAddress, String method, Object... params) {
        ExternalSmartContract usedContract = usedContracts.containsKey(contractAddress)
            ? usedContracts.get(contractAddress)
            : new ExternalSmartContract(callService(() -> nodeApiService.getExternalSmartContractByteCode(accessId, contractAddress)));
        usedContracts.put(contractAddress, usedContract);

        Variant[][] variantParams = null;
        if (params != null) {
            variantParams = new Variant[1][params.length];
            for (int i = 0; i < params.length; i++) {
                final Object param = params[i];
                variantParams[0][i] = toVariant(getClassType(param), param);
            }
        }

        final ReturnValue returnValue = contractExecutorService.executeExternalSmartContract(
            new InvokeMethodSession(
                accessId,
                initiator,
                contractAddress,
                usedContract.getContractData().getByteCodeObjects(),
                usedContract.getContractData().getContractState(),
                method,
                variantParams,
                MAX_VALUE),
            usedContracts,
            (ByteCodeContractClassLoader) getClass().getClassLoader());

        final APIResponse returnStatus = returnValue.executeResults.get(0).status;
        if (returnStatus.code == FAILURE.code) {
            throw new ExternalSmartContractException(
                returnStatus.message + ". Contract address: " + contractAddress + ". Method: " + method + ". Args: " + Arrays.toString(params));
        }

        if (!usedContract.getContractData().isStateCanModify() && !Arrays.equals(
            usedContract.getContractData().getContractState(),
            returnValue.newContractState)) {
            throw new ContractExecutorException("smart contract \"" + contractAddress + "\" can't be modify");
        }
        usedContract.getContractData().setContractState(returnValue.newContractState);

        Variant result = returnValue.executeResults.get(0).result;
        return result == null ? toVariant("", Void.TYPE) : VariantConverter.toObject(result);
    }

    final protected byte[] getSeed() {
        return callService(() -> nodeApiService.getSeed(accessId));
    }

    final protected BigDecimal getBalance(String addressBase58) {
        return callService(() -> nodeApiService.getBalance(addressBase58));
    }

    private <R> R callService(Function<R> method) {
        try {
            return cachedPool.submit(method::apply).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
