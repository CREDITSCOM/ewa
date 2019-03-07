package com.credits.secure;

import com.credits.ApplicationProperties;

import javax.inject.Inject;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.Permissions;
import java.security.SecurityPermission;
import java.util.PropertyPermission;

import static com.credits.ioc.Injector.INJECTOR;

public class PermissionManager {
    @Inject
    public ApplicationProperties properties;

    public PermissionManager() {
        INJECTOR.component.inject(this);
    }

    public static final String NODE_API_EXEC_SERVICE_CLASS = "com.credits.service.node.apiexec.NodeApiExecServiceImpl";
    public static final String BYTEARRAY_CLASSLOADER_CONSTRUCTOR_CLASS =
        "com.credits.service.contract.ByteArrayClassLoaderConstructor";

    public void createPermissionsForSmartContractClass(Class<?> contractClass) {
        Permissions permissions = createPermissions();
        Sandbox.confine(contractClass, permissions);
    }

    public void createPermissionsForNodeApiExecService() throws ClassNotFoundException {
        Class<?> serviceClass = Class.forName(NODE_API_EXEC_SERVICE_CLASS);
        Permissions permissions = createPermissions();
        permissions.add(
            new SocketPermission(properties.apiHost + ":" + properties.executorNodeApiPort, "connect,listen,resolve"));
        Sandbox.confine(serviceClass, permissions);
    }

    public void createPermissionsForByteArrayClassLoaderConstructor() throws ClassNotFoundException {
        Class<?> classLoaderConstructor = Class.forName(BYTEARRAY_CLASSLOADER_CONSTRUCTOR_CLASS);
        Permissions classLoaderPermissions = createPermissions();
        classLoaderPermissions.add(new RuntimePermission("createClassLoader"));
        Sandbox.confine(classLoaderConstructor, classLoaderPermissions);
    }

    private Permissions createPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new ReflectPermission("suppressAccessChecks"));
        permissions.add(new NetPermission("getProxySelector"));
        permissions.add(new RuntimePermission("readFileDescriptor"));
        permissions.add(new RuntimePermission("writeFileDescriptor"));
        permissions.add(new RuntimePermission("accessDeclaredMembers"));
        permissions.add(new RuntimePermission("accessClassInPackage.sun.security.ec"));
        permissions.add(new RuntimePermission("accessClassInPackage.sun.security.rsa"));
        permissions.add(new RuntimePermission("accessClassInPackage.sun.security.provider"));
        permissions.add(new RuntimePermission("loadLibrary.sunec"));
        permissions.add(new SecurityPermission("getProperty.networkaddress.cache.ttl", "read"));
        permissions.add(new SecurityPermission("getProperty.networkaddress.cache.negative.ttl", "read"));
        permissions.add(new SecurityPermission("getProperty.jdk.jar.disabledAlgorithms"));
        permissions.add(new SecurityPermission("putProviderProperty.SunRsaSign"));
        permissions.add(new SecurityPermission("putProviderProperty.SUN"));
        permissions.add(new PropertyPermission("sun.net.inetaddr.ttl", "read"));
        permissions.add(new PropertyPermission("socksProxyHost", "read"));
        permissions.add(new PropertyPermission("java.net.useSystemProxies", "read"));
        permissions.add(new PropertyPermission("java.home", "read"));
        permissions.add(new PropertyPermission("com.sun.security.preserveOldDCEncoding", "read"));
        permissions.add(new PropertyPermission("sun.security.key.serial.interop", "read"));
        permissions.add(new PropertyPermission("sun.security.rsa.restrictRSAExponent", "read"));
        //                        permissions.add(new FilePermission("<<ALL FILES>>", "read"));
        return permissions;
    }

}
