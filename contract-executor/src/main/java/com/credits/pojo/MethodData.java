package com.credits.pojo;

import com.credits.general.thrift.generated.Variant;

import java.lang.reflect.Method;

public class MethodData {

    public final Method method;
    public final Class<?>[] argTypes;
    public final Variant[] argValues;

    public MethodData(Method methodName, Class<?>[] argTypes, Variant[] argValues) {
        this.method = methodName;
        this.argTypes = argTypes;
        this.argValues = argValues;
    }
}
