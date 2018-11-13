package com.credits.wallet.desktop.utils.sourcecode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class JavaReflect {

    public static Map<Method, String> getDeclaredMethods(Class clazz) {
        Map<Method, String> res = new HashMap<>();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            StringBuilder methodString = new StringBuilder();
            methodString.append(method.getReturnType().equals(Void.class) ? "void" : method.getReturnType().getSimpleName());

            methodString.append(" ")
                    .append(method.getName())
                    .append("(");

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                methodString.append(parameter.getType().getSimpleName())
                        .append(" ")
                        .append(parameter.getName());
                if (i < parameters.length - 1) {
                    methodString.append(", ");
                }
            }

            methodString.append(")");

            if (method.getExceptionTypes().length > 0) {
                methodString.append(" throws ")
                        .append(Arrays.stream(method.getExceptionTypes()).map(Class::getSimpleName).collect(Collectors.joining(", ")));
            }
            res.put(method, methodString.toString());
        }
        return res;
    }

    public static Map<Field, String> getDeclaredFields(Class clazz) {
        Map<Field, String> res = new HashMap<>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            StringBuilder fieldString = new StringBuilder();
            fieldString.append(field.getType().getSimpleName());

            fieldString.append(" ")
                    .append(field.getName());

            res.put(field, fieldString.toString());
        }
        return res;
    }

}