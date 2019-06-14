package com.credits.utils;

import com.credits.general.pojo.AnnotationData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.variant.VariantConverter;
import com.credits.pojo.MethodData;
import com.credits.scapi.annotations.ContractAddress;
import com.credits.scapi.annotations.ContractMethod;
import com.credits.scapi.annotations.UsingContract;
import com.credits.scapi.annotations.UsingContracts;
import exception.ContractExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.credits.general.pojo.ApiResponseCode.FAILURE;
import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static com.credits.general.util.Utils.rethrowUnchecked;
import static com.credits.thrift.utils.ContractExecutorUtils.OBJECT_METHODS;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.beanutils.MethodUtils.getMatchingAccessibleMethod;
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
        Method method = getMatchingAccessibleMethod(contractClass, methodName, argTypes);
        if (method != null) {
            Object[] argValues = castValues(argTypes, params, classLoader);
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
        Variant param;
        for (int i = 0, typesLength = types.length; i < typesLength; i++) {
            param = params[i];
            retVal[i] = VariantConverter.toObject(param, classLoader);
            logger.debug("casted param[{}] = {}", i, retVal[i]);
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
                case V_BYTE_ARRAY:
                    classes[i] = byte[].class;
                    break;
                case V_BIG_DECIMAL:
                    classes[i] = BigDecimal.class;
                    break;
                default:
                    classes[i] = variant.getFieldValue().getClass();
                    break;
            }
        }
        return classes;
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

    public static List<MethodDescriptionData> createMethodDescriptionListByClass(Class<?> contractClass) {
        return stream(contractClass.getMethods())
                .filter(m -> !OBJECT_METHODS.contains(m.getName()))
                .map(method -> {
                    var annotations = toAnnotationDataList(method.getAnnotations());
                    var parameters = stream(method.getParameters())
                            .map(p -> new MethodArgumentData(p.getType().getTypeName(), p.getName(), toAnnotationDataList(p.getAnnotations())))
                            .collect(toList());
                    return toMethodDescriptionData(method, annotations, parameters);
                })
                .collect(toList());
    }

    private static MethodDescriptionData toMethodDescriptionData(Method m, List<AnnotationData> annotations, List<MethodArgumentData> parameters) {
        return new MethodDescriptionData(m.getGenericReturnType().getTypeName(), m.getName(), parameters, annotations);
    }

    private static List<AnnotationData> toAnnotationDataList(Annotation[] annotations) {
        return stream(annotations).flatMap(a -> readAnnotation(a).stream()).collect(toList());
    }

    private static List<AnnotationData> readAnnotation(Annotation annotation) {
        if (annotation instanceof UsingContract) {
            var usingContract = ((UsingContract) annotation);
            return singletonList(new AnnotationData(
                    UsingContract.class.getName(),
                    Map.of("address", usingContract.address(), "method", usingContract.method())));

        } else if (annotation instanceof UsingContracts) {
            return stream(((UsingContracts) annotation).value())
                    .flatMap(a -> readAnnotation(a).stream())
                    .collect(toList());
        } else if (annotation instanceof ContractAddress) {
            return singletonList(new AnnotationData(
                    ContractAddress.class.getName(),
                    Map.of("id", Integer.toString(((ContractAddress) annotation).id()))));

        } else if (annotation instanceof ContractMethod) {
            return singletonList(new AnnotationData(
                    ContractMethod.class.getName(),
                    Map.of("id", Integer.toString(((ContractMethod) annotation).id()))));
        } else {
            return singletonList(new AnnotationData(annotation.annotationType().getName(), emptyMap()));
        }
    }
}
