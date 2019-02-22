package com.credits.utils;

import com.credits.general.pojo.AnnotationData;
import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generated.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.credits.general.util.variant.VariantConverter.variantToObject;


public class ContractExecutorServiceUtils {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServiceUtils.class);



    public static Object[] castValues(Class<?>[] types, Variant[] params) throws ContractExecutorException {
        if (params == null || params.length != types.length) {
            throw new ContractExecutorException("Not enough arguments passed");
        }
        Object[] retVal = new Object[types.length];
        int i = 0;
        Variant param;
        for (Class<?> type : types) {
            param = params[i];
            if (type.isArray()) {
                if (types.length > 1) {
                    throw new ContractExecutorException("Having array with other parameter types is not supported");
                }
            }

            retVal[i] = variantToObject(param);
            logger.info(String.format("param[%s] = %s", i, retVal[i]));
            i++;
        }
        return retVal;
    }

    public static Class[] getArgTypes(Variant[] params) {
        Class[] argTypes = new Class[params.length];

        for (int i = 0; i < params.length; i++) {
            argTypes[i] = params[i].getFieldValue().getClass();
        }
        return argTypes;
    }

    public static void initializeField(String fieldName, Object value, Class<?> clazz, Object instance) {
        try {
            Field field = clazz.getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot initialize \"{}\" field. Reason:{}", fieldName, e.getMessage());
        }
    }

    public static void initializeSmartContractField(String fieldName, Object value, Class<?> clazz, Object instance) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot initialize \"{}\" field. Reason:{}", fieldName, e.getMessage());
        }
    }

    public static AnnotationData parseAnnotationData(String annotation) {
        String name = null;
        Map<String, String> annotationArguments = new HashMap<>();
        new AnnotationData(name, annotationArguments);

        if (annotation.contains("@")) {
            int paramIndex = annotation.indexOf("(");
            name = annotation.substring(1, paramIndex);
            annotation = annotation.substring(paramIndex+1,annotation.length()-1);
            if(annotation.length()>0) {
                String regex = "([A-Za-z0-9]+) *= *([A-Za-z0-9]+)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(annotation);
                while (matcher.find()) {
                    annotationArguments.put(matcher.group(1),matcher.group(2));
                }
            }
        }
        return new AnnotationData(name,annotationArguments);
    }

}
