module contract.executor {
    requires general;
    requires org.apache.commons.lang3;
    requires general.structures;
    requires node.client;
    requires javax.inject;
    requires slf4j.api;
    requires sc.api;
    requires dagger;
    requires executor.client;
    requires libthrift;
    requires java.compiler;
    requires error.prone.annotations;
}