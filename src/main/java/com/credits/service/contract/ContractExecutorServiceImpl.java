package com.credits.service.contract;

import com.credits.Const;
import com.credits.common.utils.Converter;
import com.credits.common.utils.Utils;
import com.credits.crypto.Blake2S;
import com.credits.crypto.Ed25519;
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
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
            File specPropertySerFile = Serializer.getPropertySerFile(address);
            Serializer.serialize(specPropertySerFile, specialProperty);

            instance = clazz.newInstance();

            Field totalField = clazz.getSuperclass().getDeclaredField("total");
            totalField.setAccessible(true);
            double total = totalField.getDouble(instance);

            if (total != 0) {
                byte[] hashBytes = Blake2S.generateHash(4);
                String hash = com.credits.leveldb.client.util.Converter.bytesToHex(hashBytes);

                String innerId = UUID.randomUUID().toString();

                byte[] privateKeyByteArrSystem = Converter.decodeFromBASE58(Const.SYS_TRAN_PRIVATE_KEY);
                PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArrSystem);

                byte[] privateKeyByteArr = Converter.decodeFromBASE58(specialProperty);
                byte[] publicKeyByteArr = Utils.parseSubarray(privateKeyByteArr, 32, 32);
                String target = Converter.encodeToBASE58(publicKeyByteArr);

                String signatureBASE58 =
                    Ed25519.generateSignOfTransaction(hash, innerId, Const.SYS_TRAN_PUBLIC_KEY, target, total, address, privateKey);

                dbInteractionService.transactionFlow(hash, innerId, Const.SYS_TRAN_PUBLIC_KEY, target, total, address, signatureBASE58);
            }

            logger.info("Contract {} has been successfully saved.", address);
        } catch (Exception e) {
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
