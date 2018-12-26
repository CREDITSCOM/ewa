package com.credits.general.util.variant;

import com.credits.general.pojo.VariantData;
import com.credits.general.pojo.VariantType;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.exception.UnsupportedTypeException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VariantDataMapper implements Function<VariantData, Optional<Variant>> {

    @Override
    public Optional<Variant> apply(VariantData o) {
        try {
            return Optional.of(map(o));
        } catch (UnsupportedTypeException e) {
            return Optional.empty();
        }
    }
    
    @SuppressWarnings("unchecked")
    private Variant map(VariantData variantData) throws UnsupportedTypeException {
        Variant variant;
        if (variantData == null) {
            return new Variant(Variant._Fields.V_NULL, VariantUtils.NULL_TYPE_VALUE);
        }
        VariantType variantType = variantData.getVariantType();
        switch(variantType) {
            case ARRAY:
                List<Variant> variantCollectionArray =
                        Arrays.stream((VariantData[]) variantData.getBoxedValue()).map(this::mapSimpleType).collect(Collectors.toList());

                if (variantCollectionArray.stream().anyMatch(Objects::isNull)) {
                    throw new UnsupportedTypeException();
                }
                variant = new Variant(Variant._Fields.V_ARRAY, variantCollectionArray);
                break;
            case LIST:
                List<Variant> variantCollectionList =
                        ((List<VariantData>) variantData.getBoxedValue()).stream().map(this::mapSimpleType).collect(Collectors.toList());

                if (variantCollectionList.stream().anyMatch(Objects::isNull)) {
                    throw new UnsupportedTypeException();
                }

                variant = new Variant(Variant._Fields.V_LIST, variantCollectionList);
                break;
            case SET:
                Set<Variant> variantCollectionSet =
                        ((Set<VariantData>) variantData.getBoxedValue()).stream().map(this::mapSimpleType).collect(Collectors.toSet());

                if (variantCollectionSet.stream().anyMatch(Objects::isNull)) {
                    throw new UnsupportedTypeException();
                }

                variant = new Variant(Variant._Fields.V_SET, variantCollectionSet);
                break;
            case MAP:
                Map<Variant, Variant> variantMap = ((Map<VariantData, VariantData>) variantData.getBoxedValue()).entrySet()
                        .stream()
                        .collect(Collectors.toMap(entry -> mapSimpleType(entry.getKey()), entry -> mapSimpleType(entry.getValue())));

                boolean match = variantMap.entrySet()
                        .stream()
                        .anyMatch(vEntry -> Objects.isNull(vEntry.getKey()) || Objects.isNull(vEntry.getValue()));
                if (match) {
                    throw new UnsupportedTypeException();
                }

                variant = new Variant(Variant._Fields.V_MAP, variantMap);
                break;
            default:
                variant = mapSimpleType(variantData);
        }

        return variant;
    }

    /**
     * Returns null for unsupported
     *
     * @param variantData an object to map
     * @return Thrift custom type defined as Variant
     */
    private Variant mapSimpleType(VariantData variantData) {
        Variant variant;
        VariantType variantType = variantData.getVariantType();
        Object boxedValue = variantData.getBoxedValue();
        switch (variantType) {
            case NULL:
                variant = new Variant(Variant._Fields.V_NULL, VariantUtils.NULL_TYPE_VALUE);
                break;
            case BOOL:
                variant = new Variant(Variant._Fields.V_BOOLEAN, boxedValue);
                break;
            case BOOL_BOX:
                variant = new Variant(Variant._Fields.V_BOOLEAN_BOX, boxedValue);
                break;
            case BYTE:
                variant = new Variant(Variant._Fields.V_BYTE, boxedValue);
                break;
            case BYTE_BOX:
                variant = new Variant(Variant._Fields.V_BYTE_BOX, boxedValue);
                break;
            case SHORT:
                variant = new Variant(Variant._Fields.V_SHORT, boxedValue);
                break;
            case SHORT_BOX:
                variant = new Variant(Variant._Fields.V_SHORT_BOX, boxedValue);
                break;
            case INT:
                variant = new Variant(Variant._Fields.V_INT, boxedValue);
                break;
            case INT_BOX:
                variant = new Variant(Variant._Fields.V_INT_BOX, boxedValue);
                break;
            case LONG:
                variant = new Variant(Variant._Fields.V_LONG, boxedValue);
                break;
            case LONG_BOX:
                variant = new Variant(Variant._Fields.V_LONG_BOX, boxedValue);
                break;
            case FLOAT:
                variant = new Variant(Variant._Fields.V_FLOAT, GeneralConverter.toDouble(boxedValue));
                break;
            case FLOAT_BOX:
                variant = new Variant(Variant._Fields.V_FLOAT_BOX, GeneralConverter.toDouble(boxedValue));
                break;
            case DOUBLE:
                variant = new Variant(Variant._Fields.V_DOUBLE, boxedValue);
                break;
            case DOUBLE_BOX:
                variant = new Variant(Variant._Fields.V_DOUBLE_BOX, boxedValue);
                break;
            case STRING:
                variant = new Variant(Variant._Fields.V_STRING, boxedValue);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported variant type: %s", variantType.name));
        }
        return variant;
    }
}
