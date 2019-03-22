package com.credits.general.util.variant;

import com.credits.general.thrift.generated.ClassObject;
import com.credits.general.thrift.generated.Variant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ObjectMapperTest {

    @Parameter
    public String testName;

    @Parameter(1)
    public Object input;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"Boolean", ""},
            {"Boolean", true},
            {"Byte", (byte)1},
            {"Short", (short)2},
            {"Integer", 3},
            {"Long", 4L},
            {"Float", 5},
            {"Double", 1.1D},
            {"String", "test string"},
            {VariantUtils.OBJECT_TYPE, new ClassObject()},
        });
    }

    @Test
    public void mapSuccessfulTest() {
        Variant variant = new ObjectMapper()
            .apply(input)
            .orElse(new Variant());

        Assert.assertEquals(input, variant.getFieldValue());
    }
}
