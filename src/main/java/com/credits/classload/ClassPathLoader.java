package com.credits.classload;

import com.credits.exception.ClassLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLClassLoader;

@Component
public class ClassPathLoader {

    private final static Logger logger = LoggerFactory.getLogger(ClassPathLoader.class);

    public Class<?> loadClass(URL newClassPathRoot, String className) throws ClassLoadException {
        logger.debug("Loading class {} from {}", newClassPathRoot.getFile(), className);
        Class<?> clazz;
        try {
            URL[] urls = {newClassPathRoot};
            ClassLoader classLoader = new URLClassLoader(urls);
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ClassLoadException("Failed to load class. " + e.getMessage(), e);
        }

        logger.debug("Class has been successfully loaded", newClassPathRoot.getFile(), className);
        return clazz;
    }

}