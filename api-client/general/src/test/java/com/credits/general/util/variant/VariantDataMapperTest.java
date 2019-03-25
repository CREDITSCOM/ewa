package com.credits.general.util.variant;

import com.credits.general.pojo.VariantData;
import com.credits.general.pojo.VariantType;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Deprecated
@RunWith(Parameterized.class)
public class VariantDataMapperTest {

    @Parameter
    public String name;

    @Parameter(1)
    public VariantData input;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"Boolean", VariantUtils.createVariantData("Boolean", "true")},
            {"boolean", VariantUtils.createVariantData("boolean", "false")},
            {"Byte", VariantUtils.createVariantData("Byte", "1")},
            {"byte", VariantUtils.createVariantData("byte", "2")},
            {"Short", VariantUtils.createVariantData("Short", "2")},
            {"short", VariantUtils.createVariantData("short", "3")},
            {"Integer", VariantUtils.createVariantData("Integer", "3")},
            {"int", VariantUtils.createVariantData("int", "4")},
            {"Long", VariantUtils.createVariantData("Long", "4")},
            {"long", VariantUtils.createVariantData("long", "5")},
            {"Float", VariantUtils.createVariantData("Float", "1.1")},
            {"float", VariantUtils.createVariantData("float", "1.2")},
            {"Double", VariantUtils.createVariantData("Double", "1.1")},
            {"double", VariantUtils.createVariantData("double", "1.2")},
            {"String", VariantUtils.createVariantData("String", "test string")},
            {"Array", VariantUtils.createVariantData("int[]", String.format("4%s5", VariantUtils.COLLECTION_VALUES_DELIMITER))},
            {"List", VariantUtils.createVariantData("List<Short>", String.format("2%s3", VariantUtils.COLLECTION_VALUES_DELIMITER))},
            {"Map", VariantUtils.createVariantData("Map<String, String>", String.format(
                    "1%s2%s3%s4",
                    VariantUtils.MAP_KEY_VALUE_DELIMITER,
                    VariantUtils.COLLECTION_VALUES_DELIMITER,
                    VariantUtils.MAP_KEY_VALUE_DELIMITER
            ))},
            {"Set", VariantUtils.createVariantData("Set<Double>", String.format("5%s6", VariantUtils.COLLECTION_VALUES_DELIMITER))},
        });
    }

    @Test
    public void mapSuccessfulTest() {
        testVariantData(input);
    }

    private void testVariantData(VariantData variantData) {

        VariantType variantType = variantData.getVariantType();
        Object boxedValue = variantData.getBoxedValue();

        switch (variantType) {
            case ARRAY:
                VariantData[] variantDataArr = (VariantData[]) boxedValue;
                Arrays.asList(variantDataArr).forEach(this::testVariantData);
                break;
            case LIST:
                List<VariantData> variantDataList = (List<VariantData>)boxedValue;
                variantDataList.forEach(this::testVariantData);
                break;
            case SET:
                Set<VariantData> variantDataSet = (Set<VariantData>)boxedValue;
                variantDataSet.forEach(this::testVariantData);
                break;
            case MAP:
                Map<VariantData, VariantData> variantDataMap = (Map<VariantData, VariantData>)boxedValue;
                variantDataMap.forEach((key, value) -> {
                    testVariantData(key);
                    testVariantData(value);
                });
                break;
            case FLOAT: // in Variant struct Float is stored as Double
                testVariantData(VariantUtils.createVariantData("double", GeneralConverter.toString(variantData.getBoxedValue())));
                break;
            case FLOAT_BOX:
                testVariantData(VariantUtils.createVariantData("Double", GeneralConverter.toString(variantData.getBoxedValue())));
                break;
            default:
                Variant variant = new VariantDataMapper()
                        .apply(variantData)
                        .orElse(new Variant());
                Assert.assertEquals(boxedValue, variant.getFieldValue());
        }
    }
}
