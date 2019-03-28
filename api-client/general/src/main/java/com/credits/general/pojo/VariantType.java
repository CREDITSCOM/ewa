package com.credits.general.pojo;

import com.credits.general.exception.CreditsException;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum VariantType {
    OBJECT(Object.class.getSimpleName()),
    NULL("null"),
    STRING(String.class.getSimpleName()),
    BYTE(byte.class.getSimpleName()),
    BYTE_BOX(Byte.class.getSimpleName()),
    SHORT(short.class.getSimpleName()),
    SHORT_BOX(Short.class.getSimpleName()),
    INT(int.class.getSimpleName()),
    INT_BOX(Integer.class.getSimpleName()),
    LONG(long.class.getSimpleName()),
    LONG_BOX(Long.class.getSimpleName()),
    FLOAT(float.class.getSimpleName()),
    FLOAT_BOX(Float.class.getSimpleName()),
    DOUBLE(double.class.getSimpleName()),
    DOUBLE_BOX(Double.class.getSimpleName()),
    BOOL(boolean.class.getSimpleName()),
    BOOL_BOX(Boolean.class.getSimpleName()),
    LIST(List.class.getSimpleName()),
    SET(Set.class.getSimpleName()),
    MAP(Map.class.getSimpleName()),
    ARRAY(Array.class.getSimpleName()),
    VOID(Void.class.getSimpleName());

    public final String name;

    VariantType(String name) {
        this.name = name;
    }

    public static VariantType parseVariant(String variant) {
        for (VariantType variantType : values()) {
            if (variantType.name.equals(variant)) {
                return variantType;
            }
        }
        StringBuilder availableVariantType = new StringBuilder("\n_____________________________\nAvailable variant types:");
        for (VariantType value : VariantType.values()) {
            availableVariantType.append("\n").append(value.toString().toLowerCase());
        }
        throw new CreditsException("Incorrect variant type \"" + variant + "\"" + availableVariantType);
    }

    public boolean isCollection() {
        return (
                this.equals(VariantType.LIST)
                || this.equals(VariantType.MAP)
                || this.equals(VariantType.SET)
                || this.equals(VariantType.ARRAY)
        );
    }
}

