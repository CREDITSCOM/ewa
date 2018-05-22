package com.credits.service.contract;

import com.credits.Const;
import com.credits.common.utils.Converter;
import com.credits.common.utils.Utils;
import com.credits.crypto.Blake2S;
import com.credits.crypto.Ed25519;
import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.thrift.SmartContract;
import com.credits.secure.Sandbox;
import com.credits.serialise.Serializer;
import com.credits.service.contract.method.MethodParamValueRecognizer;
import com.credits.service.contract.method.MethodParamValueRecognizerFactory;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.service.usercode.UserCodeStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.math.BigDecimal;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.Permissions;
import java.security.PrivateKey;
import java.security.SecurityPermission;
import java.util.Arrays;
import java.util.List;
import java.util.PropertyPermission;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

@Component
public class ContractExecutorServiceImpl implements ContractExecutorService {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServiceImpl.class);

    @Value("${api.server.host}")
    private String apiServerHost;

    @Value("${api.server.port}")
    private Integer apiServerPort;

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
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + e.getMessage(), e);
        }

        Object instance;
        try {
            File specPropertySerFile = Serializer.getPropertySerFile(address);
            Serializer.serialize(specPropertySerFile, specialProperty);

            instance = clazz.newInstance();
            Sandbox.confine(instance.getClass(), new Permissions());
            clazz.getMethod("initialize").invoke(instance);

            Field totalField = clazz.getSuperclass().getDeclaredField("total");
            totalField.setAccessible(true);
            String strTotalField = String.valueOf(totalField.getDouble(instance));
            BigDecimal total = new BigDecimal(strTotalField);

            if (total.doubleValue() != 0) {
                byte[] innerIdhashBytes = Blake2S.generateHash(4);
                String innerId = Converter.bytesToHex(innerIdhashBytes);

                byte[] privateKeyByteArrSystem = Converter.decodeFromBASE58(Const.SYS_TRAN_PRIVATE_KEY);
                PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArrSystem);

                byte[] privateKeyByteArr = Converter.decodeFromBASE58(specialProperty);
                byte[] publicKeyByteArr = Utils.parseSubarray(privateKeyByteArr, 32, 32);
                String target = Converter.encodeToBASE58(publicKeyByteArr);

                BigDecimal balance =
                    dbInteractionService.getBalance(Const.SYS_TRAN_PUBLIC_KEY, Const.SYS_TRAN_CURRENCY);
                String signatureBASE58 =
                    Ed25519.generateSignOfTransaction(innerId, Const.SYS_TRAN_PUBLIC_KEY, target, total, balance,
                        Const.SYS_TRAN_CURRENCY, privateKey);

                dbInteractionService.transactionFlow(innerId, Const.SYS_TRAN_PUBLIC_KEY, target, total, balance,
                    Const.SYS_TRAN_CURRENCY, signatureBASE58);
            }

            logger.info("Contract {} has been successfully saved.", address);
        } catch (Exception e) {
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + e.getMessage(), e);
        }

        Serializer.serialize(serFile, instance);
    }

    public void execute(String address, String methodName, String[] params) throws ContractExecutorException {
        Class<?> clazz;
        try {
            clazz = storageService.load(address);
        } catch (ClassLoadException e) {
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + getRootCauseMessage(e), e);
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
            Permissions permissions = new Permissions();
            //            if (isSmartContractBaseMethod(targetMethod)) {
            permissions.add(new SocketPermission(apiServerHost + ":" + apiServerPort, "connect,listen,resolve"));
            permissions.add(new SecurityPermission("getProperty.networkaddress.cache.ttl", "read"));
            permissions.add(new SecurityPermission("getProperty.networkaddress.cache.negative.ttl", "read"));
            permissions.add(new PropertyPermission("sun.net.inetaddr.ttl", "read"));
            permissions.add(new PropertyPermission("socksProxyHost", "read"));
            permissions.add(new PropertyPermission("java.net.useSystemProxies", "read"));
            permissions.add(new PropertyPermission("java.home", "read"));
            permissions.add(new NetPermission("getProxySelector"));
            permissions.add(new RuntimePermission("readFileDescriptor"));
            permissions.add(new RuntimePermission("writeFileDescriptor"));
            permissions.add(new RuntimePermission("accessDeclaredMembers"));
            permissions.add(new ReflectPermission("suppressAccessChecks"));
            try {
                Field f = ClassLoader.class.getDeclaredField("classes");
                f.setAccessible(true);
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                List<Class> classes = (List<Class>) f.get(classLoader);
                for (Class c: classes) {
                    if(c.getName().contains("hrift"))
                    System.out.println(c.getName());
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            Sandbox.confine(instance.getClass(), permissions);
            //            }
            targetMethod.invoke(instance, argValues);
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println(getStackTrace(e));
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + getRootCauseMessage(e));
        }

        //Serializing object
        Serializer.serialize(serFile, instance);
    }

    private boolean isSmartContractBaseMethod(Method method) {
        try {
            return method.equals(SmartContract.class.getMethod(method.getName()));
        } catch (NoSuchMethodException ignored) {
        }
        return false;
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
