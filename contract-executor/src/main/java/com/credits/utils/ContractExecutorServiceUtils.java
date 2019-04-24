package com.credits.utils;

import com.credits.general.pojo.AnnotationData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.variant.VariantConverter;
import com.credits.pojo.MethodData;
import com.credits.scapi.annotations.ContractAddress;
import com.credits.scapi.annotations.ContractMethod;
import com.credits.scapi.annotations.UsingContract;
import com.credits.scapi.annotations.UsingContracts;
import exception.ContractExecutorException;
import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.credits.general.pojo.ApiResponseCode.FAILURE;
import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static com.credits.general.util.Utils.rethrowUnchecked;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList;


public class ContractExecutorServiceUtils {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServiceUtils.class);

    public final static APIResponse SUCCESS_API_RESPONSE = new APIResponse(SUCCESS.code, "success");

    public static MethodData getMethodArgumentsValuesByNameAndParams(
        Class<?> contractClass,
        String methodName,
        Variant[] params,
        ClassLoader classLoader) throws ClassNotFoundException {

        if (params == null) {
            throw new ContractExecutorException("Cannot find method params == null");
        }

        Class<?>[] argTypes = getArgTypes(params, classLoader);
        Method method = MethodUtils.getMatchingAccessibleMethod(contractClass, methodName, argTypes);
        Object[] argValues = argTypes != null ? castValues(argTypes, params, classLoader) : null;
        if (method != null) {
            return new MethodData(method, argTypes, argValues);
        } else {
            throw new ContractExecutorException("Cannot find a method by name and parameters specified");
        }
    }

    public static APIResponse failureApiResponse(Throwable e) {
        return new APIResponse(FAILURE.code, getRootCauseMessage(e));
    }

    public static Object[] castValues(Class<?>[] types, Variant[] params, ClassLoader classLoader) throws ContractExecutorException {
        if (params == null || params.length != types.length) {
            throw new ContractExecutorException("not enough arguments passed");
        }
        Object[] retVal = new Object[types.length];
        int i = 0;
        Variant param;
        for (Class<?> type : types) {
            param = params[i];
            if (type.isArray()) {
                if (types.length > 1) {
                    throw new ContractExecutorException("having array with other parameter types is not supported");
                }
            }

            retVal[i] = VariantConverter.toObject(param, classLoader);
            logger.debug("casted param[{}] = {}", i, retVal[i]);
            i++;
        }
        return retVal;
    }

    private static Class<?>[] getArgTypes(Variant[] params, ClassLoader classLoader) throws ClassNotFoundException {
        Class<?>[] classes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            Variant variant = params[i];
            switch (variant.getSetField()) {
                case V_OBJECT:
                    classes[i] = Class.forName(variant.getV_object().nameClass, false, classLoader);
                    break;
                case V_VOID:
                    classes[i] = Void.TYPE;
                    break;
                case V_NULL:
                    classes[i] = Class.forName(variant.getV_null(), false, classLoader);
                    break;
                default:
                    classes[i] = variant.getFieldValue().getClass();
                    break;
            }
        }
        return classes;
    }

    public static void initializeField(String fieldName, Object value, Class<?> clazz, Object instance) {
        try {
            Field field = clazz.getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot initialize \"{}\" field. Reason:{}", fieldName, getRootCauseMessage(e));
        }
    }

    public static void initializeSmartContractField(String fieldName, Object value, Class<?> clazz, Object instance) {
        getAllFieldsList(clazz).stream()
            .filter(field -> field.getName().equals(fieldName))
            .findAny()
            .ifPresent(field -> rethrowUnchecked(() -> {
                field.setAccessible(true);
                field.set(instance, value);
            }));
    }
    public static List<AnnotationData> readAnnotation(Annotation annotation) {
        if (annotation instanceof UsingContract) {
            UsingContract usingContract = ((UsingContract) annotation);
            return singletonList(new AnnotationData(
                UsingContract.class.getName(),
                Map.of("address", usingContract.address(), "method", usingContract.method())));

        } else if (annotation instanceof UsingContracts) {
            return Arrays.stream(((UsingContracts) annotation).value())
                .flatMap(a -> readAnnotation(a).stream())
                .collect(Collectors.toList());
        } else if (annotation instanceof ContractAddress) {
            ContractAddress contractAddress = ((ContractAddress) annotation);
            return singletonList(new AnnotationData(
                ContractAddress.class.getName(),
                Map.of("id", Integer.toString(contractAddress.id()))));

        } else if (annotation instanceof ContractMethod) {
            ContractMethod contractMethod = ((ContractMethod) annotation);
            return singletonList(new AnnotationData(
                ContractMethod.class.getName(),
                Map.of("id", Integer.toString(contractMethod.id()))));

        } else {
            return singletonList(new AnnotationData(annotation.annotationType().getName(), emptyMap()));
        }
    }
}
