package com.credits.service.contract.method;

public class MethodParamBooleanValueRecognizer extends MethodParamValueRecognizer {
    public MethodParamBooleanValueRecognizer(String param) {
        super(param);
    }

    @Override
    public Object castValue(Class<?> type) {
        Boolean retVal = Boolean.valueOf(param);

        if (type.isPrimitive() && Boolean.TYPE.equals(type)) {
            return retVal.booleanValue();
        } else {
            return type.cast(param);
        }
    }
}
