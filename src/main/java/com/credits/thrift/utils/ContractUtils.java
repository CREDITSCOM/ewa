package com.credits.thrift.utils;

import com.credits.exception.ContractExecutorException;
import com.credits.exception.UnsupportedTypeException;
import com.credits.thrift.DeployReturnValue;
import com.credits.thrift.generated.Variant;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.credits.serialise.Serializer.serialize;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class ContractUtils {

    public static Map<String, Variant> getContractVariables(Object object) throws ContractExecutorException {
        Map<String, Variant> contractVariables = null;
        Field[] fields = object.getClass().getFields();
        if (fields.length != 0) {
            contractVariables = new HashMap<>();
            for (Field field : fields) {
                String name = field.getName();
                Variant variant = null;

                Object fieldValue;
                try {
                    fieldValue = field.get(object);
                } catch (IllegalAccessException e) {
                    throw new ContractExecutorException(
                        "Cannot get access to field: " + name + ". Reason: " + getRootCauseMessage(e), e);
                }

                if (fieldValue != null) {
                    variant = new VariantMapper().apply(fieldValue)
                        .orElseThrow(() -> {
                            UnsupportedTypeException e = new UnsupportedTypeException(
                                "Unsupported type of the value {" + fieldValue.toString() + "}: " + fieldValue.getClass());
                            return new ContractExecutorException(
                                "Cannot execute the contract: " + ". Reason: " + getRootCauseMessage(e), e);
                        });
                }
                contractVariables.put(name, variant);
            }
        }
        return contractVariables;
    }

    public static DeployReturnValue deployAndGetContractVariables(Class<?> clazz, String address) throws ContractExecutorException {
        try {
            Object instance = clazz.newInstance();
            return new DeployReturnValue(serialize(address, instance), ContractUtils.getContractVariables(instance));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ContractExecutorException(
                "Cannot create new instance of the contract: " + address + ". Reason: " + getRootCauseMessage(e), e);
        }
    }
}
