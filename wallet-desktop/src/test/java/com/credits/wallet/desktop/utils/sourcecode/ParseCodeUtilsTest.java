package com.credits.wallet.desktop.utils.sourcecode;

import com.credits.wallet.desktop.struct.MethodSimpleDeclaration;
import com.credits.wallet.desktop.struct.ParseResultStruct;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ParseCodeUtilsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParseCodeUtilsTest.class);

    private String sourceCode = "public class Contract{\n" +
            "\tpublic int[] testArray(double[] array01, Long[] array02) {}\n" +
            "}";
    @Test
    public void parseClassNameTest() {
        ParseResultStruct build =
            new ParseResultStruct.Builder(sourceCode).fields().constructors().methods().build();

        List<MethodSimpleDeclaration> methods = build.methods;
        List<SingleVariableDeclaration> methodParams = methods.get(0).getMethodDeclaration().parameters();
        methodParams.forEach(methodParam -> {
            LOGGER.info(build.currentClass);
        });
    }
}
