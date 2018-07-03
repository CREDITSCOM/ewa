package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.SourceCodeUtils;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SourceCodeUtilsTest {
    private static Logger LOGGER = LoggerFactory.getLogger(SourceCodeUtilsTest.class);

    private String sourceCode = "public class Contract extends SmartContract {" +
            "public Contract() {" +
            "  total = 0; " +
            "  } " +
            "public void method01(String arg01, String arg02) {" +
            "  total = 1;" +
            "}" +
            "}";


    @Test
    public void parseMethodsTest() {
        List<MethodDeclaration> methods = SourceCodeUtils.parseMethods(this.sourceCode);
        methods.forEach(method -> {
            LOGGER.info("method.getName() = {}", method.getName());
        });

    }

    @Test
    public void getMethodParametersTest() {
        List<MethodDeclaration> methodDeclarations = SourceCodeUtils.parseMethods(this.sourceCode);
        methodDeclarations.forEach(methodDeclaration -> {
            List parameters = SourceCodeUtils.getMethodParameters(methodDeclaration);
            LOGGER.info("parameters = {}", parameters);
        });
    }

    @Test
    public void formatSourceCodeTest() {
        String sourceCode = "public class Contract extends SmartContract {public Contract() {total = 0; }}";
        String formattedSourceCode = null;
        try {
            formattedSourceCode = SourceCodeUtils.formatSourceCode(sourceCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("formattedSourceCode = {}", formattedSourceCode);

    }
}
