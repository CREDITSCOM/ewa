package com.credits.service.contract;

import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import com.credits.serialise.Serializer;
import com.credits.service.contract.method.MethodParamValueRecognizer;
import com.credits.service.contract.method.MethodParamValueRecognizerFactory;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.service.usercode.UserCodeStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContractExecutorServiceImpl implements ContractExecutorService {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServiceImpl.class);

    @Resource
    private LevelDbInteractionService dbInteractionService;

    @Resource
    private UserCodeStorageService storageService;

    @PostConstruct
    private void setUp() {
        try {
            Class<?> contract = Class.forName("SmartContract");
            Field interactionService = contract.getDeclaredField("service");
            interactionService.setAccessible(true);
            interactionService.set(null, dbInteractionService);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot load smart contract's super class", e);
        }
    }

    public void execute(String address, String specialProperty) throws ContractExecutorException {
        File serFile = Serializer.getSerFile(address);
        if (serFile.exists()) {
            throw new ContractExecutorException("Contract " + address + " has been already stored.");
        }

        Class<?> clazz;
        try {
            clazz = storageService.load(address);
        } catch (ClassLoadException e) {
            throw new ContractExecutorException("Cannot execute the contract: " + address + ". Reason: "
                + e.getMessage(), e);
        }

        Object instance;
        try {
            instance = clazz.newInstance();
            Field specPropField = clazz.getSuperclass().getDeclaredField("specialProperty");
            specPropField.setAccessible(true);
            specPropField.set(instance, specialProperty);

            Field totalField = clazz.getSuperclass().getDeclaredField("total");
            totalField.setAccessible(true);
            double total = totalField.getDouble(instance);

            if (total != 0) {
                Method sendTransactionSystem = clazz.getSuperclass().getDeclaredMethod("sendTransactionSystem", String.class, Double.class, String.class);
                sendTransactionSystem.setAccessible(true);
                sendTransactionSystem.invoke(instance, "publc_key_wich_i_dont_know_where_to_get", total, address);
            }

            logger.info("Contract {} has been successfully saved.", address);
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            throw new ContractExecutorException("Cannot execute the contract: " + address + ". Reason: " + e.getMessage(), e);
        }

        Serializer.serialize(serFile, instance);
    }

    public void execute(String address, String methodName, String[] params) throws ContractExecutorException {
        Class<?> clazz;
        try {
            clazz = storageService.load(address);
        } catch (ClassLoadException e) {
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + e.getMessage(), e);
        }

        Object instance;
        File serFile = Serializer.getSerFile(address);
        if (serFile.exists()) {
            ClassLoader customLoader = clazz.getClassLoader();
            instance = Serializer.deserialize(serFile, customLoader);
        } else {
            throw new ContractExecutorException("Smart contract instance doesn't exist.");
        }

        List<Method> methods = Arrays.stream(clazz.getMethods()).filter(method -> {
            if (params == null || params.length == 0) {
                return method.getName().equals(methodName) && method.getParameterCount() == 0;
            } else {
                return method.getName().equals(methodName) && method.getParameterCount() == params.length;
            }
        }).collect(Collectors.toList());

        Method targetMethod = null;
        Object[] argValues = null;
        if (methods.isEmpty()) {
            throw new ContractExecutorException("Cannot execute the contract: " + address +
                ". Reason: Cannot find a method by name and parameters specified");
        } else {
            for (Method method : methods) {
                try {
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length > 0) {
                        argValues = castValues(types, params);
                    }
                } catch (ClassCastException e) {
                    continue;
                } catch (ContractExecutorException e) {
                    throw new ContractExecutorException("Cannot execute the contract: " + address, e);
                }
                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            throw new ContractExecutorException("Cannot execute the contract: " + address +
                ". Reason: Cannot cast parameters to the method found by name: " + methodName);
        }

        //Invoking target method
        try {
            targetMethod.invoke(instance, argValues);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + e.getMessage(), e);
        }

        //Serializing object
        Serializer.serialize(serFile, instance);
    }

    private Object[] castValues(Class<?>[] types, String[] params) throws ContractExecutorException {
        if (params == null || params.length != types.length) {
            throw new ContractExecutorException("Not enough arguments passed");
        }

        Object[] retVal = new Object[types.length];
        int i = 0;
        String param;
        Class<?> componentType;
        for (Class<?> type : types) {
            param = params[i];
            componentType = type;
            if (type.isArray()) {
                if (types.length > 1) {
                    throw new ContractExecutorException("Having array with other parameter types is not supported");
                }
                componentType = type.getComponentType();
            }

            MethodParamValueRecognizer recognizer = MethodParamValueRecognizerFactory.get(param);
            try {
                retVal[i] = recognizer.castValue(componentType);
            } catch (ContractExecutorException e) {
                throw new ContractExecutorException(
                    "Failed when casting the parameter given with the number: " + (i + 1), e);
            }
            i++;
        }

        return retVal;
    }
}
