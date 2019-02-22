package com.credits.general.pojo;

import java.util.List;

public class MethodArgumentData {
    public String returnType;
    public String name;
    public List<AnnotationData> annotations;

    public MethodArgumentData(String typeName, String name, List<AnnotationData> annotationData) {
        this.annotations = annotationData;
        this.name = name;
        this.returnType = typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodArgumentData that = (MethodArgumentData) o;

        if (returnType != null ? !returnType.equals(that.returnType) : that.returnType != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return annotations != null ? annotations.equals(that.annotations) : that.annotations == null;
    }

    @Override
    public int hashCode() {
        int result = returnType != null ? returnType.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        return result;
    }
}
