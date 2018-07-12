package com.credits.thrift.utils;

import com.credits.exception.ContractExecutorException;
import com.credits.thrift.DeployReturnValue;
import com.credits.thrift.generated.Variant;

import java.util.HashMap;
import java.util.Map;

import static com.credits.serialise.Serializer.serialize;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class ContractUtils {

    public static Map<String, Variant> getContractVariables(Object object) {
        Map<String, Variant> contractVariables = new HashMap<>();

        return contractVariables;
    }

    public static DeployReturnValue deploy(Class<?> clazz, String address) throws ContractExecutorException {
        try {
            Object instance = clazz.newInstance();
            DeployReturnValue returnValue =
                new DeployReturnValue(serialize(address, instance), ContractUtils.getContractVariables(instance));
            return returnValue;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ContractExecutorException(
                "Cannot create new instance of the contract: " + address + ". Reason: " + getRootCauseMessage(e), e);
        }
    }
}
