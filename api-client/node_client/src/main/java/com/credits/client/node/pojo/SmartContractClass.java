package com.credits.client.node.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class SmartContractClass implements Serializable {
    private static final long serialVersionUID = -380177117218351553L;

    private final Class<?> rootClass;
    private final List<Class<?>> innerClasses;

    public SmartContractClass(Class<?> rootClass, List<Class<?>> innerClasses) {
        this.rootClass = rootClass;
        this.innerClasses = innerClasses;
    }

    public Class<?> getRootClass() {
        return rootClass;
    }

    public List<Class<?>> getInnerClasses() {
        return innerClasses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SmartContractClass)) {
            return false;
        }
        SmartContractClass that = (SmartContractClass) o;
        return Objects.equals(rootClass, that.rootClass) &&
            Objects.equals(innerClasses, that.innerClasses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootClass, innerClasses);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SmartContractClass{");
        sb.append("rootClass=").append(rootClass);
        sb.append(", innerClasses=").append(innerClasses);
        sb.append('}');
        return sb.toString();
    }
}
