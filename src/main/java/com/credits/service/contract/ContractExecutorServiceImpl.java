package com.credits.service.contract;

import com.credits.classload.RuntimeDependencyInjector;
import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import com.credits.service.contract.method.MethodParamValueRecognizer;
import com.credits.service.contract.method.MethodParamValueRecognizerFactory;
import com.credits.service.usercode.UserCodeStorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ContractExecutorServiceImpl implements ContractExecutorService {

    private final static String SER_EXT = "out";
    private final static String SER_SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "credits";

    @Resource
    private UserCodeStorageService storageService;

    @Resource
    private RuntimeDependencyInjector dependencyInjector;

    public void execute(String address, String methodName, String[] params) throws ContractExecutorException {
        Class<?> clazz;
        try {
            clazz = storageService.load(address);
        } catch (ClassLoadException e) {
            throw new ContractExecutorException("Cannot execute the contract: " + address + ". Reason: "
                + e.getMessage(), e);
        }

        List<Method> methods = Arrays.stream(clazz.getMethods())
            .filter(method -> {
                if (params == null || params.length == 0) {
                    return method.getName().equals(methodName) && method.getParameterCount() == 0;
                } else {
                    return method.getName().equals(methodName) && method.getParameterCount() == params.length;
                }
            })
            .collect(Collectors.toList());

        Method targetMethod = null;
        Object[] argValues = null;
        if (methods.isEmpty()) {
            throw new ContractExecutorException("Cannot execute the contract: " + address
                + ". Reason: Cannot find a method by name and parameters specified");
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
            throw new ContractExecutorException("Cannot execute the contract: " + address
                + ". Reason: Cannot cast parameters to the method found by name: " + methodName);
        }

        Object instance = null;
        if (!Modifier.isStatic(targetMethod.getModifiers())) {
            try {
                instance = clazz.newInstance();
                dependencyInjector.bind(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ContractExecutorException("Cannot execute the contract: " + address + ". Reason: "
                    + e.getMessage(), e);
            }
        }

        //Injecting deserialized fields into class if present
        File serFile = getSerFile(address);

        if (serFile.exists()) {
            Map<String, Object> deserFields = deserialize(serFile);
            Field[] fieldsFromClass = clazz.getDeclaredFields();
            if (fieldsFromClass != null && fieldsFromClass.length != 0) {
                for (Field field : fieldsFromClass) {
                    try {
                        if (Modifier.isStatic(targetMethod.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                            field.setAccessible(true);
                            field.set(instance, deserFields.get(field.getName()));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        //Invoking target method
        try {
            targetMethod.invoke(instance, argValues);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ContractExecutorException("Cannot execute the contract: " + address + ". Reason: "
                + e.getMessage(), e);
        }

        //Serializing class fields
        HashMap<String, Object> serFields = new HashMap<>();
        Field[] fieldsForSer = clazz.getDeclaredFields();
        if (fieldsForSer != null && fieldsForSer.length != 0) {
            for (Field field : fieldsForSer) {
                try {
                    if (Modifier.isStatic(targetMethod.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        serFields.put(field.getName(), field.get(instance));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            serialize(serFields, serFile);
        }
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

    private void serialize(Map<String, Object> serFields, File serFile) {
        try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(serFile))){
            ous.writeObject(serFields);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> deserialize(File serFile) {
        Map<String, Object> deserFields = null;
        try (ObjectInputStream ous = new ObjectInputStream(new FileInputStream(serFile))){
            deserFields = (Map<String, Object>) ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return deserFields;
    }

    private File getSerFile(String address) {
        File sourcePath = new File(SER_SOURCE_FOLDER_PATH + File.separator + address);
        String fileName = sourcePath.listFiles()[0].getName();
        String serFileName = FilenameUtils.getBaseName(fileName) + "." + SER_EXT;
        return new File(SER_SOURCE_FOLDER_PATH + File.separator + address +
                                File.separator + serFileName);
    }
}
