package com.credits.service.contract;

import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import com.credits.service.usercode.UserCodeStorageService;
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
public class ContractExecutorServiceImpl implements ContractExecutorService {

    @Resource
    private UserCodeStorageService storageService;

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

    private Object[] castValues(Class<?>[] types, String[] params) throws ContractExecutorException {
        if (params == null || params.length != types.length) {
            throw new ContractExecutorException("Not enough arguments passed");
        }

        Object[] retVal = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];

            if (type.isArray()) {
                if (types.length > 1) {
                    throw new ContractExecutorException("Having array with other parameter types is not supported");
                } else {
                    retVal[i] = createObjectAsArray(params, type.getComponentType());
                }
            } else {
                String param = params[i];
                try {
                    retVal[i] = castValue(param, type);
                } catch (ContractExecutorException e) {
                    throw new ContractExecutorException("Failed when casting the parameter given with the number: "
                        + (i + 1), e);
                }
            }
        }
        return retVal;
    }

    private Object castValue(String param, Class<?> type) throws ContractExecutorException {
        Object retVal = null;
        if (param == null || isNullLiteral(param)) {
            retVal = type.cast(null);
        } else if (isNumberLiteralOrCastableNumber(param)) {
            Number numParam;
            if (isCastedNumber(param)) {
                numParam = createCastedNumber(param);
                if (numParam == null) {
                    throw new ContractExecutorException("Unknown primitive type of the parameter");
                }
            } else {
                if (isDoubleLiteral(param)) {
                    numParam = NumberUtils.createDouble(param);
                } else {
                    numParam = NumberUtils.createNumber(param);
                }
            }

            if (Long.class.equals(numParam.getClass())) {
                retVal = type.cast(numParam.longValue());
            } else if (Integer.class.equals(numParam.getClass())) {
                retVal = type.cast(numParam.intValue());
            } else if (Double.class.equals(numParam.getClass())) {
                retVal = type.cast(numParam.doubleValue());
            } else if (Float.class.equals(numParam.getClass())) {
                retVal = type.cast(numParam.floatValue());
            } else if (Short.class.equals(numParam.getClass())) {
                retVal = type.cast(numParam.shortValue());
            } else if (Byte.class.equals(numParam.getClass())) {
                retVal = type.cast(numParam.byteValue());
            }

        } else if (isStringLiteral(param)) {
            param = createFromStringOrCharLiteral(param, '"');
            if (param == null) {
                throw new ContractExecutorException("Illegal string literal for the parameter");
            }
            retVal = type.cast(param);
        } else if (isCharLiteral(param)) {
            param = createFromStringOrCharLiteral(param, '\'');
            if (param == null) {
                throw new ContractExecutorException("Illegal char literal for the parameter");
            }
            retVal = type.cast(param.charAt(0));
        } else if (isBooleanLiteral(param)) {
            retVal = BooleanUtils.toBoolean(param);
        } else {
            throw new ContractExecutorException("Unknown literal for the parameter");
        }

        return retVal;
    }

    // Utility methods for working with literals
    private boolean isNumberLiteralOrCastableNumber(String str) {
        return NumberUtils.isCreatable(str) || isCastedNumber(str);
    }

    private boolean isCastedNumber(String str) {
        String pattern = "\\((byte|short|int|long|float|double)\\)\\s*.+";
        return str.matches(pattern);
    }

    private boolean isDoubleLiteral(String str) {
        String pattern1 = "\\d*\\.\\d+(([eE])\\d+)*d?";
        String pattern2 = "\\d+\\.\\d*(([eE])\\d+)*d?";
        return str.matches(pattern1) || str.matches(pattern2);
    }

    private boolean isNullLiteral(String str) {
        String pattern = "null";
        return str.matches(pattern);
    }

    private boolean isStringLiteral(String str) {
        char literalMarker = '"';
        int firstQuotePos = str.indexOf(literalMarker);
        int lastQuotePos = str.lastIndexOf(literalMarker);

        return firstQuotePos != -1 && lastQuotePos != -1;
    }

    private boolean isCharLiteral(String str) {
        char literalMarker = '\'';
        int firstQuotePos = str.indexOf(literalMarker);
        int lastQuotePos = str.lastIndexOf(literalMarker);

        return firstQuotePos != -1 && lastQuotePos != -1;
    }

    private boolean isBooleanLiteral(String str) {
        return Boolean.toString(true).equals(str) || Boolean.toString(false).equals(str);
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

    private String createFromStringOrCharLiteral(String param, char literalMarker) {
        int firstQuotePos = param.indexOf(literalMarker);
        int lastQuotePos = param.lastIndexOf(literalMarker);
        String retVal = null;
        if (firstQuotePos > -1 && lastQuotePos > firstQuotePos) {
            retVal = param.substring(firstQuotePos + 1, lastQuotePos);
        }
        return retVal;
    }

    private Object createObjectAsArray(String[] params, Class<?> typeOfArray) throws ContractExecutorException {
        Object[] retVal = new Object[params.length];
        int i = 0;
        for (String param : params) {
            retVal[i++] = castValue(param, typeOfArray);
        }
        return retVal;
    }
}
