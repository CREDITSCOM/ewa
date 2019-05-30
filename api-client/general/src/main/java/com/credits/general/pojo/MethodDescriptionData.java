package com.credits.general.pojo;

import java.util.List;


public class MethodDescriptionData {
    public final String returnType;
    public final  String name;
    public final List<MethodArgumentData> args;
    public List<AnnotationData> annotations;

    public MethodDescriptionData(String returnType, String name,  List<MethodArgumentData> args, List<AnnotationData> annotationData) {
        this.name = name;
        this.args = args;
        this.returnType = returnType;
        this.annotations = annotationData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodDescriptionData that = (MethodDescriptionData) o;

        if (returnType != null ? !returnType.equals(that.returnType) : that.returnType != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (args != null ? !args.equals(that.args) : that.args != null) {
            return false;
        }
        return annotations != null ? annotations.equals(that.annotations) : that.annotations == null;
    }

    @Override
    public int hashCode() {
        int result = returnType != null ? returnType.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (args != null ? args.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MethodDescriptionData{" + "returnType='" + returnType + '\'' + ", name='" + name + '\'' + ", args=" +
            args + ", annotations=" + annotations + '}';
    }
}
