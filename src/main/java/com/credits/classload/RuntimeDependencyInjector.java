package com.credits.classload;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RuntimeDependencyInjector {

    @Resource
    private AutowiredAnnotationBeanPostProcessor bpp;

    public <T> T bind(T instance) {
        bpp.processInjection(instance);
        return instance;
    }
}
