package general.tests.util.variant;

import com.credits.general.thrift.generated.Variant;
import com.credits.general.thrift.generated.object;
import com.credits.general.util.variant.VariantConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

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
import static com.credits.general.util.Utils.getClassType;
import static com.credits.general.util.variant.VariantConverter.VOID_TYPE_VALUE;
import static general.tests.TestHelper.ExampleClass;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unchecked")
public class VariantConverterTest {

    private static ExampleClass exampleClass = new ExampleClass(23);
    private static ArrayList listWithStrings = new ArrayList() {{
        add("1");
        add("2");
    }};
    private static ArrayList listWithObjects = new ArrayList() {{
        add(new Variant(V_NULL, ""));
        add(new Variant(V_OBJECT, new object(exampleClass.getClass().getName(), ByteBuffer.wrap(serialize(exampleClass)))));
    }};
    private static HashMap mapStringInteger =
        new HashMap() {{
            put("one", 1);
        }};
    private static HashSet stringSet = new HashSet() {{
        add("object");
    }};
    private static Object[] objectsArray = new ExampleClass[] {null, exampleClass};
    private static int[] intsArray = new int[] {1, 2, 3};
    private static Integer[] integersArray = new Integer[] {1, 2, 3};

    @SuppressWarnings("unchecked")
    static Stream<Object> provideObjectsForMapping() {
        return Stream.of(
            Arguments.of(getClassType(""), null, V_NULL, getClassType("")),
            Arguments.of(Void.TYPE.getName(), Void.TYPE, V_VOID, VOID_TYPE_VALUE),
            Arguments.of(getClassType(Boolean.TRUE), Boolean.TRUE, V_BOOLEAN_BOX, Boolean.TRUE),
            Arguments.of(getClassType(Integer.MAX_VALUE), Integer.MAX_VALUE, V_INT_BOX, Integer.MAX_VALUE),
            Arguments.of(getClassType(Short.MAX_VALUE), Short.MAX_VALUE, V_SHORT_BOX, Short.MAX_VALUE),
            Arguments.of(getClassType(Long.MAX_VALUE), Long.MAX_VALUE, V_LONG_BOX, Long.MAX_VALUE),
            Arguments.of(getClassType(Float.MAX_VALUE), Float.MAX_VALUE, V_FLOAT_BOX, (double) Float.MAX_VALUE),
            Arguments.of(getClassType(Double.MAX_VALUE), Double.MAX_VALUE, V_DOUBLE_BOX, Double.MAX_VALUE),
            Arguments.of(getClassType("String"), "String", V_STRING, "String"),
            Arguments.of(getClassType(exampleClass), exampleClass, V_OBJECT, ByteBuffer.wrap(serialize(exampleClass))),

            Arguments.of(
                getClassType(listWithStrings),
                listWithStrings,
                V_LIST,
                new ArrayList() {{
                    add(new Variant(V_STRING, "1"));
                    add(new Variant(V_STRING, "2"));
                }}),

            Arguments.of(
                getClassType(mapStringInteger),
                mapStringInteger,
                V_MAP,
                new HashMap() {{
                    put(new Variant(V_STRING, "one"), new Variant(V_INT_BOX, 1));
                }}),

            Arguments.of(
                getClassType(stringSet),
                stringSet,
                V_SET,
                new HashSet() {{
                    add(new Variant(V_STRING, "object"));
                }}),

            Arguments.of(
                getClassType(objectsArray),
                objectsArray,
                V_ARRAY,
                new ArrayList() {{
                    add(new Variant(V_NULL, "general.tests.TestHelper$ExampleClass"));
                    add(new Variant(V_OBJECT, new object(getClassType(exampleClass), ByteBuffer.wrap(serialize(exampleClass)))));
                }}
            ));
    }


    @ParameterizedTest
    @MethodSource("provideObjectsForMapping")
    public void objectsMap(String inputClassName, Object inputObject, Variant._Fields variantType, Object variantValue) {
        Variant variant = VariantConverter.toVariant(inputClassName, inputObject);

        assertEquals(variantType, variant.getSetField());
        if (variantType == V_OBJECT) {
            assertEquals(inputClassName, variant.getV_object().nameClass);
            assertEquals(variantValue, variant.getV_object().instance);
        } else {
            assertEquals(variantValue, variant.getFieldValue());
        }

        if (inputObject != null && inputObject.getClass().isArray()) {
            Object[] convertedObjects = (Object[]) VariantConverter.toObject(variant);
            assertArrayEquals((Object[]) inputObject, convertedObjects);
        } else {
            Object convertedObject = VariantConverter.toObject(variant);
            assertEquals(inputObject, convertedObject);
        }

    }

    @Test
    public void primitiveMap() {
        Variant variantValue = VariantConverter.toVariant(int.class.getName(), Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, variantValue.getV_int());

        variantValue = VariantConverter.toVariant(short.class.getName(), Short.MAX_VALUE);
        assertEquals(Short.MAX_VALUE, variantValue.getV_short());

        variantValue = VariantConverter.toVariant(long.class.getName(), Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, variantValue.getV_long());

        variantValue = VariantConverter.toVariant(float.class.getName(), Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, variantValue.getV_float());

        variantValue = VariantConverter.toVariant(double.class.getName(), Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, variantValue.getV_double());

        variantValue = VariantConverter.toVariant(byte.class.getName(), Byte.MAX_VALUE);
        assertEquals(Byte.MAX_VALUE, variantValue.getV_byte());

    }

    @Test
    public void arrayMapTest() {
        Variant variant = VariantConverter.toVariant(getClassType(intsArray), intsArray);

        Object[] convertedArray = (Object[]) VariantConverter.toObject(variant);
        assertEquals(V_ARRAY, variant.getSetField());
        assertEquals(V_INT, variant.getV_array().get(0).getSetField());
        assertEquals(intsArray[0], convertedArray[0]);

        variant = VariantConverter.toVariant(getClassType(integersArray), integersArray);

        convertedArray = (Object[]) VariantConverter.toObject(variant);
        assertEquals(V_ARRAY, variant.getSetField());
        assertEquals(V_INT_BOX, variant.getV_array().get(0).getSetField());
        assertEquals(integersArray[0], convertedArray[0]);
    }
}
