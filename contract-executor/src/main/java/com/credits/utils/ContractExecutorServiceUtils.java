package com.credits.utils;

import com.credits.exception.ContractExecutorException;
import com.credits.general.pojo.AnnotationData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.variant.VariantConverter;
import com.credits.pojo.MethodData;
import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.credits.general.pojo.ApiResponseCode.FAILURE;
import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;


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
                    classes[i] = Class.forName(variant.getV_object().className, false, classLoader);
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
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot initialize \"{}\" field. Reason:{}", fieldName, getRootCauseMessage(e));
        }
    }

    public static List<AnnotationData> parseAnnotationData(String annotation) {
        List<AnnotationData> annotationDataList = new ArrayList<>();
        if (annotation.contains("@")) {
            String[] splitArray = annotation.split("@");
            if (splitArray.length == 2) {
                annotationDataList.addAll(parseAnnotation(splitArray[1]));
            } else if (splitArray.length > 2) {
                for (int i = 2; i < splitArray.length; i++) {
                    annotationDataList.addAll(parseAnnotation(splitArray[i]));
                }
            }
        }
        return annotationDataList;
    }

    private static List<AnnotationData> parseAnnotation(String annotation) {
        List<AnnotationData> annotationDataList = new ArrayList<>();
        Map<String, String> annotationArguments = new HashMap<>();
        int paramIndex = annotation.indexOf("(");
        String name = annotation.substring(0, paramIndex);
        annotation = annotation.substring(paramIndex + 1, annotation.length() - 1);
        if (annotation.length() > 0) {
            String regex = "([A-Za-z0-9]+) *= *([A-Za-z0-9]+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(annotation);
            while (matcher.find()) {
                annotationArguments.put(matcher.group(1), matcher.group(2));
            }
        }
        AnnotationData annotationData = new AnnotationData(name, annotationArguments);
        annotationDataList.add(annotationData);
        return annotationDataList;
    }

}
