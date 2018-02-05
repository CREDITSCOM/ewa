package com.credits.serialise;

public enum SupportedSerialisationType {

    BYTE_PRIM_TYPE(Byte.TYPE),
    SHORT_PRIM_TYPE(Short.TYPE),
    INT_PRIM_TYPE(Integer.TYPE),
    LONG_PRIM_TYPE(Long.TYPE),
    FLOAT_PRIM_TYPE(Float.TYPE),
    DOUBLE_PRIM_TYPE(Double.TYPE),
    CHAR_PRIM_TYPE(Character.TYPE),
    BOOLEAN_PRIM_TYPE(Boolean.TYPE),
    BYTE_OBJ_TYPE(java.lang.Byte.class),
    SHORT_OBJ_TYPE(java.lang.Short.class),
    INT_OBJ_TYPE(java.lang.Integer.class),
    LONG_OBJ_TYPE(java.lang.Long.class),
    FLOAT_OBJ_TYPE(java.lang.Float.class),
    DOUBLE_OBJ_TYPE(java.lang.Double.class),
    CHAR_OBJ_TYPE(java.lang.Character.class),
    BOOLEAN_OBJ_TYPE(java.lang.Boolean.class),
    STRING_TYPE(java.lang.String.class);

    private Class<?> clazz;

    SupportedSerialisationType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
