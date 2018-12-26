package com.credits.general.util.variant;

import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.generated.Variant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.credits.general.util.variant.VariantUtils.COLLECTION_VALUES_DELIMITER;
import static com.credits.general.util.variant.VariantUtils.MAP_KEY_VALUE_DELIMITER;

@RunWith(Parameterized.class)
public class VariantConverterTest {

    @Parameterized.Parameter
    public String classname;

    @Parameterized.Parameter(1)
    public String value;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"Boolean", ""},
                {"boolean", null},
                {"boolean", "false"},
                {"Boolean", "true"},
                {"Byte", "12"},
                {"byte", "13"},
                {"Short", "12"},
                {"short", "13"},
                {"Integer", "14"},
                {"int", "15"},
                {"Long", "999999999999999"},
                {"long", "1000000000000001"},
                {"Float", "999.99"},
                {"float", "10001.01"},
                {"Double", "9999999999.99"},
                {"double", "10000000001.01"},
                {"Double[]", String.format("1%s2", COLLECTION_VALUES_DELIMITER)},
                {"Set<Integer>", String.format("3%s4", COLLECTION_VALUES_DELIMITER)},
                {"List<Float>", String.format("5.3%s6.4", COLLECTION_VALUES_DELIMITER)},
                {"Map<String, String>", String.format(
                        "7%s8%s9%s10",
                        MAP_KEY_VALUE_DELIMITER,
                        COLLECTION_VALUES_DELIMITER,
                        MAP_KEY_VALUE_DELIMITER
                )},
        });
    }

    @Test
    public void test() {
        VariantData variantDataIn = VariantUtils.createVariantData(classname, value);
        Variant variant = VariantConverter.variantDataToVariant(variantDataIn);
        VariantData variantDataOut = VariantConverter.variantToVariantData(variant);
        Assert.assertEquals(variantDataIn, variantDataOut);
    }
}