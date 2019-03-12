package com.credits.utils;

import com.credits.exception.ContractExecutorException;
import com.credits.general.pojo.AnnotationData;
import com.credits.general.thrift.generated.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            logger.debug(String.format("param[%s] = %s", i, retVal[i]));
            i++;
        }
        return retVal;
    }

    private static final String LINE = "-----------------------------------------------------";
    public static void writeLog(String s) {
        logger.info("\n{}\n-----------{}---------------\n{}\n", LINE, s, LINE);
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

    public static List<AnnotationData> parseAnnotationData(String annotation) {
        List<AnnotationData> annotationDataList = new ArrayList<>();
        if (annotation.contains("@")) {
            String[] splitArray = annotation.split("@");
            if(splitArray.length==2) {
                annotationDataList.addAll(parseAnnotation(splitArray[1]));
            } else if(splitArray.length>2){
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
