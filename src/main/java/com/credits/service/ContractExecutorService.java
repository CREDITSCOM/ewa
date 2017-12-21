package com.credits.service;

import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContractExecutorService {

    @Resource
    private StorageService storageService;

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
                        return method.getName().equals(methodName);
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
                    argValues = castValues(method.getParameterTypes(), params);
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
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ContractExecutorException("Cannot execute the contract: " + address + ". Reason: "
                        + e.getMessage(), e);
            }
        }

        try {
            targetMethod.invoke(instance, argValues);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ContractExecutorException("Cannot execute the contract: " + address + ". Reason: "
                    + e.getMessage(), e);
        }
    }

    private Object[] castValues(Class<?>[] types, String[] params) throws ClassCastException, ContractExecutorException {
        Object[] retVal = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];

            String param;
            if (params == null || params.length <= i) {
                param = null;
            } else {
                param = params[i];
            }

            if (param == null) {
                retVal[i] = type.cast(null);
            } else if (isNumberLiteralOrCastable(param)) {
                Number numParam;
                if (isCastedNumber(param)) {
                    numParam = createCastedNumber(param);
                    if (numParam == null) {
                        throw new ContractExecutorException("Unknown primitive type of the parameter with number: "
                                + (i + 1));
                    }
                } else {
                    if (isDoubleLiteral(param)) {
                        numParam = NumberUtils.createDouble(param);
                    } else {
                        numParam = NumberUtils.createNumber(param);
                    }
                }

                if (Long.class.equals(numParam.getClass())) {
                    retVal[i] = type.cast(numParam.longValue());
                } else if (Integer.class.equals(numParam.getClass())) {
                    retVal[i] = type.cast(numParam.intValue());
                } else if (Double.class.equals(numParam.getClass())) {
                    retVal[i] = type.cast(numParam.doubleValue());
                } else if (Float.class.equals(numParam.getClass())) {
                    retVal[i] = type.cast(numParam.floatValue());
                } else if (Short.class.equals(numParam.getClass())) {
                    retVal[i] = type.cast(numParam.shortValue());
                } else if (Byte.class.equals(numParam.getClass())) {
                    retVal[i] = type.cast(numParam.byteValue());
                }

            } else if (String.class.equals(type)) {
                param = getStringOrCharLiteral(param, '"');
                if (param == null) {
                    throw new ContractExecutorException("Illegal string literal for the parameter with number: "
                            + (i + 1));
                }
                retVal[i] = param;
            } else if (Character.class.equals(type)) {
                param = getStringOrCharLiteral(param, '\'');
                if (param == null) {
                    throw new ContractExecutorException("Illegal char literal for the parameter with number: "
                            + (i + 1));
                }
                retVal[i] = param.charAt(0);
            } else if (Boolean.class.equals(type)) {
                retVal[i] = BooleanUtils.toBoolean(param);
            } else {
                throw new ContractExecutorException("Unknown type of the parameter with number: "
                        + (i + 1));
            }
        }
        return retVal;
    }


    // Utility methods for working with literals
    private boolean isNumberLiteralOrCastable(String str) {
        return NumberUtils.isCreatable(str) || isCastedNumber(str);
    }

    private boolean isCastedNumber(String str) {
        String pattern = "\\((byte|short|int|long|float|double)\\)\\s*.+";
        return str.matches(pattern);
    }

    private Number createCastedNumber(String param) {
        String pattern = "(\\((byte|short|int|long|float|double)\\)\\s*)(.+)";
        String number = param.replaceFirst(pattern, "$3").trim();
        String type = param.replaceFirst(pattern, "$2").trim();
        Number retVal = null;
        if (Long.TYPE.getName().equals(type)) {
            retVal = NumberUtils.createLong(number);
        } else if (Integer.TYPE.getName().equals(type)) {
            retVal = NumberUtils.createInteger(number);
        } else if (Double.TYPE.getName().equals(type)) {
            retVal = NumberUtils.createDouble(number);
        } else if (Float.TYPE.getName().equals(type)) {
            retVal = NumberUtils.createFloat(number);
        } else if (Short.TYPE.getName().equals(type)) {
            retVal = Short.valueOf(number);
        } else if (Byte.TYPE.getName().equals(type)) {
            retVal = Byte.valueOf(number);
        }
        return retVal;
    }

    private boolean isDoubleLiteral(String str) {
        String pattern1 = "\\d*\\.\\d+(([eE])\\d+)*d?";
        String pattern2 = "\\d+\\.\\d*(([eE])\\d+)*d?";
        return str.matches(pattern1) || str.matches(pattern2);
    }

    private String getStringOrCharLiteral(String param, char literalMarker) {
        int firstQuotePos = param.indexOf(literalMarker);
        int lastQuotePos = param.lastIndexOf(literalMarker);
        String retVal = null;
        if (firstQuotePos > -1 && lastQuotePos > firstQuotePos) {
            retVal = param.substring(firstQuotePos + 1, lastQuotePos);
        }
        return retVal;
    }
}
