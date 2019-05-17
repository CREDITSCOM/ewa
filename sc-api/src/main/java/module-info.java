module sc.api {
    requires general;
    requires general.structures;
    requires org.apache.commons.lang3;
    exports com.credits.service;
    exports com.credits.scapi.v0;
    exports com.credits.scapi.annotations;
}