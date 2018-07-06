package com.credits.thrift;

public class VariantMapper {
    public static Variant map(Object object) {
        Variant variant = null;
        if (object == null) {
            return variant;
        }
        if (object instanceof Boolean) {
            variant = new Variant(Variant._Fields.V_BOOL, object);
        }
        if (object instanceof Byte) {
            variant = new Variant(Variant._Fields.V_I8, object);
        }
        if (object instanceof Short) {
            variant = new Variant(Variant._Fields.V_I16, object);
        }
        if (object instanceof Integer) {
            variant = new Variant(Variant._Fields.V_I32, object);
        }
        if (object instanceof Long) {
            variant = new Variant(Variant._Fields.V_I64, object);
        }
        if (object instanceof Double) {
            variant = new Variant(Variant._Fields.V_DOUBLE, object);
        }
        if (object instanceof String) {
            variant = new Variant(Variant._Fields.V_STRING, object);
        }

        return variant;
    }
}
