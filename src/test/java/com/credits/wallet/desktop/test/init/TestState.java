package com.credits.wallet.desktop.test.init;

import com.credits.leveldb.client.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestState {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestState.class);

    public static ApiClient apiClient;
    public static String decimalSeparator;
    public static String creditMonitorURL;

    public static void init() {
        TestStateInitializer initializer = new TestStateInitializer();
        LOGGER.info("Initializing test state");
        initializer.init();
    }
}
