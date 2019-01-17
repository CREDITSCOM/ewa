package com.credits.general.pojo;

import com.credits.general.exception.CreditsException;
import com.credits.general.util.variant.VariantUtils;

public enum VariantType {
    OBJECT("Object"),
    NULL(VariantUtils.NULL_TYPE),
    STRING(VariantUtils.STRING_TYPE),
    BYTE("byte"),
    BYTE_BOX("Byte"),
    SHORT("short"),
    SHORT_BOX("Short"),
    INT("int"),
    INT_BOX("Integer"),
    LONG("long"),
    LONG_BOX("Long"),
    FLOAT("float"),
    FLOAT_BOX("Float"),
    DOUBLE("double"),
    DOUBLE_BOX("Double"),
    BOOL("boolean"),
    BOOL_BOX("Boolean"),
    LIST("List"),
    SET("Set"),
    MAP("Map"),
    ARRAY(VariantUtils.ARRAY_TYPE);

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

