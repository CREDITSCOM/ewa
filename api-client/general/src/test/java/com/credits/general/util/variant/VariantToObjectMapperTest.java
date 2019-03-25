package com.credits.general.util.variant;

import com.credits.general.thrift.generated.Variant;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

import static com.credits.general.TestHelper.ExampleClass;
import static com.credits.general.serialize.Serializer.serialize;
import static com.credits.general.thrift.generated.Variant._Fields.V_DOUBLE;
import static com.credits.general.thrift.generated.Variant._Fields.V_DOUBLE_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_FLOAT;
import static com.credits.general.thrift.generated.Variant._Fields.V_FLOAT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_INT;
import static com.credits.general.thrift.generated.Variant._Fields.V_INT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_LIST;
import static com.credits.general.thrift.generated.Variant._Fields.V_LONG;
import static com.credits.general.thrift.generated.Variant._Fields.V_LONG_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_MAP;
import static com.credits.general.thrift.generated.Variant._Fields.V_NULL;
import static com.credits.general.thrift.generated.Variant._Fields.V_OBJECT;
import static com.credits.general.thrift.generated.Variant._Fields.V_SET;
import static com.credits.general.thrift.generated.Variant._Fields.V_SHORT;
import static com.credits.general.thrift.generated.Variant._Fields.V_SHORT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static com.credits.general.thrift.generated.Variant._Fields.V_VOID;
import static com.credits.general.util.variant.VariantUtils.NULL_TYPE_VALUE;
import static com.credits.general.util.variant.VariantUtils.VOID_TYPE_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VariantToObjectMapperTest {

    @SuppressWarnings("unchecked")
    static Stream<Object> provideObjectsForMapping() {
        return Stream.of(
            Arguments.of(new Variant(V_NULL, NULL_TYPE_VALUE), null),
            Arguments.of(new Variant(V_VOID, VOID_TYPE_VALUE), Void.TYPE),
            Arguments.of(new Variant(V_INT, Integer.MAX_VALUE), Integer.MAX_VALUE),
            Arguments.of(new Variant(V_INT_BOX, Integer.MAX_VALUE), Integer.MAX_VALUE),
            Arguments.of(new Variant(V_SHORT, Short.MAX_VALUE), Short.MAX_VALUE),
            Arguments.of(new Variant(V_SHORT_BOX, Short.MAX_VALUE), Short.MAX_VALUE),
            Arguments.of(new Variant(V_LONG, Long.MAX_VALUE), Long.MAX_VALUE),
            Arguments.of(new Variant(V_LONG_BOX, Long.MAX_VALUE), Long.MAX_VALUE),
            Arguments.of(new Variant(V_FLOAT, (double) Float.MAX_VALUE), Float.MAX_VALUE),
            Arguments.of(new Variant(V_FLOAT_BOX, (double) Float.MAX_VALUE), Float.MAX_VALUE),
            Arguments.of(new Variant(V_DOUBLE, Double.MAX_VALUE), Double.MAX_VALUE),
            Arguments.of(new Variant(V_DOUBLE_BOX, Double.MAX_VALUE), Double.MAX_VALUE),
            Arguments.of(new Variant(V_STRING, "string"), "string"),

            Arguments.of(
                new Variant(
                    V_LIST,
                    new ArrayList() {{
                        add(new Variant(V_STRING, "value1"));
                        add(new Variant(V_STRING, "value2"));
                    }}),
                new ArrayList() {{
                    add("value1");
                    add("value2");
                }}),

            Arguments.of(
                new Variant(
                    V_SET,
                    new HashSet() {{
                        add(new Variant(V_STRING, "value1"));
                        add(new Variant(V_VOID, VOID_TYPE_VALUE));
                    }}),
                new HashSet() {{
                    add("value1");
                    add(Void.TYPE);
                }}),

            Arguments.of(
                new Variant(
                    V_MAP,
                    new HashMap() {{
                        put(new Variant(V_STRING, "key1"), new Variant(V_STRING, "value"));
                        put(new Variant(V_STRING, "key2"), new Variant(V_STRING, "value"));
                        put(new Variant(V_NULL, NULL_TYPE_VALUE), new Variant(V_NULL, NULL_TYPE_VALUE));
                    }}),
                new HashMap() {{
                    put("key1", "value");
                    put("key2", "value");
                    put(null, null);
                }}),

            Arguments.of(
                new Variant(V_OBJECT, ByteBuffer.wrap(serialize(new ExampleClass(23)))),
                new ExampleClass(23)));
    }


    @ParameterizedTest
    @MethodSource("provideObjectsForMapping")
    public void objectsMap(Variant inputVariant, Object expectedObject) {
        Object object = new VariantMapper.VariantToObject().apply(inputVariant);

        assertEquals(expectedObject, object);
    }


}