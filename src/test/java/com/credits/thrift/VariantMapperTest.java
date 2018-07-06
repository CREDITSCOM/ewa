package com.credits.thrift;

import org.junit.Assert;
import org.junit.Test;

public class VariantMapperTest {

    @Test
    public void mapTest() {
        byte b = 1;
        Variant variant = VariantMapper.map(b);
        Assert.assertEquals(b, variant.getV_i8());
    }
}
