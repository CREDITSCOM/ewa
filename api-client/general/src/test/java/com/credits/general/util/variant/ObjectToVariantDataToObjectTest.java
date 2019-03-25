package com.credits.general.util.variant;

import com.credits.general.pojo.VariantData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ObjectToVariantDataToObjectTest {

    @Parameter
    public Object object;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object> data() {
        return Arrays.asList(
                null,
                Boolean.FALSE,
                (byte) 1,
                (short) 2,
                3,
                999999999999999L,
                (float)999.99,
                9999999999.99,
                new byte[]{0xB, 0xA, 0xB, 0xE},
                new int[]{1, 2, 3, 4},
                new short[]{1, 2, 3, 4},
                new long[]{1, 2, 3, 4},
                new float[]{1, 2, 3, 4},
                new double[]{1, 2, 3, 4},
                new Byte[]{0xB},
                new Integer[]{1},
                new Short[]{1},
                new Long[]{1L},
                new Float[]{1f},
                new Double[]{1d},
                new ArrayList<String>() {{
                    add("A");
                    add("B");
                    add("C");
                }},
                new ArrayList<List<Double>>() {{
                    add(
                        new ArrayList<Double>(){{
                            add(1.2);
                            add(3.4);
                        }}
                    );
                    add(
                        new ArrayList<Double>(){{
                            add(5.6);
                            add(7.8);
                        }}
                    );
                    add(
                        new ArrayList<Double>(){{
                            add(9.10);
                            add(11.12);
                        }}
                    );
                }},
                new HashSet<Float>() {{
                    add((float) 2.3);
                    add((float) 4.5);
                    add((float) 6.7);
                }},
                new HashMap<String, Integer>()
                {{
                    put("One", 1);
                    put("Two", 2);
                    put("Three", 3);
                }},
//                new ClassObjectData(new ArrayList<ByteCodeObjectData>(){{
//                    add(new ByteCodeObjectData("name01", new byte[3]));
//                    add(new ByteCodeObjectData("name02", new byte[2]));
//                }}, new byte[1]),
                new ObjectToVariantDataToObjectTest()
        );
    }

    @Test
    public void test() {
        VariantData variantData = VariantConverter.objectToVariantData(object);
        Object objectOut = VariantConverter.variantDataToObject(variantData);
        if (object != null && object.getClass().isArray()) {
            Assert.assertArrayEquals(new Object[] {object}, new Object[] {objectOut});
        } else {
            Assert.assertEquals(object, objectOut);
        }
    }
}