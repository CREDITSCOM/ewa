package com.credits.pojo;

import com.credits.general.thrift.generated.Variant;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodData {

    public final Method method;
    public final Class<?>[] argTypes;
    public final Variant[] argValues;

    public MethodData(Method methodName, Class<?>[] argTypes, Variant[] argValues) {
        this.method = methodName;
        this.argTypes = argTypes;
        this.argValues = argValues;
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

        if (method != null ? !method.equals(that.method) : that.method != null) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(argTypes, that.argTypes)) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(argValues, that.argValues);
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(argTypes);
        result = 31 * result + Arrays.hashCode(argValues);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MethodData{");
        sb.append("method=").append(method);
        sb.append(", argTypes=").append(Arrays.toString(argTypes));
        sb.append(", argValues=").append(Arrays.toString(argValues));
        sb.append('}');
        return sb.toString();
    }
}
