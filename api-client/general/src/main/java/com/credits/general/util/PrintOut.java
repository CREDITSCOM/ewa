package com.credits.general.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintOut {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintOut.class);
    private static final String COLLECTION_DELIMITER = ",";

    public static void printBytes(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();

        for(byte element : bytes) {
            stringBuilder.append(element);
            stringBuilder.append(COLLECTION_DELIMITER);
        }
        LOGGER.info(stringBuilder.toString());
    }
}
