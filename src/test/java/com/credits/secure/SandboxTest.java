package com.credits.secure;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.AccessControlException;
import java.security.Permissions;

import static org.junit.Assert.fail;

public class SandboxTest {

    @Test(expected = AccessControlException.class)
    public void confineTest() throws IOException {
        UnsafeClass unsafeClass = new UnsafeClass();
        try {
            unsafeClass.openSocket();
        } catch (SecurityException ignored) {
            fail();
        }

        Permissions permissions = new Permissions();
        Sandbox.confine(unsafeClass.getClass(), permissions);
        unsafeClass.openSocket();
    }

    static class UnsafeClass {
        public UnsafeClass() {
        }

        public void openSocket() throws IOException {
            System.out.println("opening socket...");
            new ServerSocket(10000);
            System.out.println("success");
        }
    }
}