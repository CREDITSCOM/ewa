package com.credits.pojo;

import com.credits.general.thrift.generated.Variant;

import java.lang.reflect.Method;

public class MethodArgumentsValuesData {

    final Method method;
    final Class<?>[] argTypes;
    final Variant[] argValues;

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public Variant[] getArgValues() {
        return argValues;
    }

    public MethodArgumentsValuesData(Method methodName, Class<?>[] argTypes, Variant[] argValues) {
        this.method = methodName;
        this.argTypes = argTypes;
        this.argValues = argValues;
    }
}
