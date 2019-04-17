package com.credits.wallet.desktop.utils.sourcecode;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

        Field[] fields = FieldUtils.getAllFieldsList(clazz).stream().filter(f -> Modifier.isProtected(f.getModifiers())).toArray(Field[]::new);
        for (Field field : fields) {

            String fieldString = field.getType().getSimpleName() + " " + field.getName();
            res.put(field, fieldString);
        }
        return res;
    }

}