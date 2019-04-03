package com.credits.wallet.desktop.struct;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public class MethodData implements Serializable {
    private static final long serialVersionUID = 8003127501538287437L;
    final Method method;

    public MethodData(Method method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodData)) {
            return false;
        }
        MethodData that = (MethodData) o;
        return Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method);
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return method.getReturnType().getSimpleName()
            + " "
            + method.getName()
            + " ("
            + Arrays.stream(method.getParameters())
            .map(p -> p.getType().getSimpleName() + " " + p.getName())
            .collect(joining(", "))
            + ")";
    }
}
