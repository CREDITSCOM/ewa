package com.credits.general.pojo;

import java.util.HashMap;
import java.util.Map;

public class AnnotationData {
    public String name;
    public Map<String,String> arguments;

    public AnnotationData(AnnotationData annotationData1) {
        this.name = annotationData1.name;
        this.arguments = new HashMap<>(annotationData1.arguments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnnotationData that = (AnnotationData) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return arguments != null ? arguments.equals(that.arguments) : that.arguments == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        return result;
    }

    public AnnotationData(String name, Map<String, String> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

}
