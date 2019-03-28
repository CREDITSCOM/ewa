package com.credits.general.util.variant;

import com.credits.general.thrift.generated.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Stream;

import static com.credits.general.TestHelper.ExampleClass;
import static com.credits.general.serialize.Serializer.serialize;
import static com.credits.general.thrift.generated.Variant._Fields.V_ARRAY;
import static com.credits.general.thrift.generated.Variant._Fields.V_BOOLEAN_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_DOUBLE_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_FLOAT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_INT;
import static com.credits.general.thrift.generated.Variant._Fields.V_INT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_LIST;
import static com.credits.general.thrift.generated.Variant._Fields.V_LONG_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_MAP;
import static com.credits.general.thrift.generated.Variant._Fields.V_NULL;
import static com.credits.general.thrift.generated.Variant._Fields.V_OBJECT;
import static com.credits.general.thrift.generated.Variant._Fields.V_SET;
import static com.credits.general.thrift.generated.Variant._Fields.V_SHORT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static com.credits.general.thrift.generated.Variant._Fields.V_VOID;
import static com.credits.general.util.variant.VariantConverter.NULL_TYPE_VALUE;
import static com.credits.general.util.variant.VariantConverter.VOID_TYPE_VALUE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ObjectToVariantConverterConverterTest {

    @SuppressWarnings("unchecked")
    static Stream<Object> provideObjectsForMapping() {
        return Stream.of(
            Arguments.of(null, V_NULL, NULL_TYPE_VALUE),
            Arguments.of(Void.TYPE, V_VOID, VOID_TYPE_VALUE),
            Arguments.of(Boolean.TRUE, V_BOOLEAN_BOX, Boolean.TRUE),
            Arguments.of(Integer.MAX_VALUE, V_INT_BOX, Integer.MAX_VALUE),
            Arguments.of(Short.MAX_VALUE, V_SHORT_BOX, Short.MAX_VALUE),
            Arguments.of(Long.MAX_VALUE, V_LONG_BOX, Long.MAX_VALUE),
            Arguments.of(Float.MAX_VALUE, V_FLOAT_BOX, (double) Float.MAX_VALUE),
            Arguments.of(Double.MAX_VALUE, V_DOUBLE_BOX, Double.MAX_VALUE),
            Arguments.of("String", V_STRING, "String"),
            Arguments.of(new Random(23), V_OBJECT, ByteBuffer.wrap(serialize(new Random(23)))),

            Arguments.of(
                new ArrayList() {{
                    add("object");
                }},
                V_LIST,
                new ArrayList() {{
                    add(new Variant(V_STRING, "object"));
                }}),

            Arguments.of(
                new HashMap() {{
                    put("one", 1);
                }},
                V_MAP,
                new HashMap() {{
                    put(new Variant(V_STRING, "one"), new Variant(V_INT_BOX, 1));
                }}),

            Arguments.of(
                new HashSet() {{
                    add("object");
                }},
                V_SET,
                new HashSet() {{
                    add(new Variant(V_STRING, "object"));
                }}),

            Arguments.of(
                new Object[] {null, new ExampleClass(23)},
                V_ARRAY,
                new ArrayList() {{
                    add(new Variant(V_NULL, NULL_TYPE_VALUE));
                    add(new Variant(V_OBJECT, ByteBuffer.wrap(serialize(new ExampleClass(23)))));
                }}
            ),

            Arguments.of(
                new int[] {1, 2, 3},
                V_ARRAY,
                new ArrayList() {{
                    add(new Variant(V_INT, 1));
                    add(new Variant(V_INT, 2));
                    add(new Variant(V_INT, 3));
                }}),

            Arguments.of(
                new Integer[] {1, 2, 3},
                V_ARRAY,
                new ArrayList() {{
                    add(new Variant(V_INT_BOX, 1));
                    add(new Variant(V_INT_BOX, 2));
                    add(new Variant(V_INT_BOX, 3));
                }}));
    }


    @ParameterizedTest
    @MethodSource("provideObjectsForMapping")
    public void objectsMap(Object inputObject, Variant._Fields expectedType, Object expectedValue) {
        Variant variant = VariantConverter.toVariant(inputObject);

        assertAll(() -> {
            assertEquals(expectedType, variant.getSetField());
            assertEquals(expectedValue, variant.getFieldValue());
        });
    }

    @Test
    public void primitiveMap() {
        Variant variantValue = VariantConverter.toVariant(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, variantValue.getV_int());

        variantValue = VariantConverter.toVariant(Short.MAX_VALUE);
        assertEquals(Short.MAX_VALUE, variantValue.getV_short());

        variantValue = VariantConverter.toVariant(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, variantValue.getV_long());

        variantValue = VariantConverter.toVariant(Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, variantValue.getV_float());

        variantValue = VariantConverter.toVariant(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, variantValue.getV_double());

        variantValue = VariantConverter.toVariant(Byte.MAX_VALUE);
        assertEquals(Byte.MAX_VALUE, variantValue.getV_byte());
    }
}
