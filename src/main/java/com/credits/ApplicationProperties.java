package com.credits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApplicationProperties {
    private final static Logger log = LoggerFactory.getLogger(ApplicationProperties.class);
    private Properties prop = new Properties();

    public String apiHost;
    public int apiPort;
    public int executorPort;

    public ApplicationProperties() {
        File propertyFile = new File(getClass().getResource("../../application.properties").getFile()) ;
        try (FileInputStream fis = new FileInputStream(propertyFile)) {
            prop.load(fis);
        } catch (IOException e) {
            log.error("Can't load properties file. Reason: {}", e.getMessage());
        }

        loadProperties();
    }

    private void loadProperties() {
        apiHost = getProperty("api.server.host");
        apiPort = Integer.parseInt(getProperty("api.server.port"));
        executorPort = Integer.parseInt(getProperty("executor.server.port"));
    }

    private String getProperty(String key) {
        return prop.getProperty(key, "");
    }
}
