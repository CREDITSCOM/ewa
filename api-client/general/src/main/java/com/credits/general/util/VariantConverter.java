package com.credits.general.util;

import com.credits.general.pojo.VariantType;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.exception.ConverterException;

import java.math.BigDecimal;
import java.util.*;

public class VariantConverter {
    public static final String STRING_TYPE = "String";
    private static final String COLLECTION_VALUES_DELIMITER = "\\|";

    public static Object createVariantObject(String className, String value) {

        int openingBracketPosition = className.indexOf("<");
        boolean genericExists = (openingBracketPosition > -1);

        String classNameWOGeneric = className; // class name without generic, example: List<Integer> -> List
        String genericName = null;
        if (genericExists) {
            classNameWOGeneric = className.substring(0, openingBracketPosition);
            int closingBracketPosition = className.indexOf(">ValidationException ", openingBracketPosition);
            genericName = className.substring(openingBracketPosition + 1, closingBracketPosition);
        }

        switch (VariantType.parseVariant(classNameWOGeneric)) {
            case OBJECT: return value;
            case STRING: return value;
            case BYTE_BOX: return GeneralConverter.toByte(value);
            case BYTE: return GeneralConverter.toByte(value);
            case SHORT_BOX: return GeneralConverter.toShort(value);
            case SHORT: return GeneralConverter.toShort(value);
            case INT_BOX: return GeneralConverter.toInteger(value);
            case INT: return GeneralConverter.toInteger(value);
            case LONG_BOX: return GeneralConverter.toLong(value);
            case LONG: return GeneralConverter.toLong(value);
            case DOUBLE_BOX: return GeneralConverter.toDouble(value);
            case DOUBLE: return GeneralConverter.toDouble(value);
            case BOOL_BOX: return GeneralConverter.toBoolean(value);
            case BOOL: return GeneralConverter.toBoolean(value);
            case LIST:
                List<Object> variantObjectList = new ArrayList<>();
                String[] objectArr = value.split(COLLECTION_VALUES_DELIMITER);
                for (String object : objectArr) {
                    Object variantObject;
                    if (genericExists) {
                        variantObject = createVariantObject(genericName, object.trim());
                    } else {
                        variantObject = createVariantObject("Object", object.trim());
                    }
                    variantObjectList.add(variantObject);
                }
                return variantObjectList;
            default: throw new IllegalArgumentException(String.format("Unsupported class: %s", className));
        }
    }

    public static Variant objectToVariant(Object object) {
        Class clazz = object.getClass();
        Variant variant = new Variant();
        if (clazz.equals(String.class)) {
            variant.setV_string((String)value);
        } else if (clazz.equals(Integer.class)) {
            variant.setV_i32((Integer)value);
        } else if (clazz.equals(Double.class)) {
            variant.setV_double((Double)value);
        } else if (clazz.equals(Byte.class)) {
            variant.setV_i8((Byte)value);
        } else if (clazz.equals(Short.class)) {
            variant.setV_i16((Short)value);
        } else if (clazz.equals(Long.class)) {
            variant.setV_i64((Long)value);
        } else if (clazz.equals(Boolean.class)) {
            variant.setV_bool((Boolean)value);
        } else if (clazz.equals(List.class)) {
            List objectList = (List)value;
            List<Variant> variantList = new ArrayList();
            objectList.forEach(obj -> variantList.add(objectToVariant(obj)));
            variant.setV_list(variantList);
        }
        return variant;
    }

    public static BigDecimal getBigDecimalFromVariant(Variant variant) throws ConverterException {
        Object object = parseObjectFromVariant(variant);
        BigDecimal balance;
        if (object.getClass() == Integer.class) {
            balance = new BigDecimal((Integer) object);
        }
        if (object.getClass() == Long.class) {
            balance = new BigDecimal((Long) object);
        } else if (object.getClass() == String.class) {
            balance = new BigDecimal((String) object);
        } else if (object.getClass() == Double.class) {
            balance = new BigDecimal((Double) object);
        } else {
            throw new ConverterException("Balance type is" + object.getClass());
        }
        return balance;
    }

    public static Object parseObjectFromVariant(Variant variant) throws ConverterException {
        //        Object value = null;
        if (variant.isSetV_string()) {
            return variant.getV_string();
        } else if (variant.isSetV_bool()) {
            return variant.getV_bool();
        } else if (variant.isSetV_double()) {
            return variant.getV_double();
        } else if (variant.isSetV_i8()) {
            return variant.getV_i8();
        } else if (variant.isSetV_i16()) {
            return variant.getV_i16();
        } else if (variant.isSetV_i32()) {
            return variant.getV_i32();
        } else if (variant.isSetV_i64()) {
            return variant.getV_i64();

        } else if (variant.isSetV_list()) {
            List<Variant> variantList = variant.getV_list();
            List<Object> objectList = new ArrayList<>();
            for (Variant element : variantList) {
                objectList.add(parseObjectFromVariant(element));
            }
            return objectList;
        } else if (variant.isSetV_map()) {
            Map<Variant, Variant> variantMap = variant.getV_map();
            Map<Object, Object> objectMap = new HashMap<>();
            for (Map.Entry<Variant, Variant> entry : variantMap.entrySet()) {
                objectMap.put(parseObjectFromVariant(entry.getKey()), parseObjectFromVariant(entry.getValue()));
            }
            return objectMap;
        } else if (variant.isSetV_set()) {
            Set<Variant> variantSet = variant.getV_set();
            Set<Object> objectSet = new HashSet<>();
            for (Variant element : variantSet) {
                objectSet.add(parseObjectFromVariant(element));
            }
            return objectSet;
        }
        throw new ConverterException("Unsupported variant type");
    }
}
