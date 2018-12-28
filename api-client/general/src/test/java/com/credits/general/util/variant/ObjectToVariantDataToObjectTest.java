package com.credits.general.util.variant;

import com.credits.general.pojo.VariantData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

@RunWith(Parameterized.class)
public class ObjectToVariantDataToObjectTest {

    @Parameterized.Parameter
    public Object object;

    @Parameterized.Parameters(name = "{0}")
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
                }}
        );
    }

    @Test
    public void test() {
        VariantData variantData = VariantConverter.objectToVariantData(object);
        Object objectOut = VariantConverter.variantDataToObject(variantData);
        Assert.assertEquals(object, objectOut);
    }
}