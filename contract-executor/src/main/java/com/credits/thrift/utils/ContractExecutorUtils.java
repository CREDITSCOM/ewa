package com.credits.thrift.utils;

import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.thrift.generated.Variant;
import exception.ContractExecutorException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.credits.ApplicationProperties.APP_VERSION;
import static com.credits.general.util.variant.VariantConverter.toVariant;
import static com.credits.service.BackwardCompatibilityService.allVersionsBasicStandardClass;
import static com.credits.utils.Constants.*;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public class ContractExecutorUtils {


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
                Variant variant;

                Object fieldValue;
                try {
                    fieldValue = field.get(object);
                } catch (IllegalAccessException e) {
                    throw new ContractExecutorException(
                            "Cannot getObject access to field: " + name + ". Reason: " + getRootCauseMessage(e), e);
                }

                variant = toVariant(field.getType().getTypeName(), fieldValue);
                contractVariables.put(name, variant);
            }
        }
        return contractVariables;
    }

    public static List<Class<?>> compileSmartContractByteCode(
            List<ByteCodeObjectData> smartContractByteCodeData,
            ByteCodeContractClassLoader byteCodeContractClassLoader) {

        List<Class<?>> compiledClasses = new ArrayList<>(smartContractByteCodeData.size());
        for (ByteCodeObjectData compilationUnit : smartContractByteCodeData) {
            compiledClasses.add(byteCodeContractClassLoader.loadClass(compilationUnit.getName(), compilationUnit.getByteCode()));
        }
        return compiledClasses;
    }

    public static void validateVersion(short version) {
        if (version != APP_VERSION) {
            throw new IllegalArgumentException(String.format("Invalid version %s, %s expected", version, APP_VERSION));
        }
    }

    public static void checkThatIsNotCreditsToken(Class<?> contractClass, Object instance) {
        stream(contractClass.getInterfaces())
                .filter(allVersionsBasicStandardClass::contains)
                .findAny()
                .ifPresent(ignore -> stream(contractClass.getMethods())
                        .filter(m -> m.getName().equals("getName") || m.getName().equals("getSymbol") && m.getParameters().length == 0)
                        .forEach(method -> {
                            try {
                                String methodName = method.getName();
                                if (methodName.equals("getName")) {
                                    if (((String) method.invoke(instance)).equalsIgnoreCase(CREDITS_TOKEN_NAME)) {
                                        throw new ContractExecutorException(TOKEN_NAME_RESERVED_ERROR);
                                    }
                                } else if (methodName.equals("getSymbol")) {
                                    if (((String) method.invoke(instance)).equalsIgnoreCase(CREDITS_TOKEN_SYMBOL)) {
                                        throw new ContractExecutorException(TOKEN_NAME_RESERVED_ERROR);
                                    }
                                }
                            } catch (ContractExecutorException e) {
                                rethrow(e);
                            } catch (Throwable ignored) {
                            }
                        }));
    }
}
