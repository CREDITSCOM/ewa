package com.credits.general.util.variant;

import com.credits.general.pojo.VariantData;
import com.credits.general.pojo.VariantType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class ObjectToVariantDataTest {

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
        VariantType variantType = variantData.getVariantType();
        Object boxedValue = variantData.getBoxedValue();
        switch(variantType) {
            case LIST:
                List<VariantData> variantDataList = (List<VariantData>) boxedValue;
                List<Object> objectList =
                        variantDataList.stream().map(
                                variantDataElem -> {
                                    return variantDataElem.getBoxedValue();
                                }).collect(Collectors.toList());
                Assert.assertEquals(object, objectList);
                break;
            case SET:
                Set<VariantData> variantDataSet = (Set<VariantData>) boxedValue;
                Set<Object> objectSet =
                        variantDataSet.stream().map(
                                variantDataElem -> {
                                    return variantDataElem.getBoxedValue();
                                }).collect(Collectors.toSet());
                Assert.assertEquals(object, objectSet);
                break;
            case MAP:
                Map<VariantData, VariantData> variantDataMap = (Map<VariantData, VariantData>) boxedValue;
                Map<Object, Object> objectMap =
                        variantDataMap.entrySet().stream()
                                .collect(Collectors.toMap(
                                        e -> e.getKey().getBoxedValue(),
                                        e -> e.getValue().getBoxedValue()
                                ));
                Assert.assertEquals(object, objectMap);
                break;
            default:
                Assert.assertEquals(object, variantData.getBoxedValue());
        }

    }
}