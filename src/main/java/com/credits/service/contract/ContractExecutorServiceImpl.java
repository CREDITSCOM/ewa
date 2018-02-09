package com.credits.service.contract;

import com.credits.classload.RuntimeDependencyInjector;
import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import com.credits.serialise.SupportedSerialisationType;
import com.credits.service.contract.method.MethodParamValueRecognizer;
import com.credits.service.contract.method.MethodParamValueRecognizerFactory;
import com.credits.serialise.Serializer;
import com.credits.service.usercode.UserCodeStorageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContractExecutorServiceImpl implements ContractExecutorService {

    @Resource
    private UserCodeStorageService storageService;

    @Resource
    private RuntimeDependencyInjector dependencyInjector;

    public void execute(String address) throws ContractExecutorException {
        Class<?> clazz;
        try {
            clazz = storageService.load(address);
        } catch (ClassLoadException e) {
            throw new ContractExecutorException("Cannot execute the contract: " + address + ". Reason: "
                + e.getMessage(), e);
        }

        Object instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ContractExecutorException("Cannot execute the contract: " + address + ". Reason: " + e.getMessage(), e);
        }

        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> {
                for (SupportedSerialisationType type : SupportedSerialisationType.values()) {
                    if (type.getClazz() == field.getType()) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());

        File serFile = Serializer.getSerFile(address);

        Serializer.serialize(serFile, instance, fields);
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
                throw new ContractExecutorException("Failed when casting the parameter given with the number: "
                    + (i + 1), e);
            }
            i++;
        }

        return retVal;
    }
}
