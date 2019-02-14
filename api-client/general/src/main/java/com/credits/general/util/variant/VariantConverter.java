package com.credits.general.util.variant;

import com.credits.general.pojo.VariantData;
import com.credits.general.pojo.VariantType;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.exception.ConverterException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VariantConverter {

    public static Variant variantDataToVariant(VariantData variantData) {
        return new VariantDataMapper().apply(variantData)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("Unsupported type of the value {" + variantData.getBoxedValue().toString() + "}: " + variantData.getVariantType().name);
                });
    }

    public static BigDecimal getBigDecimalFromVariant(Variant variant) throws ConverterException {
        VariantData variantData = variantToVariantData(variant);
        BigDecimal balance;
        VariantType variantType = variantData.getVariantType();
        Object value = variantData.getBoxedValue();
        if (variantType == VariantType.INT || variantType == VariantType.INT_BOX) {
            balance = new BigDecimal((Integer) value);
        } else if (variantType == VariantType.LONG || variantType == VariantType.LONG_BOX) {
            balance = new BigDecimal((Long) value);
        } else if (variantType == VariantType.STRING) {
            balance = new BigDecimal((String) value);
        } else if (variantType == VariantType.DOUBLE || variantType == VariantType.DOUBLE_BOX) {
            balance = new BigDecimal((Double) value);
        } else {
            throw new ConverterException("Balance type is" + variantData.getClass());
        }
        return balance;
    }

    public static VariantData variantToVariantData(Variant variant) throws ConverterException {
        VariantType variantType;
        Object boxedValue;
        if (variant.isSetV_null()) {
            variantType = VariantType.NULL;
            boxedValue = null;
        } else if (variant.isSetV_string()) {
            variantType = VariantType.STRING;
            boxedValue = variant.getV_string();
        } else if (variant.isSetV_boolean()) {
            variantType = VariantType.BOOL;
            boxedValue = variant.getV_boolean();
        } else if (variant.isSetV_boolean_box()) {
            variantType = VariantType.BOOL_BOX;
            boxedValue = variant.getV_boolean_box();

        } else if (variant.isSetV_double()) {
            variantType = VariantType.DOUBLE;
            boxedValue = variant.getV_double();
        } else if (variant.isSetV_double_box()) {
            variantType = VariantType.DOUBLE_BOX;
            boxedValue = variant.getV_double_box();

        } else if (variant.isSetV_float()) {
            variantType = VariantType.FLOAT;
            boxedValue = GeneralConverter.toFloat(variant.getV_float());
        } else if (variant.isSetV_float_box()) {
            variantType = VariantType.FLOAT_BOX;
            boxedValue = GeneralConverter.toFloat(variant.getV_float_box());

        } else if (variant.isSetV_byte()) {
            variantType = VariantType.BYTE;
            boxedValue = variant.getV_byte();
        } else if (variant.isSetV_byte_box()) {
            variantType = VariantType.BYTE_BOX;
            boxedValue = variant.getV_byte_box();

        } else if (variant.isSetV_short()) {
            variantType = VariantType.SHORT;
            boxedValue = variant.getV_short();
        } else if (variant.isSetV_short_box()) {
            variantType = VariantType.SHORT_BOX;
            boxedValue = variant.getV_short_box();

        } else if (variant.isSetV_int()) {
            variantType = VariantType.INT;
            boxedValue = variant.getV_int();
        } else if (variant.isSetV_int_box()) {
            variantType = VariantType.INT_BOX;
            boxedValue = variant.getV_int_box();

        } else if (variant.isSetV_long()) {
            variantType = VariantType.LONG;
            boxedValue = variant.getV_long();
        } else if (variant.isSetV_long_box()) {
            variantType = VariantType.LONG_BOX;
            boxedValue = variant.getV_long_box();

        } else if (variant.isSetV_list()) {
            variantType = VariantType.LIST;
            List<VariantData> variantDataList = new ArrayList<>();
            variant.getV_list().forEach(variantListElem -> {
                variantDataList.add(VariantConverter.variantToVariantData(variantListElem));
            });
            boxedValue = variantDataList;
        } else if (variant.isSetV_map()) {
            variantType = VariantType.MAP;
            Map<VariantData, VariantData> variantDataMap = new HashMap<>();
            variant.getV_map().entrySet().forEach(entry -> {
                VariantData key = VariantConverter.variantToVariantData(entry.getKey());
                VariantData value = VariantConverter.variantToVariantData(entry.getValue());
                variantDataMap.put(key, value);
            });
            boxedValue = variantDataMap;
        } else if (variant.isSetV_set()) {
            variantType = VariantType.SET;
            Set<VariantData> variantDataSet = new HashSet<>();
            variant.getV_set().forEach(variantSetElem -> {
                variantDataSet.add(VariantConverter.variantToVariantData(variantSetElem));
            });
            boxedValue = variantDataSet;
        } else if (variant.isSetV_array()) {
            variantType = VariantType.ARRAY;
            List<VariantData> variantDataList = new ArrayList<>();
            variant.getV_array().forEach(variantListElem -> {
                variantDataList.add(VariantConverter.variantToVariantData(variantListElem));
            });
            boxedValue = variantDataList.toArray(new VariantData[variantDataList.size()]);
        } else {
            throw new ConverterException("Unsupported variant type");
        }
        return new VariantData(variantType, boxedValue);
    }

    public static Object variantToObject(Variant variant) {
        Object object;
        if (variant.isSetV_null()) {
            object = null;
        } else if (variant.isSetV_string()) {
            object = variant.getV_string();
        } else if (variant.isSetV_boolean()) {
            object = variant.getV_boolean();
        } else if (variant.isSetV_boolean_box()) {
            object = variant.getV_boolean_box();
        } else if (variant.isSetV_double()) {
            object = variant.getV_double();
        } else if (variant.isSetV_double_box()) {
            object = variant.getV_double_box();
        } else if (variant.isSetV_float()) {
            object = GeneralConverter.toFloat(variant.getV_float());
        } else if (variant.isSetV_float_box()) {
            object = GeneralConverter.toFloat(variant.getV_float_box());
        } else if (variant.isSetV_byte()) {
            object = variant.getV_byte();
        } else if (variant.isSetV_byte_box()) {
            object = variant.getV_byte_box();
        } else if (variant.isSetV_short()) {
            object = variant.getV_short();
        } else if (variant.isSetV_short_box()) {
            object = variant.getV_short_box();
        } else if (variant.isSetV_int()) {
            object = variant.getV_int();
        } else if (variant.isSetV_int_box()) {
            object = variant.getV_int_box();
        } else if (variant.isSetV_long()) {
            object = variant.getV_long();
        } else if (variant.isSetV_long_box()) {
            object = variant.getV_long_box();
        } else if (variant.isSetV_array()) {
            List<Variant> variantList = variant.getV_array();
            object = variantList.stream().map(
                    VariantConverter::variantToObject
            ).toArray();
        } else if (variant.isSetV_list()) {
            object = variant.getV_list().stream().map(
                    VariantConverter::variantToObject
            ).collect(Collectors.toList());
        } else if (variant.isSetV_set()) {
            object = variant.getV_set().stream().map(
                    VariantConverter::variantToObject
            ).collect(Collectors.toSet());
        } else if (variant.isSetV_map()) {
            Map<Object, Object> objectMap = new HashMap<>();
            variant.getV_map().entrySet().forEach(entry -> {
                Object key = VariantConverter.variantToObject(entry.getKey());
                Object value = VariantConverter.variantToObject(entry.getValue());
                objectMap.put(key, value);
            });
            object = objectMap;
        } else {
            throw new ConverterException("Unsupported variant type");
        }
        return object;
    }

    public static VariantData objectToVariantData(Object object) {
        VariantData variantData = null;
        if (object == null) {
            variantData = new VariantData(VariantType.NULL, null);
        } else if (object.getClass().isArray()) {
            variantData = fromArrayToVariantData(object);
        } else if (object.getClass().isPrimitive()) {
            variantData = fromPrimitiveTypeToVariantData(object);
        } else if (object instanceof List) {
            List<VariantData> variantDataCollection = ((List<Object>) object).stream()
                .map(VariantConverter::objectToVariantData)
                .collect(Collectors.toList());
            variantData = new VariantData(VariantType.LIST, variantDataCollection);
        } else if (object instanceof Set) {
            Set<VariantData> variantDataCollection =
                ((Set<Object>) object).stream().map(VariantConverter::objectToVariantData).collect(Collectors.toSet());
            variantData = new VariantData(VariantType.SET, variantDataCollection);
        } else if (object instanceof Map) {
            Map<VariantData, VariantData> variantDataMap = ((Map<Object, Object>) object).entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> VariantConverter.objectToVariantData(entry.getKey()),
                    entry -> VariantConverter.objectToVariantData((entry.getValue()))));
            variantData = new VariantData(VariantType.MAP, variantDataMap);
        } else if (object instanceof Boolean) {
            variantData = new VariantData(VariantType.BOOL_BOX, object);
        } else if (object instanceof Byte) {
            variantData = new VariantData(VariantType.BYTE_BOX, object);
        } else if (object instanceof Short) {
            variantData = new VariantData(VariantType.SHORT_BOX, object);
        } else if (object instanceof Integer) {
            variantData = new VariantData(VariantType.INT_BOX, object);
        } else if (object instanceof Long) {
            variantData = new VariantData(VariantType.LONG_BOX, object);
        } else if (object instanceof Float) {
            variantData = new VariantData(VariantType.FLOAT_BOX, object);
        } else if (object instanceof Double) {
            variantData = new VariantData(VariantType.DOUBLE_BOX, object);
        } else if (object instanceof String) {
            variantData = new VariantData(VariantType.STRING, object);
        }
        if (variantData == null) {
            throw new ConverterException(
                String.format("Unsupported object type: %s", object.getClass().getSimpleName()));
        }
        return variantData;
    }

    private static VariantData fromPrimitiveTypeToVariantData(Object object) {
        VariantData variantData = null;
        if (byte.class.isInstance(object)) {
            variantData = new VariantData(VariantType.BYTE, object);
        }
        if (int.class.isInstance(object)) {
            variantData = new VariantData(VariantType.INT, object);
        }
        if (short.class.isInstance(object)) {
            variantData = new VariantData(VariantType.SHORT, object);
        }
        if (long.class.isInstance(object)) {
            variantData = new VariantData(VariantType.LONG, object);
        }
        if (float.class.isInstance(object)) {
            variantData = new VariantData(VariantType.FLOAT, object);
        }
        if (double.class.isInstance(object)) {
            variantData = new VariantData(VariantType.DOUBLE, object);
        }
        return variantData;
    }

    private static VariantData fromArrayToVariantData(Object object) {
        VariantData variantData;
        List<VariantData> variantDataCollection = new ArrayList<>();
        if (object instanceof Object[]) {
            variantDataCollection = Arrays.stream((Object[]) object)
                .map(VariantConverter::objectToVariantData)
                .collect(Collectors.toList());
        } else if (object instanceof byte[]) {
            for (byte b : (byte[]) object) {
                variantDataCollection.add(new VariantData(VariantType.BYTE, b));
            }
        } else if (object instanceof int[]) {
            for (int i : (int[]) object) {
                variantDataCollection.add(new VariantData(VariantType.INT, i));
            }
        } else if (object instanceof long[]) {
            for (long i : (long[]) object) {
                variantDataCollection.add(new VariantData(VariantType.LONG, i));
            }
        } else if (object instanceof short[]) {
            for (short s : (short[]) object) {
                variantDataCollection.add(new VariantData(VariantType.SHORT, s));
            }
        } else if (object instanceof float[]) {
            for (float f : (float[]) object) {
                variantDataCollection.add(new VariantData(VariantType.FLOAT, f));
            }
        } else if (object instanceof double[]) {
            for (double d : (double[]) object) {
                variantDataCollection.add(new VariantData(VariantType.DOUBLE, d));
            }
        }

        if(variantDataCollection.isEmpty()) {
            throw new ConverterException(
                String.format("Unsupported object type: %s", object.getClass().getSimpleName()));
        }
        variantData = new VariantData(VariantType.ARRAY, variantDataCollection.toArray(new VariantData[] {}));
        return variantData;
    }

    public static Object variantDataToObject(VariantData variantData) {
        VariantType variantType = variantData.getVariantType();
        Object boxedValue = variantData.getBoxedValue();
        switch (variantType) {
            case ARRAY:
                VariantData[] variantDataArr = (VariantData[]) boxedValue;
                if (variantDataArr[0].getVariantType() == VariantType.BYTE) {
                    byte[] bytes = new byte[variantDataArr.length];
                    for (int i = 0; i < variantDataArr.length; i++) {
                        bytes[i] = (byte) variantDataArr[i].getBoxedValue();
                    }
                    return bytes;
                }
                //fixme add int, short, long, float, double array converters
               List<Object> objectArrAsList =
                       Arrays.stream(variantDataArr).map(
                               VariantConverter::variantDataToObject
                       ).collect(Collectors.toList());
               return objectArrAsList.toArray();
           case LIST:
               List<VariantData> variantDataList = (List<VariantData>) boxedValue;
               return variantDataList.stream().map(
                       VariantConverter::variantDataToObject
               ).collect(Collectors.toList());
           case SET:
               Set<VariantData> variantDataSet = (Set<VariantData>) boxedValue;
               return variantDataSet.stream().map(
                       VariantConverter::variantDataToObject
                       ).collect(Collectors.toSet());
           case MAP:
               Map<VariantData, VariantData> variantDataMap = (Map<VariantData, VariantData>) boxedValue;
               return variantDataMap.entrySet().stream()
                       .collect(Collectors.toMap(
                           e -> VariantConverter.variantDataToObject(e.getKey()),
                           e -> VariantConverter.variantDataToObject(e.getValue())
                       ));
           default:
               return boxedValue;
       }
   }
}