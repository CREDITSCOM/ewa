package pojo;

import com.credits.general.thrift.generated.Variant;

import java.util.List;
import java.util.Objects;

public class MethodHeaderData {
    final String methodName;
    final List<Variant> params;

    public MethodHeaderData(String methodName, List<Variant> params) {
        this.methodName = methodName;
        this.params = params;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Variant> getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodHeaderData that = (MethodHeaderData) o;
        return Objects.equals(methodName, that.methodName) &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, params);
    }

    @Override
    public String toString() {
        return "MethodHeaderData{" +
                "methodName='" + methodName + '\'' +
                ", params=" + params +
                '}';
    }
}
