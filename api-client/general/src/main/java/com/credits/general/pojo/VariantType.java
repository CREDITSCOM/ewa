package com.credits.general.pojo;

public enum VariantType {
    OBJECT("Object"),
    STRING("String"),
    BYTE("byte"),
    BYTE_BOX("Byte"),
    SHORT("short"),
    SHORT_BOX("Short"),
    INT("int"),
    INT_BOX("Integer"),
    LONG("long"),
    LONG_BOX("Long"),
    DOUBLE("double"),
    DOUBLE_BOX("Double"),
    BOOL("bool"),
    BOOL_BOX("Bool"),
    LIST("List"),
    SET("Set"),
    MAP("Map");

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
        throw new IllegalArgumentException("Incorrect variant type \"" + variant + "\"");
    }
}

