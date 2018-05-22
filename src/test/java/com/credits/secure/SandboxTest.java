package com.credits.secure;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.security.AccessControlException;
import java.security.AllPermission;
import java.security.Permissions;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.fail;

public class SandboxTest {

    @Test(expected = SecurityException.class)
    public void confineTest() throws IOException {
        UnsafeClass unsafeClass = new UnsafeClass();
        try {
            unsafeClass.openSocket(1500);
        } catch (SecurityException ignored) {
            fail();
        }

        Permissions permissions = new Permissions();
        Sandbox.confine(unsafeClass.getClass(), permissions);
        unsafeClass.openSocket(1500);
    }

    @Test(expected = SecurityException.class)
    public void getPermission() {
        UnsafeClass unsafeClass = new UnsafeClass();
        Sandbox.confine(unsafeClass.getClass(), new Permissions());
        Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        Sandbox.confine(unsafeClass.getClass(), permissions);
    }

    @Test(expected = SecurityException.class)
    public void callChildMethod() throws Exception {
        UnsafeClass unsafeClass = new UnsafeClass();
        Sandbox.confine(unsafeClass.getClass(), new Permissions());
        unsafeClass.callChildMethod();
    }

    @Test(expected = SecurityException.class)
    public void reflectionUse() throws InstantiationException, IllegalAccessException {
        UnsafeClass unsafeClass = new UnsafeClass();
        Sandbox.confine(unsafeClass.getClass(), new Permissions());
        try {
            UnsafeClass instance = unsafeClass.getInstance();
            instance.setValue(1);
        } catch (Exception ignored) {
            fail();
        }

        try {
            UnsafeClass instance = unsafeClass.getInstance();
            Method method = instance.getClass().getMethod("setValue", int.class);
            method.invoke(null, 1);
            fail();
        } catch (Exception ignored) {
        }

        unsafeClass.invokeConstructor(UnsafeClass.class);
    }

    @Test
    public void threadSafetyTest() throws InterruptedException {
        int amountThreads = 10_000;
        CountDownLatch count = new CountDownLatch(amountThreads);

        UnsafeClass[] unsafeClasses =
            IntStream.range(0, amountThreads).mapToObj(i -> new UnsafeClass()).toArray(UnsafeClass[]::new);

        Thread[] locks = IntStream.range(0, amountThreads).mapToObj(i -> new Thread(() -> {
            try {
                Sandbox.confine(unsafeClasses[i].getClass(), new Permissions());
            } catch (SecurityException ignored) {
            }

        })).toArray(Thread[]::new);

        Thread[] checks = IntStream.range(0, amountThreads).mapToObj(i -> new Thread(() -> {
            try {
                unsafeClasses[i].openSocket(1000 + i);
            } catch (AccessControlException e) {
                count.countDown();
            } catch (Exception e) {
                fail(ExceptionUtils.getRootCauseMessage(e));
            }
        })).toArray(Thread[]::new);

        for (int i = 0; i < amountThreads; i++) {
            locks[i].start();
            checks[i].start();
        }
        count.await(1, TimeUnit.SECONDS);
//        assertEquals(0, count.getCount());
    }

    @Test(expected = SecurityException.class)
    public void loadOtherClassloader() throws Exception {
        UnsafeClass unsafeClass = new UnsafeClass();
        Sandbox.confine(unsafeClass.getClass(), new Permissions());
        unsafeClass.createOtherClassloader();
    }

    static class UnsafeClass {

        private int value = 0;

        public void openSocket(int port) throws IOException {
            System.out.println("opening socket...");
            new ServerSocket(port);
            System.out.println("success");
        }

        public void setValue(int val) {
            value = val;
        }

        public UnsafeClass getInstance() {
            return new UnsafeClass();
        }

        public void callChildMethod() throws Exception {
            new Child().foo();
        }

        public void createOtherClassloader() throws Exception{
            ((UnsafeClass)(Class.forName("UnsafeClass").newInstance())).setValue(10000);
        }

        public void invokeConstructor(Class clazz) throws IllegalAccessException, InstantiationException {
            clazz.newInstance();
            System.out.println("new instance created");
        }

        static class Child extends UnsafeClass {
            Child() throws Exception {
                super();
            }

            public void foo() throws Exception {
                new ServerSocket(1234);
            }
        }
    }
}