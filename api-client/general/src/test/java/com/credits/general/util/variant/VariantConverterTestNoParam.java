package com.credits.general.util.variant;

import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.ClassObjectData;
import com.credits.general.pojo.VariantData;
import com.credits.general.pojo.VariantType;
import com.credits.general.thrift.generated.Variant;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class VariantConverterTestNoParam {

    @Test
    public void testClassObject() {
        VariantData variantDataIn = new VariantData(
                VariantType.OBJECT,
                new ClassObjectData(new ArrayList<ByteCodeObjectData>(){{
                    add(new ByteCodeObjectData("name01", new byte[2]));
                    add(new ByteCodeObjectData("name02", new byte[3]));
                }}, new byte[1])
        );
        Variant variant = VariantConverter.variantDataToVariant(variantDataIn);
        VariantData variantDataOut = VariantConverter.variantToVariantData(variant);
        Assert.assertEquals(variantDataIn, variantDataOut);
    }

    @Test
    public void testVoid() {
        VariantData variantDataIn = new VariantData(
                VariantType.VOID, null
        );
        Variant variant = VariantConverter.variantDataToVariant(variantDataIn);
        VariantData variantDataOut = VariantConverter.variantToVariantData(variant);
        Assert.assertEquals(variantDataIn, variantDataOut);
    }
}