package com.credits.general.util.variant;

import com.credits.general.thrift.generated.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

import static com.credits.general.thrift.generated.Variant._Fields.V_INT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ObjectMapperTest {

    @SuppressWarnings("unchecked")
    static Stream<Object> provideObjectsForMapping() {
        return Stream.of(
            Arguments.of(null, (byte) 0),
            Arguments.of(Boolean.TRUE, Boolean.TRUE),
            Arguments.of(Integer.MAX_VALUE, Integer.MAX_VALUE),
            Arguments.of(Long.MAX_VALUE, Long.MAX_VALUE),
            Arguments.of(Float.MAX_VALUE, (double) Float.MAX_VALUE),
            Arguments.of(Double.MAX_VALUE, Double.MAX_VALUE),
            Arguments.of("String", "String"),

            Arguments.of(
                new ArrayList() {{
                    add("object");
                }},
                new ArrayList() {{
                    add(new Variant(V_STRING, "object"));
                }}),

            Arguments.of(
                new HashMap() {{
                    put("one", 1);
                }},
                new HashMap() {{
                    put(new Variant(V_STRING, "one"), new Variant(V_INT_BOX, 1));
                }}),

            Arguments.of(
                new HashSet() {{
                    add("object");
                }},
                new HashSet() {{
                    add(new Variant(V_STRING, "object"));
                }}),

            Arguments.of(
                new String[] {"str", "str"},
                new ArrayList() {{
                    add(new Variant(V_STRING, "str"));
                    add(new Variant(V_STRING, "str"));
                }}
            ));

    }


    @ParameterizedTest
    @MethodSource("provideObjectsForMapping")
    public void objectsMap(Object inputObject, Object expected) {
        Object variantValue = new ObjectMapper()
            .apply(inputObject).orElseThrow(null)
            .getFieldValue();

        assertEquals(expected, variantValue);
    }

    @Test
    public void primitiveMap() {

    }
}
