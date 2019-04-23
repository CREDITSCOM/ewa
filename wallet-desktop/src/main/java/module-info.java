module wallet.desktop {
    requires javafx.graphics;
    requires javafx.fxml;
    requires slf4j.api;
    requires com.google.common;
    requires org.apache.commons.lang3;
    requires general;
    requires node.client;
    requires javafx.controls;
    requires sc.api;
    requires wellbehavedfx;
    requires flowless;
    requires gson;
    requires org.apache.commons.io;
    requires reactfx;
    requires junit;
    requires java.compiler;
    requires java.datatransfer;
    requires org.eclipse.text;
    requires org.eclipse.jdt.core;
    requires java.desktop;

    requires transitive javafx.base;

    requires transitive java.management;
    requires transitive java.instrument;

    requires transitive richtextfx;

    opens com.credits.wallet.desktop;
}