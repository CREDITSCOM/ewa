package com.credits.general.pojo;

import com.credits.general.util.variant.VariantUtils;

import java.util.Arrays;
import java.util.Objects;

import static com.credits.general.pojo.VariantType.ARRAY;

@Deprecated
public class VariantData {
    private VariantType variantType;
    private Object boxedValue;

    public VariantData(VariantType variantType, Object boxedValue) {
        VariantUtils.validateVariantData(variantType, boxedValue);
        this.setVariantType(variantType);
        this.setBoxedValue(boxedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantType, boxedValue);
    }

    @Override
    public String toString() {
        return String.format("value: %s; type: %s", boxedValue, variantType);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof VariantData)) {
            return false;
        }
        VariantData variantData = (VariantData)object;
        VariantType variantType = variantData.getVariantType();
        Object boxedValue = variantData.getBoxedValue();
        VariantType thisVariantType = this.getVariantType();
        Object thisBoxedValue = this.getBoxedValue();

        if (thisVariantType != variantType) {
            return false;
        }
        if (thisBoxedValue == null) {
            return boxedValue == null;
        }
        if (variantType == ARRAY) {
            VariantData[] variantDataArr = (VariantData[]) boxedValue;
            VariantData[] thisVariantDataArr = (VariantData[]) thisBoxedValue;
            return Arrays.equals(variantDataArr, thisVariantDataArr);
        } else {
            return thisBoxedValue.equals(boxedValue);
        }

//        switch (variantType) {
//            case ARRAY:
//                VariantData[] variantDataArr = (VariantData[]) boxedValue;
//                VariantData[] thisVariantDataArr = (VariantData[]) thisBoxedValue;
//                return Arrays.equals(variantDataArr, thisVariantDataArr);
//            case LIST:
//                List<VariantData> variantDataList = (List<VariantData>)boxedValue;
//                List<VariantData> thisVariantDataList = (List<VariantData>)thisBoxedValue;
//                return thisVariantDataList.equals(variantDataList);
//            case SET:
//                Set<VariantData> variantDataSet = (Set<VariantData>)boxedValue;
//                Set<VariantData> thisVariantDataSet = (Set<VariantData>)thisBoxedValue;
//                return thisVariantDataSet.equals(variantDataSet);
//            case MAP:
//                Map<VariantData, VariantData> variantDataMap = (Map<VariantData, VariantData>)boxedValue;
//                Map<VariantData, VariantData> thisVariantDataMap = (Map<VariantData, VariantData>)thisBoxedValue;
//                return thisVariantDataMap.equals(variantDataMap);
//            default:
//                return thisBoxedValue.equals(boxedValue);
//        }
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public void setVariantType(VariantType variantType) {
        this.variantType = variantType;
    }

    public Object getBoxedValue() {
        return boxedValue;
    }

    public void setBoxedValue(Object boxedValue) {
        this.boxedValue = boxedValue;
    }
}
