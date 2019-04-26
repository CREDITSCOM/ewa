package tests.credits.secure;

import com.credits.secure.Sandbox;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FilePermission;
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


@Ignore
public class SandboxTest {

    @Test
    public void confineTest() throws IOException {
        UnsafeClass unsafeClass = new UnsafeClass();

        unsafeClass.openSocket(1500);
        Permissions permissions = new Permissions();
        Sandbox.confine(unsafeClass.getClass(), permissions);

        try {
            unsafeClass.openSocket(1500);
        } catch (SecurityException ignored) {
            return;
        }
        fail("UnsafeClass can use socket");
    }

    @Test
    public void getPermission() {
        UnsafeClass unsafeClass = new UnsafeClass();

        Sandbox.confine(unsafeClass.getClass(), new Permissions());
        unsafeClass.setValue(10);
        try {
            unsafeClass.addMorePermissions();
        } catch (SecurityException e) {
            return;
        }
        fail("UnsafeClass add yourself permissions");
    }

    @Test
    public void callChildMethod() throws Exception {
        String appPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

        UnsafeClass unsafeClass = new UnsafeClass();

        Permissions permissions = new Permissions();
        permissions.add(new FilePermission(appPath + "\\-", "read"));
        Sandbox.confine(unsafeClass.getClass(), permissions);

        unsafeClass.callChildMethod();
    }

    @Test(expected = SecurityException.class)
    public void reflectionUse() throws InstantiationException, IllegalAccessException {
        UnsafeClass unsafeClass = new UnsafeClass();
        Sandbox.confine(unsafeClass.getClass(), new Permissions());

        UnsafeClass instance = null;
        try {
            instance = unsafeClass.createInstance();
            instance.setValue(1);
        } catch (Exception ignored) {
            fail();
        }

        try {
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
                //                new Sandbox().confine(unsafeClasses[i].getClass(), new Permissions());
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

    @Test
    public void test() throws Exception {
        class SomeExternalClassLodaer extends ClassLoader {
            @Override
            protected Class<?> findClass(String s) throws ClassNotFoundException {
                return super.findClass(s);
            }
        }
        System.out.println(getClass().getClassLoader());
        UnsafeClass privelegedClass = (UnsafeClass) Class.forName("com.credits.secure.SandboxTest$UnsafeClass", false,
                                                                  new SomeExternalClassLodaer()).newInstance();
        //        new Sandbox().confine(privelegedClass.getClass(), new Permissions());
        System.out.println(privelegedClass.getClass().getClassLoader());
        privelegedClass.createInstance();
    }

    static class UnsafeClass {

        private int value = 0;

        public void openSocket(int port) throws IOException {
            System.out.println("opening socket...");
            new ServerSocket(port);
            System.out.println("success");
        }

        public void addMorePermissions() {
            Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            Sandbox.confine(UnsafeClass.class, permissions);
        }

        public void setValue(int val) {
            value = val;
        }

        public UnsafeClass createInstance() {
            return new UnsafeClass();
        }

        public void callChildMethod() throws Exception {
            new Child().foo();
        }

        public void invokeConstructor(Class clazz) throws IllegalAccessException, InstantiationException {
            clazz.newInstance();
            System.out.println("new instance created");
        }

    }

    static class Child extends UnsafeClass {
        public Child() {
            super();
        }

        public void foo() throws Exception {
            System.out.println("method foo invoked ");
        }

        public void openSocket() throws Exception {
            new ServerSocket(1500);
        }
    }
}