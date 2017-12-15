package com.credits.service;

import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Component
public class ContractExecutorService {

    @Resource
    private StorageService storageService;

    public void execute(String address, String methodName, String[] params) throws ContractExecutorException {
        Class<?> clazz;
        try {
            clazz = storageService.load(address);
        } catch (ClassLoadException e) {
            throw new ContractExecutorException("Cannot execute the contract " + address + ". Reason: "
                + e.getMessage(), e);
        }

        Method method;
        try {
            method = clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new ContractExecutorException("Cannot execute the contract " + address + ". Reason: "
                + e.getMessage(), e);
        }

        if (method.isAccessible()) {
            throw new ContractExecutorException("Cannot execute the contract " + address
                + ". Reason: method is not accessible");
        }

        Object instance = null;
        if (!Modifier.isStatic(method.getModifiers())) {
            try {
                instance = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ContractExecutorException("Cannot execute the contract " + address + ". Reason: "
                    + e.getMessage(), e);
            }
        }

        if (method.getParameterCount() != params.length) {
            throw new ContractExecutorException("Cannot execute the contract " + address
                + ". Reason: wrong number of arguments, expected: " + method.getParameterCount()
                + ", actual: " + params.length);
        }

        //TODO: parse method arguments according to their java types and cast them to Object type
        Object[] args = null;
        try {
            method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ContractExecutorException("Cannot execute the contract " + address + ". Reason: "
                + e.getMessage(), e);
        }
    }

}
