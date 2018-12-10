package com.credits.thrift.utils;

import com.credits.exception.ContractExecutorException;
import com.credits.exception.UnsupportedTypeException;
import com.credits.general.thrift.generated.Variant;
import com.credits.thrift.DeployReturnValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static com.credits.serialize.Serializer.serialize;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class ContractUtils {

    /**
     * Returns null if class instance has no public variables.
     *
     * @param object an instance which field's values will be taken from
     * @return key-value mapping of the name of the field and a field value in Thrift custom type Variant
     * @throws ContractExecutorException
     */
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
                        "Cannot getObject access to field: " + name + ". Reason: " + getRootCauseMessage(e), e);
                }

                if (fieldValue != null) {
                    variant = mapObjectToVariant(fieldValue);
                }
                contractVariables.put(name, variant);
            }
        }
        return contractVariables;
    }

    public static DeployReturnValue deployAndGetContractVariables(Class<?> clazz, String address) throws ContractExecutorException {
        try {
            Constructor constructor = clazz.getConstructor(String.class);
            Object instance = constructor.newInstance(address);
            return new DeployReturnValue(serialize(instance),ContractUtils.getContractVariables(instance));
        } catch (IllegalAccessException | NoSuchMethodException ignored) {
        } catch (InstantiationException | InvocationTargetException e) {
            throw new ContractExecutorException(
                    "Cannot create new instance of the contract. Reason: " + getRootCauseMessage(e), e);
        }

        try {
            Object instance = clazz.newInstance();
            return new DeployReturnValue(serialize(instance), ContractUtils.getContractVariables(instance));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ContractExecutorException(
                "Cannot create new instance of the contract: " + address + ". Reason: " + getRootCauseMessage(e), e);
        }
    }

    public static Variant mapObjectToVariant(Object object) throws ContractExecutorException {
        return new VariantMapper().apply(object)
            .orElseThrow(() -> {
                UnsupportedTypeException e = new UnsupportedTypeException(
                    "Unsupported type of the value {" + object.toString() + "}: " + object.getClass());
                return new ContractExecutorException(
                    "Cannot execute the contract: " + ". Reason: " + getRootCauseMessage(e), e);
            });
    }
}
