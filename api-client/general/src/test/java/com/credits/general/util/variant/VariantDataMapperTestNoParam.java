package com.credits.general.util.variant;

import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.ClassObjectData;
import com.credits.general.pojo.VariantData;
import com.credits.general.pojo.VariantType;
import com.credits.general.thrift.generated.ClassObject;
import com.credits.general.thrift.generated.Variant;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static com.credits.general.util.GeneralPojoConverter.createClassObjectData;

public class VariantDataMapperTestNoParam {

    @Test
    public void testClassObject() {
        VariantData variantData = new VariantData(
                VariantType.OBJECT,
                new ClassObjectData(new ArrayList<ByteCodeObjectData>(){{
                    add(new ByteCodeObjectData("name01", new byte[2]));
                    add(new ByteCodeObjectData("name02", new byte[3]));
                }}, new byte[1])
        );

        Object boxedValue = variantData.getBoxedValue();

        Variant variant = new VariantDataMapper()
                .apply(variantData)
                .orElse(new Variant());
        Assert.assertEquals(boxedValue, createClassObjectData((ClassObject)variant.getFieldValue()));
    }

    @Test
    public void testVoid() {
        VariantData variantData = new VariantData(
                VariantType.VOID, null
        );
        Variant variant = new VariantDataMapper()
                .apply(variantData)
                .orElse(new Variant());
        Assert.assertEquals(VariantUtils.VOID_TYPE_VALUE, variant.getFieldValue());
    }
}
