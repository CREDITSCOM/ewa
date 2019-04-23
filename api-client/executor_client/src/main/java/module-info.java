module executor.client {
    exports com.credits.client.executor.thrift.generated.apiexec;
    exports com.credits.client.executor.thrift.generated;
    requires jsr250.api;
    requires libthrift;
    requires node.client;
    requires slf4j.api;
    requires general;
}