package com.credits.general.util.variant;

import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.VariantData;
import com.credits.general.pojo.VariantType;
import com.credits.general.thrift.generated.Any;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VariantUtils {
    public static final String COLLECTION_VALUES_DELIMITER = ",";
    public static final String MAP_KEY_VALUE_DELIMITER = ":";
    public static final String ARRAY_TYPE = "Array";
    public static final String NULL_TYPE = "Null";
    public static final String STRING_TYPE = "String";
    public static final Byte NULL_TYPE_VALUE = 0;
    public static final Byte VOID_TYPE_VALUE = 0;
    public static final String OBJECT_TYPE = "Object";
    public static final String VOID_TYPE = "Void";

    private static final String INVALID_VARIANT_DATA_MESSAGE = "Invalid VariantData fields";

    public static void validateVariantData(VariantType variantType, Object boxedValue) throws CreditsException {
        if (variantType == null) {
            throw new CreditsException("variantType is null");
        }
        if (variantType == VariantType.OBJECT) {
            if (!(boxedValue instanceof Any))
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
        } else if (variantType == VariantType.NULL || variantType == VariantType.VOID) {
            if (boxedValue != null)
                throw new CreditsException("Variant VOID value must be null");
        } else if (variantType == VariantType.BOOL || variantType == VariantType.BOOL_BOX) {
            if (!(boxedValue instanceof Boolean))
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
        } else if (variantType == VariantType.BYTE || variantType == VariantType.BYTE_BOX) {
            if (!(boxedValue instanceof Byte))
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
        } else if (variantType == VariantType.SHORT || variantType == VariantType.SHORT_BOX) {
            if (!(boxedValue instanceof Short))
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
        } else if (variantType == VariantType.INT || variantType == VariantType.INT_BOX) {
            if (!(boxedValue instanceof Integer))
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
        } else if (variantType == VariantType.LONG || variantType == VariantType.LONG_BOX) {
            if (!(boxedValue instanceof Long))
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
        } else if (variantType == VariantType.FLOAT || variantType == VariantType.FLOAT_BOX) {
            if (!(boxedValue instanceof Float))
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
        } else if (variantType == VariantType.DOUBLE || variantType == VariantType.DOUBLE_BOX) {
            if (!(boxedValue instanceof Double))
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
        } else if (variantType == VariantType.STRING) {
            if (!(boxedValue instanceof String))
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
        } else if (variantType == VariantType.LIST) {
            if (!(boxedValue instanceof List)) {
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
            } else {
                ((List<VariantData>) boxedValue).forEach(variantData -> {
                    VariantUtils.validateVariantData(variantData.getVariantType(), variantData.getBoxedValue());
                });
            }
        } else if (variantType == VariantType.SET) {
            if (!(boxedValue instanceof Set)) {
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
            } else {
                ((Set<VariantData>) boxedValue).forEach(variantData -> {
                    VariantUtils.validateVariantData(variantData.getVariantType(), variantData.getBoxedValue());
                });
            }
        } else if (variantType == VariantType.MAP) {
            if (!(boxedValue instanceof Map)) {
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
            } else {

                ((Map<VariantData, VariantData>) boxedValue).forEach((key, value) -> {
                    VariantUtils.validateVariantData(key.getVariantType(), key.getBoxedValue());
                    VariantUtils.validateVariantData(value.getVariantType(), value.getBoxedValue());
                });
            }
        } else if (variantType == VariantType.ARRAY) {
            if (!(boxedValue.getClass().isArray())) {
                throw new CreditsException(INVALID_VARIANT_DATA_MESSAGE);
            } else {
                Arrays.asList((Object[])boxedValue).forEach(object -> {
                    VariantUtils.validateVariantData(((VariantData)object).getVariantType(), ((VariantData)object).getBoxedValue());
                });
            }
        } else {
            throw new CreditsException(String.format("Unsupported variant type: %s", variantType.name));
        }
    }

    public static VariantData createVariantData(String className, String valueAsString) {

        if (valueAsString == null || valueAsString.trim().equals("")) {
            return new VariantData(VariantType.NULL, null);
        }
        // check is array
        if (className.contains("[]")) {
            className = String.format("%s<%s>", ARRAY_TYPE, GeneralSourceCodeUtils.parseArrayType(className));
        }
        // parse Generic
        Pair<String, String> classNameAndGeneric = GeneralSourceCodeUtils.splitClassnameAndGeneric(className);
        String classNameWOGeneric = classNameAndGeneric.getLeft();
        String genericName = classNameAndGeneric.getRight();
        boolean isGenericExists = !(genericName == null || genericName.trim().equals(""));

        VariantType variantType = VariantType.parseVariant(classNameWOGeneric);

        Object boxedValue;
        // collections values
        String[] collectionsValues = null;
        if (variantType.isCollection()) {
            if (!isGenericExists) {
                throw new CreditsException("Collections without generics is not supported");
            }
            collectionsValues = valueAsString.split(COLLECTION_VALUES_DELIMITER);
        }

        switch (variantType) {
            case VOID:
                if (valueAsString != null) {
                    throw new CreditsException("Value of Variant VOID type must be null");
                }
                boxedValue = null;
                break;
            case STRING: boxedValue = valueAsString; break;
            case BYTE_BOX: boxedValue = GeneralConverter.toByte(valueAsString); break;
            case BYTE: boxedValue = GeneralConverter.toByte(valueAsString); break;
            case SHORT_BOX: boxedValue = GeneralConverter.toShort(valueAsString); break;
            case SHORT: boxedValue = GeneralConverter.toShort(valueAsString); break;
            case INT_BOX: boxedValue = GeneralConverter.toInteger(valueAsString); break;
            case INT: boxedValue = GeneralConverter.toInteger(valueAsString); break;
            case LONG_BOX: boxedValue = GeneralConverter.toLong(valueAsString); break;
            case LONG: boxedValue = GeneralConverter.toLong(valueAsString); break;
            case FLOAT_BOX: boxedValue = GeneralConverter.toFloat(valueAsString); break;
            case FLOAT: boxedValue = GeneralConverter.toFloat(valueAsString); break;
            case DOUBLE_BOX: boxedValue = GeneralConverter.toDouble(valueAsString); break;
            case DOUBLE: boxedValue = GeneralConverter.toDouble(valueAsString); break;
            case BOOL_BOX: boxedValue = GeneralConverter.toBoolean(valueAsString); break;
            case BOOL: boxedValue = GeneralConverter.toBoolean(valueAsString); break;
            case ARRAY:
                VariantData[] variantDataArr = new VariantData[collectionsValues.length];
                for (int i = 0; i < collectionsValues.length; i++) {
                    variantDataArr[i] = VariantUtils.createVariantData(genericName, collectionsValues[i].trim());
                }
                boxedValue = variantDataArr;
                break;
            case LIST:
                List<VariantData> variantDataList = new ArrayList<>();
                for (String collectionsValue : collectionsValues) {
                    variantDataList.add(
                            VariantUtils.createVariantData(genericName, collectionsValue.trim())
                    );
                }
                boxedValue = variantDataList;
                break;
            case MAP:
                Map<VariantData, VariantData> variantDataMap = new HashMap<>();
                String[] keyValueGenerincnamePair = genericName.split(", ");
                for (String collectionsValue : collectionsValues) {
                    String[] keyValuePair = collectionsValue.split(MAP_KEY_VALUE_DELIMITER);
                    VariantData keyVariantData = VariantUtils.createVariantData(keyValueGenerincnamePair[0], keyValuePair[0]);
                    VariantData valueVariantData = VariantUtils.createVariantData(keyValueGenerincnamePair[1], keyValuePair[1]);
                    variantDataMap.put(keyVariantData, valueVariantData);
                }
                boxedValue = variantDataMap;
                break;
            case SET:
                Set<VariantData> variantDataSet = new HashSet<>();
                for (String collectionsValue : collectionsValues) {
                    variantDataSet.add(
                            VariantUtils.createVariantData(genericName, collectionsValue.trim())
                    );
                }
                boxedValue = variantDataSet;
                break;
            default: throw new IllegalArgumentException(String.format("Unsupported class: %s", className));
        }
        return new VariantData(variantType, boxedValue);
    }
}
