module general {
    requires libthrift;
    requires org.apache.commons.lang3;
    requires java.compiler;
    requires slf4j.api;
    requires commons.pool;
    requires org.eclipse.jdt.core;
    requires com.google.common;
    requires jsr250.api;
    requires type.parser;
    requires commons.beanutils.core;
    requires bcprov.jdk15on;
    exports com.credits.general.classload;
    exports com.credits.general.pojo;
    exports com.credits.general.thrift.generated;
    exports com.credits.general.util;
    exports com.credits.general.util.variant;
    exports com.credits.general.exception;
    exports com.credits.general.crypto;
    exports com.credits.general.util.compiler;
    exports com.credits.general.util.exception;
    exports com.credits.general.util.compiler.model;
    exports com.credits.general.serialize;
    exports com.credits.general.thrift;
    exports com.credits.general.crypto.exception;
    exports com.credits.general.util.sourceCode;
}