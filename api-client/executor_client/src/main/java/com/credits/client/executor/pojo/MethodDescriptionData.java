package com.credits.thrift;

import com.credits.client.executor.thrift.generated.MethodArgument;

import java.util.List;

/**
 * Created by Igor Goryunov on 08.10.2018
 */
public class MethodDescriptionData {
    public final String returnType;
    public final  String name;
    public final List<MethodArgument> args;

    public MethodDescriptionData(String returnType, String name,  List<MethodArgument> args) {
        this.name = name;
        this.args = args;
        this.returnType = returnType;
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

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (args != null ? !args.equals(that.args) : that.args != null) {
            return false;
        }
        return returnType != null ? returnType.equals(that.returnType) : that.returnType == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (args != null ? args.hashCode() : 0);
        result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MethodDescriptionData{" + "name='" + name + '\'' + ", args=" + args + ", returnType='" + returnType + '\'' + '}';
    }

}
