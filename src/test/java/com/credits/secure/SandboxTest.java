package com.credits.secure;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.AccessControlException;
import java.security.AllPermission;
import java.security.Permissions;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SandboxTest {

    @Test(expected = AccessControlException.class)
    public void confineTest() throws IOException {
        UnsafeClass unsafeClass = new UnsafeClass();
        try {
            unsafeClass.openSocket(123);
        } catch (SecurityException ignored) {
            fail();
        }

        Permissions permissions = new Permissions();
        Sandbox.confine(unsafeClass.getClass(), permissions);
        unsafeClass.openSocket(123);
    }

    @Test(expected = SecurityException.class)
    public void getPermission() {
        UnsafeClass unsafeClass = new UnsafeClass();
        Sandbox.confine(unsafeClass.getClass(), new Permissions());
        Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        Sandbox.confine(unsafeClass.getClass(), permissions);
    }

    @Test(expected = NoClassDefFoundError.class)
    public void callChildMethod() throws Exception {
        UnsafeClass unsafeClass = new UnsafeClass();
        Sandbox.confine(unsafeClass.getClass(), new Permissions());
        unsafeClass.callChildMethod();
    }

    @Test
    public void threadSafetyTest() throws InterruptedException {
        int amountThreads = 1000;
        CountDownLatch count = new CountDownLatch(amountThreads);
        UnsafeClass unsafeClass = new UnsafeClass();
        Sandbox.confine(unsafeClass.getClass(), new Permissions());

        IntStream.range(0, amountThreads).mapToObj(i -> new Thread(() -> {
            try {
                unsafeClass.openSocket(1000 + i);
            } catch (AccessControlException e) {
                count.countDown();
            } catch (Exception ignored) {
            }
        })).forEach(Thread::start);

        count.await(1, TimeUnit.SECONDS);
        assertEquals(0, count.getCount());
    }


    static class UnsafeClass {

        public void openSocket(int port) throws IOException {
            System.out.println("opening socket...");
            new ServerSocket(port);
            System.out.println("success");
        }

        public void callChildMethod() throws Exception {
            new Child().foo();
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