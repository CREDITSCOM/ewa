package com.credits.wallet.desktop.utils.sourcecode;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;


public class JavaReflectTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaReflectTest.class);

    @Test
    public void getSignatureTest01() {
        try {
            Class<?> clazz = Class.forName("com.credits.scapi.v0.SmartContract");
            Map<Method, String> methods = JavaReflect.getDeclaredMethods(clazz);
            methods.forEach((k, v) -> {
                LOGGER.info(v);
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
