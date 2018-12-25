package com.credits.general.util;

import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralSourceCodeUtilsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralSourceCodeUtilsTest.class);

    @Test
    public void splitClassnameAndGenericTest() {
        Pair<String, String> pair = GeneralSourceCodeUtils.splitClassnameAndGeneric("List<Integer>");
        LOGGER.info(pair.getLeft());
        LOGGER.info(pair.getRight());
    }

    @Test
    public void parseArrayTypeTest() {
        LOGGER.info(GeneralSourceCodeUtils.parseArrayType("double[]"));
    }
}
