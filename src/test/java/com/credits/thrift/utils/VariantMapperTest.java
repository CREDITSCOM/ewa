package com.credits.thrift.utils;

import com.credits.general.thrift.generate.Variant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class VariantMapperTest {

    @Parameter
    public String name;

    @Parameter(1)
    public Object input;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"Boolean", true},
            {"Byte", (byte) 1},
            {"Short", (short) 2},
            {"Integer", 3},
            {"Long", 4L},
            {"Double", 1.1D},
            {"String", "test string"},
        });
    }

    @Test
    public void mapSuccessfulTest() {
        Variant variant = new VariantMapper()
            .apply(input)
            .orElse(new Variant());

        Assert.assertEquals(input, variant.getFieldValue());
    }
}
