package com.credits.classload;

import com.credits.exception.ClassLoadException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

public class ClassPathLoaderTest {

    ClassPathLoader loader;

    @Before
    public void setUp() {
        loader = new ClassPathLoader();
    }

    @Test
    public void loadClass() throws ClassLoadException {
        URL resource = getClass().getClassLoader().getResource("com/credits/classload/");
        String className = "MySmartContract";

        Class<?> clazz;
        try {
            clazz = loader.loadClass(resource, className);
        } catch (ClassLoadException e) {
            throw e;
        }

        try {
            Method mainMethod = clazz.getDeclaredMethod("main", String[].class);
            String[] params = null;
            mainMethod.invoke(null, (Object) params);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new ClassLoadException(e.getMessage(), e);
        }
    }
}
