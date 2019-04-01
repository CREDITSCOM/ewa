package com.credits.secure;

import java.lang.reflect.ReflectPermission;
import java.security.AllPermission;
import java.security.Permissions;

public class PermissionsManager {
    private final Permissions smartContractPermissions;

    public PermissionsManager() {
        smartContractPermissions = new Permissions();
        smartContractPermissions.add(new ReflectPermission("suppressAccessChecks"));
        smartContractPermissions.add(new RuntimePermission("accessDeclaredMembers"));
        smartContractPermissions.add(new RuntimePermission("createClassLoader"));
        smartContractPermissions.add(new RuntimePermission("getProtectionDomain"));
        smartContractPermissions.add(new RuntimePermission("setContextClassLoader"));
    }

    public void dropSmartContractRights(Class<?> contractClass) {
//        Sandbox.confine(contractClass, getSmartContractPermissions());
    }

    public void grantAllPermissions(Class<?> clazz) {
        final Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
//        Sandbox.confine(clazz, permissions);
    }

    public Permissions getSmartContractPermissions() {
        return smartContractPermissions;
    }
}
