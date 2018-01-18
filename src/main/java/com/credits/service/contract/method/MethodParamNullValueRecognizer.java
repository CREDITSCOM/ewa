package com.credits.service.contract.method;

public class MethodParamNullValueRecognizer extends MethodParamValueRecognizer {
    public MethodParamNullValueRecognizer(String param) {
        super(param);
    }

    @Override
    public Object castValue(Class<?> type) {
        Object retVal = type.cast(null);
        return retVal;
    }
}
